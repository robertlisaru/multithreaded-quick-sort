package com.lisaru.pqsort;

public class QuickSort {
    public static void sort(int[] array, int start, int end) {
        if (start < end) {
            int pos = partition(array, start, end);
            sort(array, start, pos);
            sort(array, pos + 1, end);
        }
    }

    static int partition(int[] array, int start, int end) {
        int pivot = choosePivot(array, start, end);
        int i = start - 1;
        int j = end + 1;
        while (true) {
            do {
                i++;
            } while (array[i] < pivot);
            do {
                j--;
            } while (array[j] > pivot);
            if (i >= j) {
                return j;
            }
            swap(array, i, j);
        }
    }

    static int choosePivot(int[] array, int start, int end) {
        return array[start];
    }

    private static void swap(int[] array, int pos1, int pos2) {
        int aux = array[pos1];
        array[pos1] = array[pos2];
        array[pos2] = aux;
    }
}
