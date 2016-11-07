package in.ashwanthkumar.gocd.artifacts;

import in.ashwanthkumar.gocd.actions.Action;
import in.ashwanthkumar.gocd.actions.DeleteAction;
import in.ashwanthkumar.gocd.actions.MoveAction;
import in.ashwanthkumar.gocd.artifacts.config.JanitorConfiguration;
import in.ashwanthkumar.gocd.artifacts.config.PipelineConfig;
import in.ashwanthkumar.gocd.client.GoCD;
import in.ashwanthkumar.gocd.client.types.PipelineDependency;
import in.ashwanthkumar.gocd.client.types.PipelineRunStatus;
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
import java.util.*;

import static in.ashwanthkumar.utils.collections.Lists.*;

public class Janitor {
    private static final Logger LOG = LoggerFactory.getLogger(Janitor.class);
    private Action action;
    private GoCD client;
    private final JanitorConfiguration config;

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("dry-run", "Doesn't delete anything just emits the files for deletion");
        parser.accepts("config", "Path to janitor configuration").withRequiredArg().required();
        parser.accepts("delete-artifacts", "Delete the artifacts");
        parser.accepts("move-artifacts", "Move the artifacts to <destination path>").withRequiredArg();
        parser.accepts("help", "Display this help message").forHelp();
        OptionSet options = parser.parse(args);
        if (options.has("help")) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }
        Action action = null;
        if (options.has("delete-artifacts") && options.has("move-artifacts")) {
            System.err.println("I can't both move and delete artifacts. Please choose a single action");
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        if (options.has("delete-artifacts")) {
            action = new DeleteAction("cruise-output");
        } else if (options.has("move-artifacts")) {
            action = new MoveAction(new File((String) options.valueOf("move-artifacts")));
        }

        if (action == null) {
            System.err.println("You need to specify one of --move-artifacts <destination> or --delete-artifacts");
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        JanitorConfiguration config = JanitorConfiguration.load((String) options.valueOf("config"));
        GoCD client = new GoCD(config.getServer(), config.getUsername(), config.getPassword());

        new Janitor(action, config, client).run(options.has("dry-run"));
    }

    public Janitor(Action action, JanitorConfiguration config, GoCD client) {
        this.action = action;
        this.config = config;
        this.client = client;
    }

    public void run(Boolean dryRun) throws IOException {
        LOG.info("Starting Janitor");
        LOG.info("Go Server - " + config.getServer());
        LOG.info("Artifact Dir - " + config.getArtifactStorage());

        if (dryRun) {
            LOG.info("Working in Dry run mode, we will not delete anything in this run.");
        }

        List<PipelineConfig> pipelines = getPipelines();
        List<Tuple2<String, Set<Integer>>> pipelineVersionsToRetain = pipelineVersionsToRetain(pipelines);
        WhiteList whiteList = computeWhiteList(pipelineVersionsToRetain);

        LOG.info("Number of white listed pipeline instances - " + whiteList.size());
        for (PipelineDependency pipelineDependency : whiteList.it()) {
            LOG.debug("[WhiteList] - " + pipelineDependency);
        }

        long bytesProcessed = performAction(whiteList, config.getArtifactStorage(), dryRun);

        LOG.info("Total bytes (deleted / moved) - " + FileUtils.byteCountToDisplaySize(bytesProcessed));
        LOG.info("Shutting down Janitor");
    }

    /* default */ List<PipelineConfig> pipelinesNotInConfiguration() throws IOException {
        return map(
                filter(client.allPipelineNames(config.getPipelinePrefix()), new Predicate<String>() {
                    @Override
                    public Boolean apply(String pipeline) {
                        return !config.hasPipeline(pipeline);
                    }
                }),
                new Function<String, PipelineConfig>() {
                    @Override
                    public PipelineConfig apply(String pipelineName) {
                        return new PipelineConfig(pipelineName, config.getDefaultPipelineVersions());
                    }
                });
    }

    /* default */ long performAction(WhiteList whiteList, String artifactStorage, Boolean dryRun) {
        long deletedBytes = 0;

        for (String pipeline : whiteList.pipelinesUnderRadar()) {
            LOG.info("Looking for pipeline - " + pipeline);
            File pipelineDirectory = new File(artifactStorage + "/" + pipeline);
            File[] versionDirs = listFiles(pipelineDirectory.getAbsolutePath());
            for (File versionDir : versionDirs) {
                if (whiteList.contains(pipelineDirectory.getName(), versionDir.getName())) {
                    LOG.info("Skipping since it is white listed - " + versionDir.getAbsolutePath());
                } else {
                    deletedBytes += action.invoke(pipelineDirectory, versionDir.getName(), dryRun);
                }
            }
        }

        return deletedBytes;
    }

    /* default */ List<Tuple2<String, Set<Integer>>> pipelineVersionsToRetain(List<PipelineConfig> pipelines) {
        LOG.info("Calculating which pipeline versions to retain.");

        return map(pipelines, new Function<PipelineConfig, Tuple2<String, Set<Integer>>>() {
            @Override
            public Tuple2<String, Set<Integer>> apply(PipelineConfig pipelineConfig) {
                Set<Integer> versions = new HashSet<>();
                int offset = 0;
                String name = pipelineConfig.getName();

                Set<Map.Entry<Integer, PipelineRunStatus>> pipelineStatuses = getPipelineStatuses(name, offset);

                if (!pipelineStatuses.isEmpty()) {
                    Integer latestVersion = max(pipelineStatuses).getKey();
                    versions.add(latestVersion);     // Latest run version irrespective of its status will be added to whitelist
                    versions.add(latestVersion + 1); // current run of the pipeline (if any) - History endpoint doesn't expose current running pipeline info
                }

                while (versions.size() < pipelineConfig.getRunsToPersist() + 2 /* the max and max + 1 versions */) {
                    if (pipelineStatuses.isEmpty())
                        break;

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

                    offset += pipelineStatuses.size();

                    pipelineStatuses = getPipelineStatuses(name, offset);
                }

                LOG.info("Versions of pipeline " + name + " to retain: " + Arrays.toString(versions.toArray()));

                return new Tuple2<>(name, versions);
            }
        });
    }

    private List<PipelineConfig> getPipelines() throws IOException {
        LOG.info("Finding pipelines to process");
        List<PipelineConfig> pipelines = concat(config.getPipelines(), pipelinesNotInConfiguration());
        LOG.info(pipelines.size() + " pipelines found");

        return pipelines;
    }

    private Set<Map.Entry<Integer, PipelineRunStatus>> getPipelineStatuses(String pipelineName, int offset) {
        try {
            return client.pipelineRunStatus(pipelineName, offset).entrySet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* default */ WhiteList computeWhiteList(List<Tuple2<String, Set<Integer>>> requiredPipelineAndVersions) {
        return new WhiteList(flatten(map(requiredPipelineAndVersions, new Function<Tuple2<String, Set<Integer>>, List<PipelineDependency>>() {
            @Override
            public List<PipelineDependency> apply(Tuple2<String, Set<Integer>> tuple) {
                final String pipelineName = tuple._1();
                Set<Integer> versions = tuple._2();
                return flatten(map(versions, new Function<Integer, List<PipelineDependency>>() {
                    @Override
                    public List<PipelineDependency> apply(Integer version) {
                        try {
                            return client.upstreamDependencies(pipelineName, version);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }
        })));
    }

    private File[] listFiles(String path) {
        File[] files = new File(path).listFiles();
        if (files == null) {
            LOG.debug("Moving On - There are no files under " + path);
            return new File[0];
        }
        else return files;
    }

    private <T> List<T> take(List<T> list, int k) {
        List<T> topK = Lists.Nil();
        for (T aList : list) {
            if (topK.size() < k) topK.add(aList);
            else break;
        }
        return topK;
    }

    private Map.Entry<Integer, PipelineRunStatus> max(Set<Map.Entry<Integer, PipelineRunStatus>> set) {
        if (!set.isEmpty()) {
            Map.Entry<Integer, PipelineRunStatus> largest = set.iterator().next();
            for (Map.Entry<Integer, PipelineRunStatus> item : set) {
                if (item.getKey() > largest.getKey()) largest = item;
            }
            return largest;
        } else throw new RuntimeException("max of an empty Set");
    }

}
