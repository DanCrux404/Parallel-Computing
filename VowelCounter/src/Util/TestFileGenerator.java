package Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author dante
 */

// Generates random text files for testing
// Words are randomly generated alternating consonants and vowels
// They look like real words even though they are nonsense, perfect for vowel counting UwU
public class TestFileGenerator {

    private static final Random random = new Random();
    //Hope doesn't crash with the ñ
    private static final char[] CONSONANTS = "bcdfghjklmnñpqrstvwxyz".toCharArray();
    // Includes accented vowels for a more complete test
    private static final char[] VOWELS = "aeiouáéíóúü".toCharArray();

    // == Generate N files of approximately targetSizeKB each =========
    public static void generateFiles(File outputDir, int fileCount,
            int targetSizeKB) throws IOException {
        // Create output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        //creating files
        for (int i = 1; i <= fileCount; i++) {
            File file = new File(outputDir, "test_" + i + ".txt");
            generateFile(file, targetSizeKB);
            System.out.println("Generated: " + file.getName()
                    + " (" + targetSizeKB + "KB)");
        }
        System.out.println("Done! " + fileCount + " files in: "
                + outputDir.getAbsolutePath());
    }

    // == Generate a single file of approximately targetSizeKB ==========
    private static void generateFile(File file, int targetSizeKB)
            throws IOException {
        int targetBytes = targetSizeKB * 1024;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int bytesWritten = 0;

            while (bytesWritten < targetBytes) {
                // Write a line of 10-20 random words
                StringBuilder line = new StringBuilder();
                int wordsInLine = 10 + random.nextInt(10);

                for (int w = 0; w < wordsInLine; w++) {
                    line.append(generateWord());
                    if (w < wordsInLine - 1) {
                        line.append(" ");
                    }
                }
                line.append("\n");

                writer.write(line.toString());
                bytesWritten += line.length();
            }
        }
    }

    // == Generates a "word" of 3-8 letters ========================
    // Alternates consonants and vowels — looks like a real word
    private static String generateWord() {
        StringBuilder word = new StringBuilder();
        int length = 3 + random.nextInt(6); // 3 to 8 letters

        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) {
                // Even position → consonant
                word.append(CONSONANTS[random.nextInt(CONSONANTS.length)]);
            } else {
                // Odd position → vowel — this is what we are counting!!!!!
                word.append(VOWELS[random.nextInt(VOWELS.length)]);
            }
        }
        return word.toString();
    }

    // == Quick runner — generates files in a test_files folder ===================
    // Run this once before testing the application
    public static void main(String[] args) throws IOException {
        File outputDir = new File("/media/storage/Documents/TestFiles");

        //Small test — 5 files of 100KB
        //generateFiles(outputDir, 5, 100);
        // Medium test — 100 files of 500KB
        //generateFiles(outputDir, 100, 500);
        // Heavy test — 10,000 files of 1MB each — uncomment for stress test
        generateFiles(outputDir, 10_000, 1000);
        // EXTREME test — 1,000,000 files MUEJEJEJEJE and 10MB
        //generateFiles(outputDir, 1_000_000, 10_000);
    }
}
