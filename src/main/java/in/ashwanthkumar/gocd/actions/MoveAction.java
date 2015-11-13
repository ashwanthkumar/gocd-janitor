package in.ashwanthkumar.gocd.actions;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MoveAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(MoveAction.class);

    private File destination;

    public MoveAction(File destination) {
        this.destination = destination;
    }

    @Override
    public long invoke(File path, boolean dryRun) {
        long size = FileUtils.sizeOfDirectory(path);
        try {
            if (dryRun) {
                LOG.info("[DRY RUN] Will move " + path.getAbsolutePath() + " to " + destination.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
            } else {
                LOG.info("Moving the directory from " + path.getAbsolutePath() + " to " + destination.getAbsolutePath());
                FileUtils.moveDirectory(path, destination);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return size;
    }
}
