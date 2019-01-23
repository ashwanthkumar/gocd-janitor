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
        String fileToDelete4 = createFile(pipelineDir, "2", "stage-4", "a", "b");
        DeleteAction action = new DeleteAction("cruise-output");

        final long size1 = action.invoke(pipelineDir.toFile(), "1", false);
        assertThat(size1, is(0l));
        final long size2 = action.invoke(pipelineDir.toFile(), "2", false);
        assertThat(size2, is(0l));

        final File expectedDeletedFile1 = new File(fileToDelete1);
        assertThat(expectedDeletedFile1.exists(), is(false));
        assertThat(expectedDeletedFile1.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile1.getParentFile().getParentFile().exists(), is(true));

        final File expectedDeletedFile2 = new File(fileToDelete2);
        assertThat(expectedDeletedFile2.exists(), is(false));
        assertThat(expectedDeletedFile2.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile2.getParentFile().getParentFile().exists(), is(true));

        final File expectedDeletedFile3 = new File(fileToDelete3);
        assertThat(expectedDeletedFile3.exists(), is(false));
        assertThat(expectedDeletedFile3.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile3.getParentFile().getParentFile().exists(), is(true));

        final File expectedDeletedFile4 = new File(fileToDelete4);
        assertThat(expectedDeletedFile4.exists(), is(false));
        assertThat(expectedDeletedFile4.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile4.getParentFile().getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile4.getParentFile().getParentFile().getParentFile().exists(), is(true));

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