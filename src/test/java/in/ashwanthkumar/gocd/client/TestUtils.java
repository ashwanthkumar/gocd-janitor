package in.ashwanthkumar.gocd.client;

import com.mashape.unirest.http.JsonNode;
import in.ashwanthkumar.utils.io.IO;

import java.io.IOException;
import java.net.URL;

public class TestUtils {
    public static JsonNode readFileAsJSON(String file) throws IOException {
        URL resource = TestUtils.class.getResource("/responses/pipeline_history.json");
        return new JsonNode(IO.fromFile(resource.getPath()));
    }
}
