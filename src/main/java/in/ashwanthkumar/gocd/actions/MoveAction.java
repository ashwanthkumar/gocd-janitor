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
        long size;
        try {
            if (dryRun) {
                size = FileUtils.sizeOfDirectory(versionDir);
                LOG.info("[DRY RUN] Will move " + pipelineDir.getAbsolutePath() + " to " + destination.getAbsolutePath() + ", size = " + FileUtils.byteCountToDisplaySize(size));
            } else {
                size = 0l;
                LOG.info("Moving the directory from " + versionDir.getAbsolutePath() + " to " + destinationPath(pipelineDir));
                File baseDestination = new File(destinationPath(pipelineDir) + "/" + version);
                baseDestination.mkdirs();
                File[] stages = versionDir.listFiles();
                if (stages == null) {
                    LOG.warn("[Move Action] {} doesn't seem to have any stage dirs", versionDir.getAbsolutePath());
                    return 0;
                }
                /*
                    Folder structure of the artifacts are
                        <Pipeline-Name>/<Pipeline-Run>/<Stage-Name>/<Stage-Run-Counter>/...

                    /data/go-server/artifacts/pipelines/gocd-janitor
                    ├── 1
                    │   └── cleanup
                    │       └── 1
                    │           └── cleanup
                    │               └── cruise-output
                    │                   └── console.log
                    ├── 2
                    │   └── cleanup
                    │       ├── 1
                    │       │   └── cleanup
                    │       │       └── cruise-output
                    │       │           └── console.log
                    │       └── 2
                    │           └── cleanup
                    │               └── cruise-output
                    │                   └── console.log
                    └── 3
                        └── cleanup
                            └── 1
                                └── cleanup
                                    └── cruise-output
                                        └── console.log

                    7 directories, 4 files
                 */
                for (File stageDir : stages) {
                    File[] runs = stageDir.listFiles();
                    if (runs == null) {
                        LOG.warn("[Move Action] {} doesn't seem to have any stage run dirs", stageDir.getAbsolutePath());
                    } else {
                        for (File runDir : runs) {
                            size += FileUtils.sizeOf(runDir);
                            FileUtils.moveDirectory(runDir, new File(baseDestination.getAbsolutePath() + "/" + stageDir.getName() + "/" + runDir.getName()));
                        }
                    }
                    FileUtils.deleteDirectory(stageDir);
                }
                FileUtils.deleteDirectory(versionDir);
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
