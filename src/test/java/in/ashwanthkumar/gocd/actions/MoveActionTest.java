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
        Path sourceDirectory = Files.createTempDirectory("source");
        createFile(sourceDirectory, "stage-1", "cruise-output", "console.log");
        createFile(sourceDirectory, "stage-1", "blah", "blah");
        createFile(sourceDirectory, "stage-2", "cruise-output", "console.log");
        createFile(sourceDirectory, "stage-2", "foo", "bar");
        createFile(sourceDirectory, "stage-3", "cruise-output", "console.log");
        createFile(sourceDirectory, "stage-3", "bar", "baz");

        Path destinationDirectory = Files.createTempDirectory("destination");
        FileUtils.deleteDirectory(destinationDirectory.toFile());

        MoveAction action = new MoveAction(destinationDirectory.toFile());
        long size = action.invoke(sourceDirectory.toFile(), false);
        assertThat(size, is(0l));

        assertThat(sourceDirectory.toFile().exists(), is(false));
        assertThat(destinationDirectory.toFile().exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), "stage-1", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), "stage-1", "blah", "blah").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), "stage-2", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), "stage-2", "foo", "bar").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), "stage-3", "cruise-output", "console.log").exists(), is(true));
        assertThat(path(destinationDirectory.toFile(), "stage-3", "bar", "baz").exists(), is(true));
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