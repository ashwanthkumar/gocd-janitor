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

    private Set<String> whiteList = new HashSet<>();

    public DeleteAction(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    public DeleteAction(String... paths) {
        this.whiteList.addAll(Arrays.asList(paths));
    }

    @Override
    public long invoke(File pipelineDir, String version, boolean dryRun) {
        File versionDir = new File(pipelineDir.getAbsolutePath() + "/" + version);
        long size = sizeOfDirectory(versionDir);

        if (size > 0) {
            String path = versionDir.getAbsolutePath();
            String displaySize = FileUtils.byteCountToDisplaySize(size);

            if (dryRun) {
                LOG.info("[DRY RUN] Will remove " + path + ", size = " + displaySize);
            }
            else {
                LOG.info("Deleting " + path + ", size = " + displaySize);

                try {
                    deleteDirectory(versionDir);
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        return size;
    }

    private long sizeOfDirectory(File directory) {
        long size = 0;

        File[] files = directory.listFiles();

        if(files != null) {
            for (File file : files) {
                try {
                    if (!FileUtils.isSymlink(file) && isNotWhiteListed(file)) {
                        size += file.isDirectory() ? sizeOfDirectory(file) : file.length();
                    }
                }
                catch (IOException ignored) {}
            }
        }

        return size;
    }

    private void deleteDirectory(File path) throws IOException {
        if (isNotWhiteListed(path)) {
            File[] files = path.listFiles();
            if (files == null) throw new IOException("Couldn't list files inside " + path.getAbsolutePath());
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
