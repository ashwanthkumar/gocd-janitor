package in.ashwanthkumar.gocd.artifacts.config;

import in.ashwanthkumar.gocd.client.TestUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

public class JanitorConfigurationTest {
    @Test
    public void shouldLoadTestConfig() {
        JanitorConfiguration configuration = JanitorConfiguration.load(TestUtils.fileName("/test-config.conf"));
        assertThat(configuration.getServer(), is("http://localhost"));
        assertThat(configuration.getArtifactStorage(), is("/data/go-server/artifacts/"));
        assertThat(configuration.getUsername(), is("foo"));
        assertThat(configuration.getPassword(), is("bar"));
        assertThat(configuration.getDefaultPipelineVersions(), is(5));
        assertThat(configuration.getPipelinePrefix(), is("pipeline-prefix-value"));

        PipelineConfig expected1 = new PipelineConfig("Pipeline1", 2);
        assertThat(configuration.getPipelines(), hasItem(expected1));

        PipelineConfig expected2 = new PipelineConfig("Pipeline2", 5);
        assertThat(configuration.getPipelines(), hasItem(expected2));
    }

}
