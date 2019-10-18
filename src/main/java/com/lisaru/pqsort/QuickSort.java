package com.lisaru.pqsort;

public class QuickSort {
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

    private static void swap(int[] array, int pos1, int pos2) {
        int aux = array[pos1];
        array[pos1] = array[pos2];
        array[pos2] = aux;
    }

    static int choosePivot(int[] array, int start, int end) {
        return array[start];
    }
}
