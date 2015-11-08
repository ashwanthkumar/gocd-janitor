package in.ashwanthkumar.gocd.client;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class MinimalisticGoClientTest {

    @Test
    public void shouldParsPipelineHistory() throws IOException, UnirestException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFileAsJSON("/responses/pipeline_history.json"));
        Map<Integer, PipelineRunStatus> statusMap = client.pipelineRunStatus("Build-Linux");
        assertThat(statusMap, hasEntry(634, PipelineRunStatus.FAILED));
        assertThat(statusMap, hasEntry(635, PipelineRunStatus.PASSED));
        assertThat(statusMap, hasEntry(636, PipelineRunStatus.PASSED));
        assertThat(statusMap, hasEntry(642, PipelineRunStatus.FAILED));
    }

    @Test
    public void shouldParsePipelineStatus() throws IOException, UnirestException {
        MinimalisticGoClient client = new MinimalisticGoClient("http://server", "foo", "bar");
        client.setMockResponse(TestUtils.readFileAsJSON("/responses/pipeline_status.json"));
        PipelineStatus pipelineStatus = client.pipelineStatus("Build-Linux");
        assertThat(pipelineStatus.isLocked(), is(false));
        assertThat(pipelineStatus.isPaused(), is(true));
        assertThat(pipelineStatus.isSchedulable(), is(false));
    }


}