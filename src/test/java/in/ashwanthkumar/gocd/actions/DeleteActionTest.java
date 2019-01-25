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

    @Test
    public void emptyDirectoriesShouldBeDeleted() throws IOException {
        final Path pipelineDir = Files.createTempDirectory("pipelineDir");
        final File expectedDeletedFile4 = new File(createFile(pipelineDir, "2", "stage-4", "a", "x.txt"));
        assertThat(expectedDeletedFile4.exists(), is(true));
        assertThat(new File(createFile(pipelineDir, "2", "stage-4", "a", "b", "x.txt")).exists(), is(true));
        assertThat(new File(createFile(pipelineDir, "2", "stage-4", "a", "b", "c", "x.txt")).exists(), is(true));
        assertThat(new File(createFile(pipelineDir, "2", "stage-4", "a", "b", "d", "x.txt")).exists(), is(true));
        final File expectedDeletedFile5_a = new File(createFile(pipelineDir, "3", "stage-4", "a", "x.txt"));
        assertThat(expectedDeletedFile5_a.exists(), is(true));
        final File expectedDeletedFile5_a_b = new File(createFile(pipelineDir, "3", "stage-4", "a", "b", "x_whiteListed.txt"));
        assertThat(expectedDeletedFile5_a_b.exists(), is(true));
        final File expectedDeletedFile5_a_b_c = new File(createFile(pipelineDir, "3", "stage-4", "a", "b", "c", "x.txt"));
        assertThat(expectedDeletedFile5_a_b_c.exists(), is(true));
        final File expectedDeletedFile5_a_b_d = new File(createFile(pipelineDir, "3", "stage-4", "a", "b", "d", "x.txt"));
        assertThat(expectedDeletedFile5_a_b_d.exists(), is(true));

        new DeleteAction().invoke(pipelineDir.toFile(), "2", false);
        new DeleteAction("x_whiteListed.txt").invoke(pipelineDir.toFile(), "3", false);

        assertThat(expectedDeletedFile4.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile4.getParentFile().getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile4.getParentFile().getParentFile().getParentFile().exists(), is(false));

        assertThat(expectedDeletedFile5_a_b_c.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile5_a_b_d.getParentFile().exists(), is(false));
        assertThat(expectedDeletedFile5_a.exists(), is(false));
        assertThat("Directory was not deleted because it contains whitelisted file.", expectedDeletedFile5_a.getParentFile().exists(), is(true));
        assertThat("Whitelisted file cannot be deleted.", expectedDeletedFile5_a_b.exists(), is(true));
        assertThat("Directory was not deleted because it contains whitelisted file.", expectedDeletedFile5_a_b.getParentFile().exists(), is(true));
    }

}
