package in.ashwanthkumar.gocd.artifacts.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import in.ashwanthkumar.gocd.hocon.HoconUtils;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JanitorConfiguration {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private String server;
    private String username;
    private String password;
    private String artifactStorage;
    private Integer defaultPipelineVersions;
    private List<PipelineConfig> pipelines;
    private Set<String> pipelineNames;
    private String pipelinePrefix;
    private Set<String> pipelinesToIgnore;

    public static JanitorConfiguration load(String file) {
        return load(ConfigFactory.parseFile(new File(file)));
    }

    public static JanitorConfiguration load(Config config) {
        Config envThenSystem = ConfigFactory.systemEnvironment().withFallback(ConfigFactory.systemProperties());
        config = config.resolveWith(envThenSystem);

        config = config.getConfig("gocd.janitor");

        final JanitorConfiguration janitorConfiguration = new JanitorConfiguration()
                .setServer(config.getString("server"))
                .setArtifactStorage(config.getString("artifacts-dir"))
                .setDefaultPipelineVersions(config.getInt("pipeline-versions"))
                .setPipelinePrefix(HoconUtils.getString(config, "pipeline-prefix", ""))
                .setPipelinesToIgnore(new HashSet<>(HoconUtils.getStringListOrEmpty(config, "pipelines-to-ignore")));

        if (config.hasPath(USERNAME) && config.hasPath(PASSWORD)) {
            janitorConfiguration.setUsername(config.getString(USERNAME))
                                .setPassword(config.getString(PASSWORD));
        }

        List<PipelineConfig> pipelines = Lists.map((List<Config>) config.getConfigList("pipelines"), new Function<Config, PipelineConfig>() {
            @Override
            public PipelineConfig apply(Config config) {
                return PipelineConfig.fromConfig(janitorConfiguration.getDefaultPipelineVersions(), config);
            }
        });

        return janitorConfiguration.setPipelines(pipelines);
    }

    public String getServer() {
        return server;
    }

    public JanitorConfiguration setServer(String server) {
        this.server = server;
        return this;
    }

    public String getArtifactStorage() {
        return artifactStorage;
    }

    public JanitorConfiguration setArtifactStorage(String artifactStorage) {
        this.artifactStorage = artifactStorage;
        return this;
    }

    public List<PipelineConfig> getPipelines() {
        return pipelines;
    }

    public JanitorConfiguration setPipelines(List<PipelineConfig> pipelines) {
        this.pipelines = pipelines;
        setPipelineNames();
        return this;
    }

    public String getUsername() {
        return username;
    }

    public JanitorConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JanitorConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getDefaultPipelineVersions() {
        return defaultPipelineVersions;
    }

    public JanitorConfiguration setDefaultPipelineVersions(Integer defaultPipelineVersions) {
        this.defaultPipelineVersions = defaultPipelineVersions;
        return this;
    }

    public JanitorConfiguration setPipelinePrefix(String pipelinePrefix) {
        this.pipelinePrefix = pipelinePrefix;
        return this;
    }

    public boolean hasPipeline(String pipeline) {
        return pipelineNames.contains(pipeline);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JanitorConfiguration that = (JanitorConfiguration) o;
        return Objects.equals(server, that.server) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(artifactStorage, that.artifactStorage) &&
                Objects.equals(pipelines, that.pipelines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, username, password, artifactStorage, pipelines);
    }

    void setPipelineNames() {
        this.pipelineNames = new HashSet<>(Lists.map(pipelines, new Function<PipelineConfig, String>() {
            @Override
            public String apply(PipelineConfig pipelineConfig) {
                return pipelineConfig.getName();
            }
        }));
    }

    public String getPipelinePrefix() {
        return pipelinePrefix;
    }

    public Set<String> getPipelinesToIgnore() {
        if(pipelinesToIgnore != null) return pipelinesToIgnore;
        else return new HashSet<>();
    }

    public JanitorConfiguration setPipelinesToIgnore(Set<String> pipelinesToIgnore) {
        this.pipelinesToIgnore = pipelinesToIgnore;
        return this;
    }
}
