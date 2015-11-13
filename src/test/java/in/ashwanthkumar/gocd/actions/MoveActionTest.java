package in.ashwanthkumar.gocd.actions;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static in.ashwanthkumar.gocd.actions.ActionUtils.createFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MoveActionTest {
    @Test
    public void shouldMoveDirectFromSourceToDestination() throws IOException {
        Path pipelineDir = Files.createTempDirectory("source");
        String pipelineName = pipelineDir.toFile().getName();
        createFile(pipelineDir, "1", "stage-1", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-1", "blah", "blah");
        createFile(pipelineDir, "1", "stage-2", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-2", "foo", "bar");
        createFile(pipelineDir, "1", "stage-3", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-3", "bar", "baz");

        Path destinationDirectory = Files.createTempDirectory("destination");
        FileUtils.deleteDirectory(destinationDirectory.toFile());

        MoveAction action = new MoveAction(destinationDirectory.toFile());
        long size = action.invoke(pipelineDir.toFile(), "1", false);
        assertThat(size, is(0l));

        assertThat(pipelineDir.toFile().exists(), is(true));
        assertThat(new File(pipelineDir.toFile().getAbsolutePath() + "/" + "1").exists(), is(false));
        assertThat(destinationDirectory.toFile().exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-1", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-1", "blah", "blah").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-2", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-2", "foo", "bar").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-3", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-3", "bar", "baz").exists(), is(true));
    }

    @Test
    public void shouldNotMoveWhenRunningOnDryMode() throws IOException {
        Path pipelineDir = Files.createTempDirectory("source");
        createFile(pipelineDir, "1", "stage-1", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-1", "blah", "blah");
        createFile(pipelineDir, "1", "stage-2", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-2", "foo", "bar");
        createFile(pipelineDir, "1", "stage-3", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-3", "bar", "baz");

        Path destinationDirectory = Files.createTempDirectory("destination");
        FileUtils.deleteDirectory(destinationDirectory.toFile());

        MoveAction action = new MoveAction(destinationDirectory.toFile());
        long size = action.invoke(pipelineDir.toFile(), "1", false);
        assertThat(size, is(0l));

        assertThat(pipelineDir.toFile().exists(), is(true));
        assertThat(new File(pipelineDir.toFile().getAbsolutePath() + "/" + "1").exists(), is(false));
    }

    public File path(File path, String... suffixes) {
        StringBuilder builder = new StringBuilder();
        for (String suffix : suffixes) {
            builder.append(suffix)
                    .append("/");
        }

        return new File(path.getAbsolutePath() + "/" + builder.toString());
    }


}