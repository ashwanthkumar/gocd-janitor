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

        PipelineConfig expected1 = new PipelineConfig().setName("Pipeline1").setRunsToPersist(2);
        assertThat(configuration.getPipelines(), hasItem(expected1));

        PipelineConfig expected2 = new PipelineConfig().setName("Pipeline2").setRunsToPersist(5);
        assertThat(configuration.getPipelines(), hasItem(expected2));
    }

}