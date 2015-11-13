package in.ashwanthkumar.gocd.actions;

import java.io.File;

/**
 * Action to invoke when a pipeline is not whitelisted.
 * Intead of deleting we could move the artifaccts to another directory - Useful espcially doing it the first time.
 */
public interface Action {
    /**
     * @param path   Path to the version directory of the pipeline
     * @param dryRun Should we just emulate this action?
     * @return The size of the directory that was processed.
     */
    long invoke(File path, boolean dryRun);
}
