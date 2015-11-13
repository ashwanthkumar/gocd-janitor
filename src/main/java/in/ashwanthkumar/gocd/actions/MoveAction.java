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
    public long invoke(File pipelineDir, String version, boolean dryRun) {
        File versionDir = new File(pipelineDir.getAbsolutePath() + "/" + version);
        long size = FileUtils.sizeOfDirectory(versionDir);
        try {
            if (dryRun) {
                LOG.info("[DRY RUN] Will move " + pipelineDir.getAbsolutePath() + " to " + destination.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
            } else {
                LOG.info("Moving the directory from " + versionDir.getAbsolutePath() + " to " + destinationPath(pipelineDir));
                File destination = new File(destinationPath(pipelineDir));
                destination.mkdirs();
                FileUtils.moveDirectory(versionDir, new File(destination.getAbsolutePath() + "/" + version));
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return size;
    }

    private String destinationPath(File pipelineDir) {
        return destination.getAbsolutePath() + "/" + pipelineDir.getName();
    }
}
