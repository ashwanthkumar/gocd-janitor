package in.ashwanthkumar.gocd.artifacts.config;

import com.typesafe.config.Config;

import java.util.Objects;

public class PipelineConfig {
    private String name;
    private int runsToPersist;

    public static PipelineConfig fromConfig(Integer defaultPipelineRuns, Config config) {
        String name = config.getString("name");

        int runs = defaultPipelineRuns; // default value
        if (config.hasPath("runs"))
            runs = config.getInt("runs");

        return new PipelineConfig(name, runs);
    }

    public PipelineConfig(String name, Integer defaultPipelineRuns) {
        this.name = name;
        this.runsToPersist = defaultPipelineRuns;
    }

    public String getName() {
        return name;
    }

    public PipelineConfig setName(String name) {
        this.name = name;
        return this;
    }

    public int getRunsToPersist() {
        return runsToPersist;
    }

    public PipelineConfig setRunsToPersist(int runsToPersist) {
        this.runsToPersist = runsToPersist;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineConfig that = (PipelineConfig) o;
        return Objects.equals(runsToPersist, that.runsToPersist) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, runsToPersist);
    }
}
