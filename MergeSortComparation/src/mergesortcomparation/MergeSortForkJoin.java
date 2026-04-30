package mergesortcomparation;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MergeSortForkJoin {

    public void sort(int[] a) {
        long start = TimeHelper.startTimer();
        int[] helper = new int[a.length];
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new MergeSortTask(a, helper, 0, a.length - 1));
        System.out.println("Sorted array (Parallel ForkJoin):");
        printArray(a);
        long time = TimeHelper.showTime(start, "Parallel ForkJoin");
        TimeHelper.showEfficiency(time);
    }

    private static void printArray(int[] a) {
        System.out.print("[");
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    private class MergeSortTask extends RecursiveAction {

        private static final long serialVersionUID = -749935388568367268L;
        private final int[] a;
        private final int[] helper;
        private final int lo;
        private final int hi;

        public MergeSortTask(int[] a, int[] helper, int lo, int hi) {
            this.a = a;
            this.helper = helper;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected void compute() {
            if (lo < hi) {
                int mid = lo + (hi - lo) / 2;
                MergeSortTask left = new MergeSortTask(a, helper, lo, mid);
                MergeSortTask right = new MergeSortTask(a, helper, mid + 1, hi);
                invokeAll(left, right);
                merge(this.a, this.helper, this.lo, mid, this.hi);
            } else {
                return;
            }
        }

        private void merge(int[] a, int[] helper, int lo, int mid, int hi) {
            for (int k = lo; k <= hi; k++) {
                helper[k] = a[k];
            }

            int i = lo;
            int j = mid + 1;
            int k = lo;

            while (i <= mid && j <= hi) {
                if (helper[i] <= helper[j]) {
                    a[k++] = helper[i++];
                } else {
                    a[k++] = helper[j++];
                }
            }

            while (i <= mid) {
                a[k++] = helper[i++];
            }
        }
    }
}
