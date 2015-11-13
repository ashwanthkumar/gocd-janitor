package in.ashwanthkumar.gocd.actions;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static in.ashwanthkumar.gocd.actions.ActionUtils.createFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DeleteActionTest {
    @Test
    public void shouldIgnoreWhiteListedFilesOrDirectories() throws IOException {
        Path pipelineDir = Files.createTempDirectory("foo");
        String whiteListed1 = createFile(pipelineDir, "1", "stage-1", "cruise-output", "console.log");
        String fileToDelete1 = createFile(pipelineDir, "1", "stage-1", "blah", "blah");
        String whiteListed2 = createFile(pipelineDir, "1", "stage-2", "cruise-output", "console.log");
        String fileToDelete2 = createFile(pipelineDir, "1", "stage-2", "foo", "bar");
        String whiteListed3 = createFile(pipelineDir, "1", "stage-3", "cruise-output", "console.log");
        String fileToDelete3 = createFile(pipelineDir, "1", "stage-3", "bar", "baz");
        DeleteAction action = new DeleteAction("cruise-output");

        long size = action.invoke(pipelineDir.toFile(), "1", false);
        assertThat(size, is(0l));

        assertThat(new File(fileToDelete1).exists(), is(false));
        assertThat(new File(fileToDelete2).exists(), is(false));
        assertThat(new File(fileToDelete3).exists(), is(false));

        assertThat(new File(whiteListed1).exists(), is(true));
        assertThat(new File(whiteListed2).exists(), is(true));
        assertThat(new File(whiteListed3).exists(), is(true));
    }

    @Test
    public void shouldNotDeleteAnythingWhenRunningOnDryMode() throws IOException {
        Path tempDirectory = Files.createTempDirectory("foo");
        String whiteListed1 = createFile(tempDirectory, "1", "stage-1", "cruise-output", "console.log");
        String fileToDelete1 = createFile(tempDirectory, "1", "stage-1", "blah", "blah");
        String whiteListed2 = createFile(tempDirectory, "1", "stage-2", "cruise-output", "console.log");
        String fileToDelete2 = createFile(tempDirectory, "1", "stage-2", "foo", "bar");
        String whiteListed3 = createFile(tempDirectory, "1", "stage-3", "cruise-output", "console.log");
        String fileToDelete3 = createFile(tempDirectory, "1", "stage-3", "bar", "baz");
        DeleteAction action = new DeleteAction("cruise-output");

        long size = action.invoke(tempDirectory.toFile(), "1", true);
        assertThat(size, is(0l));

        assertThat(new File(fileToDelete1).exists(), is(true));
        assertThat(new File(fileToDelete2).exists(), is(true));
        assertThat(new File(fileToDelete3).exists(), is(true));

        assertThat(new File(whiteListed1).exists(), is(true));
        assertThat(new File(whiteListed2).exists(), is(true));
        assertThat(new File(whiteListed3).exists(), is(true));

    }


}