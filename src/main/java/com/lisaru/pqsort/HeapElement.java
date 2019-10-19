package com.lisaru.pqsort;

public class HeapElement {
    private int[] sortedArray;
    private int startPosition = 0;
    private int endPosition;

    public HeapElement(int[] sortedArray) {
        this.sortedArray = sortedArray;
        endPosition = sortedArray.length;
    }

    public void setStartPosition(int offset) {
        startPosition = offset;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public int key() {
        return startPosition < endPosition ? sortedArray[startPosition] : Integer.MAX_VALUE;
    }

    public int popFirst() {
        return sortedArray[startPosition++];
    }
}
