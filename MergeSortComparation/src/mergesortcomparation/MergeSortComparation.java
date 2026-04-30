package mergesortcomparation;

/**
 *
 * @author dante
 */
public class MergeSortComparation {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int[] sizes = {100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000};

        for (int size : sizes) {
            int[] a = generateRandom(size);
            int[] b = a.clone();

            
            System.out.println("\n--- Size: " + size + " ---");
            
            System.out.println("Secuential: ");
            MergeSort mergesort = new MergeSort();
            mergesort.mergeSort(a);
            
            System.out.println("/----------------------------------------------------------/");
            
            MergeSortForkJoin mergesortfork = new MergeSortForkJoin();

            System.out.println("Concurrent: ");

            mergesortfork.sort(b);
            
        }
    }

    private static int[] generateRandom(int size) {
        int[] arr = new int[size];
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(200_001) - 100_000;
        }
        return arr;
    }

}
