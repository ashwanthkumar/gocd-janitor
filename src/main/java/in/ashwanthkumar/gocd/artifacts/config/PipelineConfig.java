package in.ashwanthkumar.gocd.artifacts.config;

import com.typesafe.config.Config;

import java.util.Objects;

public class PipelineConfig {
    // I really don't want end up making too many HTTP requests and what's the point of more than this anyway? :-|
    public static final int MAX_RUN_LIMIT = 5;
    public static final int DEFAULT_RUN_LIMIT = 2;

    private String name;
    private int runsToPersist;

    public static PipelineConfig fromConfig(Config config) {
        String name = config.getString("name");

        int runs = DEFAULT_RUN_LIMIT; // default value
        if (config.hasPath("runs"))
            runs = config.getInt("runs");

        if (runs > MAX_RUN_LIMIT)
            throw new RuntimeException(name + " has " + runs + " runs configured, but the max limit is " + MAX_RUN_LIMIT);

        return new PipelineConfig()
                .setName(name)
                .setRunsToPersist(runs);
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
