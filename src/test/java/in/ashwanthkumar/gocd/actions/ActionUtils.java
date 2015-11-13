package in.ashwanthkumar.gocd.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ActionUtils {
    public static String createFile(Path tempDirectory, String... paths) throws IOException {
        String path = filePath(tempDirectory, paths);
        File file = new File(path);
        boolean mkdirs = file.getParentFile().mkdirs();
        assertThat(mkdirs, is(true));
        boolean newFileCreated = file.createNewFile();
        assertThat(newFileCreated, is(true));
        return path;
    }

    public static String filePath(Path tempDirectory, String... paths) {
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            builder.append(path)
                    .append("/");

        }
        return tempDirectory.toFile().getAbsolutePath() + "/" + builder.toString();
    }

}
