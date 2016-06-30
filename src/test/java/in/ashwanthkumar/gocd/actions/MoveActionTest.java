package in.ashwanthkumar.gocd.actions;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static in.ashwanthkumar.gocd.actions.ActionUtils.createFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MoveActionTest {

    private Path pipelineDir;
    private Path destinationDirectory;

    @Before
    public void before() throws IOException {
        pipelineDir = Files.createTempDirectory("source");
        destinationDirectory = Files.createTempDirectory("destination");
        FileUtils.deleteDirectory(destinationDirectory.toFile()); // we create dirs inside the MoveAction
    }

    @After
    public void after() throws IOException {
        FileUtils.deleteDirectory(destinationDirectory.toFile());
        FileUtils.deleteDirectory(pipelineDir.toFile());
    }

    @Test
    public void shouldMoveDirectFromSourceToDestination() throws IOException {
        String pipelineName = pipelineDir.toFile().getName();
        createFile(pipelineDir, "1", "stage-1", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-1", "blah", "blah");
        createFile(pipelineDir, "1", "stage-2", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-2", "foo", "bar");
        createFile(pipelineDir, "1", "stage-3", "cruise-output", "console.log");
        createFile(pipelineDir, "1", "stage-3", "bar", "baz");

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

    @Test
    public void shouldMoveProperlyEvenWhenTheStageWasReRan() throws IOException {
        // Run #1
        String pipelineName = pipelineDir.toFile().getName();
        createFile(pipelineDir, "1", "stage-1", "1", "cruise-output", "console.log");

        MoveAction action = new MoveAction(destinationDirectory.toFile());
        long size = action.invoke(pipelineDir.toFile(), "1", false);
        assertThat(size, is(0l));

        assertThat(pipelineDir.toFile().exists(), is(true));
        assertThat(path(pipelineDir.toFile(), "1").exists(), is(false));
        assertThat(destinationDirectory.toFile().exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-1", "1", "cruise-output", "console.log").exists(), is(true));

        // Run #2
        createFile(pipelineDir, "1", "stage-1", "2", "cruise-output", "console.log");
        size = action.invoke(pipelineDir.toFile(), "1", false);
        assertThat(size, is(0l));
        assertThat(pipelineDir.toFile().exists(), is(true));
        assertThat(path(pipelineDir.toFile(), "2").exists(), is(false));
        assertThat(destinationDirectory.toFile().exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-1", "1", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), pipelineName, "1", "stage-1", "2", "cruise-output", "console.log").exists(), is(true));
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