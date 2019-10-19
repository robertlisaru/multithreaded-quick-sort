package com.lisaru.pqsort;

public class HeapElement {
    private int[] sortedArray;
    private int headPosition = 0;
    private int length;

    public HeapElement(int[] sortedArray) {
        this.sortedArray = sortedArray;
        length = sortedArray.length;
    }

    public int key() {
        return headPosition < length ? sortedArray[headPosition] : Integer.MAX_VALUE;
    }

    public int popFirst() {
        return sortedArray[headPosition++];
    }
}
