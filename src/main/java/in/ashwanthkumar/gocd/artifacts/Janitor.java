package in.ashwanthkumar.gocd.artifacts;

import in.ashwanthkumar.gocd.artifacts.config.JanitorConfiguration;
import in.ashwanthkumar.gocd.artifacts.config.PipelineConfig;
import in.ashwanthkumar.gocd.client.MinimalisticGoClient;
import in.ashwanthkumar.gocd.client.PipelineDependency;
import in.ashwanthkumar.gocd.client.PipelineRunStatus;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.tuple.Tuple2;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static in.ashwanthkumar.utils.collections.Lists.*;

public class Janitor {
    private static final Logger LOG = LoggerFactory.getLogger(Janitor.class);

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("dry-run", "Doesn't delete anything just emits the files for deletion");
        parser.accepts("config", "Path to janitor configuration").withRequiredArg().required();
        parser.accepts("help", "Display this help message").forHelp();
        OptionSet options = parser.parse(args);
        if (options.has("help")) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }
        String configPath = (String) options.valueOf("config");
        new Janitor().run(configPath, options.has("dry-run"));
    }

    public void run(String pathToConfiguration, Boolean dryRun) {
        final JanitorConfiguration config = JanitorConfiguration.load(pathToConfiguration);
        LOG.info("Starting Janitor");
        LOG.info("Go Server - " + config.getServer());
        LOG.info("Artifact Dir - " + config.getArtifactStorage());
        if (dryRun) {
            LOG.info("Working in Dry run mode, we will not delete anything in this run.");
        }

        final MinimalisticGoClient client = new MinimalisticGoClient(config.getServer(), config.getUsername(), config.getPassword());

        List<PipelineConfig> pipelinesNotInConfig = pipelinesNotInConfiguration(client, config);
        List<Tuple2<String, List<Integer>>> requiredPipelineAndVersions = mandatoryPipelineVersions(client, concat(config.getPipelines(), pipelinesNotInConfig));
        WhiteList whiteList = computeWhiteList(client, requiredPipelineAndVersions);

        LOG.info("Number of white listed pipeline instances - " + whiteList.size());
        for (PipelineDependency pipelineDependency : whiteList.it()) {
            LOG.debug("[WhiteList] - " + pipelineDependency);
        }

        long deletedBytes = doDeletes(whiteList, config.getArtifactStorage(), dryRun);

        LOG.info("Total bytes deleted so far - " + FileUtils.byteCountToDisplaySize(deletedBytes));
        LOG.info("Shutting down Janitor");
    }

    /* default */ List<PipelineConfig> pipelinesNotInConfiguration(MinimalisticGoClient client, final JanitorConfiguration config) {
        return map(
                filter(client.allPipelineNames(), new Predicate<String>() {
                    @Override
                    public Boolean apply(String pipeline) {
                        return !config.hasPipeline(pipeline);
                    }
                }),
                new Function<String, PipelineConfig>() {
                    @Override
                    public PipelineConfig apply(String pipelineName) {
                        return new PipelineConfig(pipelineName, config.getPipelineVersions());
                    }
                });
    }

    /* default */ long doDeletes(WhiteList whiteList, String artifactStorage, Boolean dryRun) {
        long deletedBytes = 0;
        for (String pipeline : whiteList.pipelinesUnderRadar()) {
            LOG.info("Looking for pipeline - " + pipeline);
            File pipelineDirectory = new File(artifactStorage + "/" + pipeline);
            File[] versionDirs = listFiles(pipelineDirectory.getAbsolutePath());
            for (File versionDir : versionDirs) {
                if (whiteList.contains(pipelineDirectory.getName(), versionDir.getName())) {
                    LOG.info("Skipping since it is white listed" + versionDir.getAbsolutePath());
                } else {
                    deletedBytes += delete(versionDir, dryRun);
                }
            }
        }
        return deletedBytes;
    }

    /* default */ long delete(File path, boolean dryRun) {
        long size = FileUtils.sizeOfDirectory(path);
        if (dryRun) {
            LOG.info("[DRY RUN] Will delete " + path.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
        } else {
            LOG.info("Deleting " + path.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
            try {
                FileUtils.deleteDirectory(path);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        return size;
    }

    /* default */ List<Tuple2<String, List<Integer>>> mandatoryPipelineVersions(final MinimalisticGoClient client, List<PipelineConfig> pipelines) {
        return map(pipelines, new Function<PipelineConfig, Tuple2<String, List<Integer>>>() {
            @Override
            public Tuple2<String, List<Integer>> apply(PipelineConfig pipelineConfig) {
                List<Integer> versions = new ArrayList<>();
                int offset = 0;
                while (versions.size() < pipelineConfig.getRunsToPersist()) {
                    Set<Map.Entry<Integer, PipelineRunStatus>> pipelineStatuses = client.pipelineRunStatus(pipelineConfig.getName(), offset).entrySet();
                    if (pipelineStatuses.isEmpty()) {
                        break;
                    }

                    versions.add(head(pipelineStatuses).getKey());     // Latest run version irrespective of its status will be added to whitelist
                    versions.add(head(pipelineStatuses).getKey() + 1); // current run of the pipeline (if any) - History endpoint doesn't expose current running pipeline info

                    versions.addAll(
                            take(map(filter(pipelineStatuses, new Predicate<Map.Entry<Integer, PipelineRunStatus>>() {
                                @Override
                                public Boolean apply(Map.Entry<Integer, PipelineRunStatus> entry) {
                                    return entry.getValue() == PipelineRunStatus.PASSED;
                                }
                            }), new Function<Map.Entry<Integer, PipelineRunStatus>, Integer>() {
                                @Override
                                public Integer apply(Map.Entry<Integer, PipelineRunStatus> entry) {
                                    return entry.getKey();
                                }
                            }), pipelineConfig.getRunsToPersist())
                    );
                    offset += 10; // default page size is 10
                }
                return new Tuple2<>(pipelineConfig.getName(), versions);
            }
        });
    }

    /* default */ WhiteList computeWhiteList(final MinimalisticGoClient client, List<Tuple2<String, List<Integer>>> requiredPipelineAndVersions) {
        return new WhiteList(flatten(map(requiredPipelineAndVersions, new Function<Tuple2<String, List<Integer>>, List<PipelineDependency>>() {
            @Override
            public List<PipelineDependency> apply(Tuple2<String, List<Integer>> tuple) {
                final String pipelineName = tuple._1();
                List<Integer> versions = tuple._2();
                return flatten(map(versions, new Function<Integer, List<PipelineDependency>>() {
                    @Override
                    public List<PipelineDependency> apply(Integer version) {
                        return client.upstreamDependencies(pipelineName, version);
                    }
                }));
            }
        })));
    }

    private File[] listFiles(String path) {
        File[] files = new File(path).listFiles();
        if (files == null) throw new RuntimeException(path + " did not yield any pipeline run directories");
        else return files;
    }

    private <T> List<T> take(List<T> list, int k) {
        List<T> topK = Lists.<T>Nil();
        for (T aList : list) {
            if (topK.size() < k) topK.add(aList);
            else break;
        }
        return topK;
    }

    private <T> T head(Set<T> set) {
        if (!set.isEmpty()) return set.iterator().next();
        else throw new RuntimeException("head of an empty Set");
    }

}
