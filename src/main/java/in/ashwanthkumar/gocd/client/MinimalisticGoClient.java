package in.ashwanthkumar.gocd.client;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

public class MinimalisticGoClient {
    private static Logger LOG = LoggerFactory.getLogger(MinimalisticGoClient.class);

    private String server;
    private String username;
    private String password;

    // for tests
    private JsonNode mockResponse;

    public MinimalisticGoClient(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public PipelineStatus pipelineStatus(String pipeline) throws UnirestException {
        JSONObject result = getJSON("/go/api/pipelines/" + pipeline + "/status").getObject();
        return new PipelineStatus()
                .setLocked(result.getBoolean("locked"))
                .setPaused(result.getBoolean("paused"))
                .setSchedulable(result.getBoolean("schedulable"));
    }

    public Map<Integer, PipelineRunStatus> pipelineRunStatus(String pipeline) throws UnirestException {
        return pipelineRunStatus(pipeline, 0);
    }

    public Map<Integer, PipelineRunStatus> pipelineRunStatus(String pipeline, int offset) throws UnirestException {
        Map<Integer, PipelineRunStatus> result = new TreeMap<Integer, PipelineRunStatus>();
        JSONArray history = getJSON(buildUrl("/go/api/pipelines/" + pipeline + "/history/" + offset))
                .getObject().getJSONArray("pipelines");
        for (int i = 0; i < history.length(); i++) {
            JSONObject run = history.getJSONObject(i);
            if (run.getBoolean("preparing_to_schedule"))
                continue;

            PipelineRunStatus status = pipelineStatusFrom(run);
            result.put(run.getInt("counter"), status);
        }
        return result;
    }

    private PipelineRunStatus pipelineStatusFrom(JSONObject run) {
        JSONArray pipelineStages = run.getJSONArray("stages");
        for (int j = 0; j < pipelineStages.length(); j++) {
            // Since there isn't an universal way to say if the pipeline has failed or not, because
            // A stage could fail, but we could deem it unimportant (for the time being) and continue the pipeline.

            // We are a little sensitive about what we call failures of a pipeline. Possible Reasons -
            // 1. Any 1 stage failure is considered a pipeline failure.
            // 2. If the pipeline doesn't run to completion (paused or locked) is considered a failure.
            JSONObject stageRun = pipelineStages.getJSONObject(j);
            boolean stageFailed = !stageRun.has("result") || stageRun.getString("result").equalsIgnoreCase("failed");
            if (stageFailed) {
                return PipelineRunStatus.FAILED;
            }
        }

        return PipelineRunStatus.PASSED;
    }

    // for testing only
    /* default */ void setMockResponse(JsonNode response) {
        this.mockResponse = response;
    }

    private JsonNode getJSON(String resource) throws UnirestException {
        if (this.mockResponse != null) return this.mockResponse;
        else return Unirest.get(resource)
                .basicAuth(username, password)
                .asJson().getBody();
    }

    private String buildUrl(String resource) {
        try {
            return URI.create(String.format("%s/%s", server, resource)).normalize().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
