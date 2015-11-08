package in.ashwanthkumar.gocd.client;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class MinimalisticGoClientTest {

    @Test
    public void shouldParsPipelineHistory() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFileAsJSON("/responses/pipeline_history.json"));
        Map<Integer, PipelineRunStatus> statusMap = client.pipelineRunStatus("Build-Linux");
        assertThat(statusMap, hasEntry(634, PipelineRunStatus.FAILED));
        assertThat(statusMap, hasEntry(635, PipelineRunStatus.PASSED));
        assertThat(statusMap, hasEntry(636, PipelineRunStatus.PASSED));
        assertThat(statusMap, hasEntry(642, PipelineRunStatus.FAILED));
    }

    @Test
    public void shouldParsePipelineStatus() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFileAsJSON("/responses/pipeline_status.json"));
        PipelineStatus pipelineStatus = client.pipelineStatus("Build-Linux");
        assertThat(pipelineStatus.isLocked(), is(false));
        assertThat(pipelineStatus.isPaused(), is(true));
        assertThat(pipelineStatus.isSchedulable(), is(false));
    }

    @Test
    public void shouldParseUpstreamDependenciesForPipelineRun() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFileAsJSON("/responses/pipeline_value_stream_map.json"));
        List<PipelineDependency> pipelineDependencies = client.upstreamDependencies("distributions-all", 327);
        assertThat(pipelineDependencies.size(), is(8));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("create-maven-release").setVersion(18)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("build-linux").setVersion(641)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("build-windows").setVersion(566)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("plugins").setVersion(500)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("qa-packages").setVersion(499)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("acceptance-gauge").setVersion(63)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("smoke-go-gauge").setVersion(31)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("regression").setVersion(430)));
    }

}