package model;

/**
 *
 * @author dante
 */
// Represents the vowel count result of ONE file
// Each thread produces one of these, CounterManager collects them all
public class VowelResult {

    private final String fileName;  // Just the name, not the full path

    // Individual vowel counts
    private long countA;
    private long countE;
    private long countI;
    private long countO;
    private long countU;
    private long countAccented; // á, é, í, ó, ú, ü

    private long processingTime; // ms — how long this file took
    
    private Status status; //Status for files

    // Possible statuses
    public enum Status {
        PENDING,    // Not processed yet
        PROCESSING, // Currently being counted
        DONE,       // Finished successfully
        ERROR       // Something went wrong
    }

    public VowelResult(String fileName) {
        this.fileName = fileName;
        this.status = Status.PENDING; // Starts pending — not processed yet
    }

    // == Counting methods — called during processing ======================
    // Increment the correct counter based on the character found
    public void countVowel(char c) {
        switch (Character.toLowerCase(c)) {
            case 'a' ->
                countA++;
            case 'e' ->
                countE++;
            case 'i' ->
                countI++;
            case 'o' ->
                countO++;
            case 'u' ->
                countU++;
            // Accented vowels — both upper and lower handled by toLowerCase
            case 'á', 'à', 'ä' -> {
                countA++;
                countAccented++;
            }
            case 'é', 'è', 'ë' -> {
                countE++;
                countAccented++;
            }
            case 'í', 'ì', 'ï' -> {
                countI++;
                countAccented++;
            }
            case 'ó', 'ò', 'ö' -> {
                countO++;
                countAccented++;
            }
            case 'ú', 'ù', 'ü' -> {
                countU++;
                countAccented++;
            }
        }
    }

    // == Derived stats =============================================
    public long getTotal() {
        return countA + countE + countI + countO + countU;
        // Note: accented are already counted in their base vowel
    }

    // Returns which vowel appeared most — "A", "E", "I", "O", "U"
    public String getMostFrequent() {
        long max = Math.max(countA, Math.max(countE,
                Math.max(countI, Math.max(countO, countU))));
        if (max == 0) {
            return "-"; // No vowels found
        }
        if (max == countA) {
            return "A";
        }
        if (max == countE) {
            return "E";
        }
        if (max == countI) {
            return "I";
        }
        if (max == countO) {
            return "O";
        }
        return "U";
    }

    // == Getters ======================================
    public String getFileName() {
        return fileName;
    }

    public long getCountA() {
        return countA;
    }

    public long getCountE() {
        return countE;
    }

    public long getCountI() {
        return countI;
    }

    public long getCountO() {
        return countO;
    }

    public long getCountU() {
        return countU;
    }

    public long getCountAccented() {
        return countAccented;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public Status getStatus() {
        return status;
    }

    // == Setters — called by counters ==============================
    public void setProcessingTime(long ms) {
        this.processingTime = ms;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // For console debugging — minimal printing (shared resource!)
    @Override
    public String toString() {
        return fileName + " -> " + getTotal() + " vowels in " + processingTime + "ms";
    }
}
