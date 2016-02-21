package in.ashwanthkumar.gocd.client;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;

import static in.ashwanthkumar.utils.collections.Lists.filter;
import static in.ashwanthkumar.utils.collections.Lists.map;

public class MinimalisticGoClient {
    private static Logger LOG = LoggerFactory.getLogger(MinimalisticGoClient.class);

    private String server;
    private String username;
    private String password;

    // for tests
    private String mockResponse;

    public MinimalisticGoClient(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public List<String> allPipelineNames(final String pipelinePrefix) {
        String xml = getXML("/go/api/pipelines.xml");
        Document doc = Jsoup.parse(xml);
        Elements pipelineElements = doc.select("pipeline[href]");
        List<String> pipelines = filter(map(pipelineElements, new Function<Element, String>() {
            @Override
            public String apply(Element element) {
                String href = element.attr("href");
                String apiPrefix = "/go/api/pipelines/";
                return href.substring(href.indexOf(apiPrefix) + apiPrefix.length(), href.indexOf("/stages.xml"));
            }
        }),new Predicate<String>(){
            @Override
            public Boolean apply(String s) {
                if(StringUtils.isEmpty(pipelinePrefix)){
                    return true;
                }
                return s.startsWith(pipelinePrefix);
            }
        });

        return pipelines;
    }

    public List<PipelineDependency> upstreamDependencies(String pipeline, int version) {
        JSONObject result = getJSON("/go/pipelines/value_stream_map/" + pipeline + "/" + version + ".json").getObject();
        List<PipelineDependency> dependencies = Lists.of(new PipelineDependency(pipeline, version));

        // happens typically when we check for next run
        // in case of connection errors it should fail before this
        if(!result.has("levels")) return dependencies;

        JSONArray levels = result.getJSONArray("levels");
        for (int i = 0; i < levels.length(); i++) {
            JSONArray nodes = levels.getJSONObject(i).getJSONArray("nodes");
            for (int j = 0; j < nodes.length(); j++) {
                JSONObject node = nodes.getJSONObject(j);
                String name = node.getString("name");
                // The VSM JSON is always ordered (left to right in VSM view) set of dependencies
                if (name.equals(pipeline)) return dependencies;

                // We pick only the PIPELINE type dependencies
                if (node.getString("node_type").equalsIgnoreCase("PIPELINE")) {
                    JSONArray instances = node.getJSONArray("instances");
                    for (int k = 0; k < instances.length(); k++) {
                        JSONObject instance = instances.getJSONObject(k);
                        int counter = instance.getInt("counter");
                        dependencies.add(
                                new PipelineDependency()
                                        .setPipelineName(name)
                                        .setVersion(counter)
                        );
                    }
                }
            }
        }

        return dependencies;
    }

    public PipelineStatus pipelineStatus(String pipeline) {
        JSONObject result = getJSON("/go/api/pipelines/" + pipeline + "/status").getObject();
        return new PipelineStatus()
                .setLocked(result.getBoolean("locked"))
                .setPaused(result.getBoolean("paused"))
                .setSchedulable(result.getBoolean("schedulable"));
    }

    public Map<Integer, PipelineRunStatus> pipelineRunStatus(String pipeline) {
        return pipelineRunStatus(pipeline, 0);
    }

    public Map<Integer, PipelineRunStatus> pipelineRunStatus(String pipeline, int offset) {
        Map<Integer, PipelineRunStatus> result = new TreeMap<>(Collections.reverseOrder());
        JSONArray history = getJSON("/go/api/pipelines/" + pipeline + "/history/" + offset)
                .getObject().getJSONArray("pipelines");
        for (int i = 0; i < history.length(); i++) {
            JSONObject run = history.getJSONObject(i);
            if (run.getBoolean("preparing_to_schedule"))
                continue;

            PipelineRunStatus status = pipelineStatusFrom(run);
            int counter = run.getInt("counter");
            LOG.debug(pipeline + "@" + counter + " has " + status);
            result.put(counter, status);
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
    /* default */ void setMockResponse(String response) {
        this.mockResponse = response;
    }

    private JsonNode getJSON(String resource) {
        if (this.mockResponse != null) return new JsonNode(this.mockResponse);
        else try {
            return invokeGET(resource).asJson().getBody();
        } catch (UnirestException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String getXML(String resource) {
        if (this.mockResponse != null) return this.mockResponse;
        else try {
            return invokeGET(resource).asString().getBody();
        } catch (UnirestException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private GetRequest invokeGET(String resource) throws UnirestException {
        String url = buildUrl(resource);
        LOG.debug("Hitting " + url);
        // Having a large timeout (10 min) because sometimes a pipeline VSM could be very very large
        Unirest.setTimeouts(600 * 1000L, 600 * 1000L);
        return Unirest.get(url)
                .basicAuth(username, password);
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
