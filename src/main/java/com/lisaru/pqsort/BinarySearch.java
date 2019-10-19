package com.lisaru.pqsort;

public class BinarySearch {

    public static int search(int[] sortedArray, int start, int end, int pivot) {
        if (start == end) {
            if (sortedArray[end] <= pivot) {
                return end + 1;
            }
            return end;
        }
        int mid = (start + end) / 2;
        if (pivot < sortedArray[mid]) {
            return search(sortedArray, start, mid, pivot);
        } else {
            return search(sortedArray, mid + 1, end, pivot);
        }
    }
}
