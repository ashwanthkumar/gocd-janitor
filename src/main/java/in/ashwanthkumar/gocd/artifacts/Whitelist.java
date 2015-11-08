package in.ashwanthkumar.gocd.artifacts;

import in.ashwanthkumar.gocd.client.PipelineDependency;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.collections.Maps;

import java.util.*;

public class WhiteList {
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
        return isNumber(version) && hasItem(new PipelineDependency(pipeline, Integer.valueOf(version)));
    }

    public boolean hasItem(PipelineDependency dependency) {
        return pipelines.containsKey(dependency.getPipelineName()) &&
                pipelines.get(dependency.getPipelineName()).contains(dependency);
    }

    public Iterable<String> pipelinesUnderRadar() {
        return pipelines.keySet();
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
