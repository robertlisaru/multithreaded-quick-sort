package com.lisaru.pqsort;

public class MinHeap {
    private HeapElement[] heapElements;
    private int size;

    public MinHeap(HeapElement[] heapElements) {
        this.heapElements = heapElements;
        size = heapElements.length;
        if (size > 1) {
            for (int i = size / 2 - 1; i >= 0; i--) {
                bubbleDown(i);
            }
        }
    }

    private void bubbleDown(int pos) {
        int left, right, minChild;
        while (pos < size / 2) { //while not a leaf
            left = pos * 2 + 1;
            right = pos * 2 + 2;
            minChild = left;
            if (right < size && heapElements[right].key() < heapElements[left].key()) {
                minChild = right;
            }
            if (heapElements[pos].key() > heapElements[minChild].key()) {
                HeapElement aux = heapElements[pos];
                heapElements[pos] = heapElements[minChild];
                heapElements[minChild] = aux;
                pos = minChild;
            } else {
                return;
            }
        }
    }

    public int popMin() {
        int min = heapElements[0].popFirst();
        bubbleDown(0);
        return min;
    }
}
