package in.ashwanthkumar.gocd.artifacts;

import in.ashwanthkumar.gocd.client.PipelineDependency;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static in.ashwanthkumar.utils.collections.Lists.map;

public class WhiteList {
    private static final Logger LOG = LoggerFactory.getLogger(WhiteList.class);
    private Map<String, List<PipelineDependency>> pipelines;

    public WhiteList(Set<PipelineDependency> pipelines) {
        this.pipelines = new HashMap<>();
        for (PipelineDependency pipeline : pipelines) {
            List<PipelineDependency> pipelineDependencies = get(pipeline.getPipelineName());
            pipelineDependencies.add(pipeline);
            this.pipelines.put(pipeline.getPipelineName(), pipelineDependencies);
        }
    }

    public WhiteList(Collection<PipelineDependency> pipelines) {
        this(new HashSet<>(pipelines));
    }

    public int size() {
        return pipelines.size();
    }

    public Iterable<PipelineDependency> it() {
        return Lists.flatten(pipelines.values());
    }

    public boolean contains(String pipeline, String version) {
        boolean result = isNumber(version) &&
                hasItem(new PipelineDependency(pipeline, Integer.valueOf(version)));

        if (!result) {
            LOG.debug(pipeline + "@" + version + " was not found because only the following are white listed - " + versionsForPipeline(pipeline));
        }
        return result;
    }

    public boolean hasItem(PipelineDependency dependency) {
        return pipelines.containsKey(dependency.getPipelineName()) &&
                pipelines.get(dependency.getPipelineName()).contains(dependency);
    }

    public Set<String> pipelinesUnderRadar() {
        return pipelines.keySet();
    }

    List<Integer> versionsForPipeline(String pipeline) {
        List<Integer> versions = map(pipelines.get(pipeline), new Function<PipelineDependency, Integer>() {
            @Override
            public Integer apply(PipelineDependency dependency) {
                return dependency.getVersion();
            }
        });

        Collections.sort(versions);
        return versions;
    }

    boolean isNumber(String value) {
        try {
            return Integer.valueOf(value) != null;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    List<PipelineDependency> get(String name) {
        if (pipelines.containsKey(name)) return pipelines.get(name);
        else return new ArrayList<>();
    }
}
