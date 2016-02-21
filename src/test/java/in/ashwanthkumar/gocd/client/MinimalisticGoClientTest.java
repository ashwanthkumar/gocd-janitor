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
        client.setMockResponse(TestUtils.readFile("/responses/pipeline_history.json"));
        Map<Integer, PipelineRunStatus> statusMap = client.pipelineRunStatus("Build-Linux");
        assertThat(statusMap, hasEntry(634, PipelineRunStatus.FAILED));
        assertThat(statusMap, hasEntry(635, PipelineRunStatus.PASSED));
        assertThat(statusMap, hasEntry(636, PipelineRunStatus.PASSED));
        assertThat(statusMap, hasEntry(642, PipelineRunStatus.FAILED));
    }

    @Test
    public void shouldParsePipelineStatus() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFile("/responses/pipeline_status.json"));
        PipelineStatus pipelineStatus = client.pipelineStatus("Build-Linux");
        assertThat(pipelineStatus.isLocked(), is(false));
        assertThat(pipelineStatus.isPaused(), is(true));
        assertThat(pipelineStatus.isSchedulable(), is(false));
    }

    @Test
    public void shouldParseUpstreamDependenciesForPipelineRun() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFile("/responses/pipeline_value_stream_map.json"));
        List<PipelineDependency> pipelineDependencies = client.upstreamDependencies("distributions-all", 327);
        assertThat(pipelineDependencies.size(), is(9));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("distributions-all").setVersion(327)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("create-maven-release").setVersion(18)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("build-linux").setVersion(641)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("build-windows").setVersion(566)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("plugins").setVersion(500)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("qa-packages").setVersion(499)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("acceptance-gauge").setVersion(63)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("smoke-go-gauge").setVersion(31)));
        assertThat(pipelineDependencies, hasItem(new PipelineDependency().setPipelineName("regression").setVersion(430)));
    }

    @Test
    public void shouldParseAllPipelineNames() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFile("/responses/pipelines.xml"));
        List<String> pipelines = client.allPipelineNames("");
        assertThat(pipelines.size(), is(17));
        assertThat(pipelines, hasItem("create-maven-release"));
        assertThat(pipelines, hasItem("build-linux"));
        assertThat(pipelines, hasItem("build-windows"));
        assertThat(pipelines, hasItem("plugins"));
        assertThat(pipelines, hasItem("qa-packages"));
        assertThat(pipelines, hasItem("smoke-go-gauge"));
        assertThat(pipelines, hasItem("acceptance-gauge"));
        assertThat(pipelines, hasItem("regression"));
        assertThat(pipelines, hasItem("distributions-all"));
        assertThat(pipelines, hasItem("goutils"));
        assertThat(pipelines, hasItem("plugin-api-upload"));
        assertThat(pipelines, hasItem("installer_testing"));
        assertThat(pipelines, hasItem("user-documentation"));
        assertThat(pipelines, hasItem("smoke"));
        assertThat(pipelines, hasItem("acceptance"));
        assertThat(pipelines, hasItem("create-maven-release-PR"));
        assertThat(pipelines, hasItem("build-linux-PR"));
    }

    @Test
    public void shouldParsePipelineNamesWithSpecifiedPrefix() throws IOException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFile("/responses/pipelines.xml"));
        List<String> pipelines = client.allPipelineNames("build");
        assertThat(pipelines.size(), is(3));
        assertThat(pipelines, hasItem("build-linux"));
        assertThat(pipelines, hasItem("build-windows"));
        assertThat(pipelines, hasItem("build-linux-PR"));
    }

}
