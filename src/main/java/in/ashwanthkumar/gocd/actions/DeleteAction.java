package in.ashwanthkumar.gocd.actions;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Most of the methods are copied from FileUtils class of Apache Commons IO.
 */
public class DeleteAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteAction.class);

    Set<String> whiteList = new HashSet<>();

    public DeleteAction(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    public DeleteAction(String... paths) {
        this.whiteList.addAll(Arrays.asList(paths));
    }

    @Override
    public long invoke(File path, boolean dryRun) {
        long size = FileUtils.sizeOfDirectory(path);
        if (dryRun) {
            LOG.info("[DRY RUN] Will remove " + path.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
        } else {
            LOG.info("Deleting " + path.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
            try {
                deleteDirectory(path);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        return size;
    }

    void deleteDirectory(File path) throws IOException {
        if (isNotWhiteListed(path)) {
            File[] files = path.listFiles();
            if(files == null) throw new IOException("Couldn't list files inside " + path.getAbsolutePath());
            for (File file : files) {
                if (isNotWhiteListed(file)) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            throw new IOException("Could not delete " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private boolean isNotWhiteListed(File path) {
        return !this.whiteList.contains(path.getName());
    }

}
