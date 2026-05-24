package model;

/**
 *
 * @author dante
 */
// Application status — shown in the north label
// Different from VowelResult.Status (that's per file)
public enum AppStatus {
    IDLE, // No files loaded or waiting
    RUNNING_SEQUENTIAL, // Sequential processing active
    RUNNING_CONCURRENT, // Concurrent processing active
    DONE                     // Processing finished
}
