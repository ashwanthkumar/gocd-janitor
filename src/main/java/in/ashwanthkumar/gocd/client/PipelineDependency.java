package in.ashwanthkumar.gocd.client;

import java.util.Objects;

public class PipelineDependency {
    private String pipelineName;
    private Integer version;

    public PipelineDependency() {}
    public PipelineDependency(String pipelineName, Integer version) {
        this.pipelineName = pipelineName;
        this.version = version;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public PipelineDependency setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public PipelineDependency setVersion(Integer version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineDependency that = (PipelineDependency) o;
        return Objects.equals(pipelineName, that.pipelineName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipelineName, version);
    }

    @Override
    public String toString() {
        return pipelineName + "@" + version;
    }
}
