package in.ashwanthkumar.gocd.actions;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DeleteActionTest {
    @Test
    public void shouldIgnoreWhiteListedFilesOrDirectories() throws IOException {
        Path tempDirectory = Files.createTempDirectory("foo");
        String whiteListed1 = createFile(tempDirectory, "stage-1", "cruise-output", "console.log");
        String fileToDelete1 = createFile(tempDirectory, "stage-1", "blah", "blah");
        String whiteListed2 = createFile(tempDirectory, "stage-2", "cruise-output", "console.log");
        String fileToDelete2 = createFile(tempDirectory, "stage-2", "foo", "bar");
        String whiteListed3 = createFile(tempDirectory, "stage-3", "cruise-output", "console.log");
        String fileToDelete3 = createFile(tempDirectory, "stage-3", "bar", "baz");
        DeleteAction action = new DeleteAction("cruise-output");

        long size = action.invoke(tempDirectory.toFile(), false);
        assertThat(size, is(0l));

        assertThat(new File(fileToDelete1).exists(), is(false));
        assertThat(new File(fileToDelete2).exists(), is(false));
        assertThat(new File(fileToDelete3).exists(), is(false));

        assertThat(new File(whiteListed1).exists(), is(true));
        assertThat(new File(whiteListed2).exists(), is(true));
        assertThat(new File(whiteListed3).exists(), is(true));
    }

    private String createFile(Path tempDirectory, String... paths) throws IOException {
        String path = filePath(tempDirectory, paths);
        File file = new File(path);
        boolean mkdirs = file.getParentFile().mkdirs();
        assertThat(mkdirs, is(true));
        boolean newFileCreated = file.createNewFile();
        assertThat(newFileCreated, is(true));
        return path;
    }

    private String filePath(Path tempDirectory, String... paths) {
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            builder.append(path)
                    .append("/");

        }
        return tempDirectory.toFile().getAbsolutePath() + "/" + builder.toString();
    }


}