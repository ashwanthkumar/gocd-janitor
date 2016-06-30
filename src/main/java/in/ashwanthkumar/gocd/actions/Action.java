package in.ashwanthkumar.gocd.actions;

import java.io.File;

/**
 * Action to invoke when a pipeline is not white listed.
 * Instead of deleting we could move the artifacts to another directory - Useful especially doing it the first time.
 */
public interface Action {
    /**
     * @param pipelinePath   Path to pipelineDir
     * @param dryRun Should we just emulate this action?
     * @return The size of the directory that was processed.
     */
    long invoke(File pipelinePath, String versionPath, boolean dryRun);
}
