package com.lisaru.pqsort;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MinHeapTest {
    @Test
    public void createHeapElementAndPopFirst() {
        int[] sortedArray = {1, 2, 3, 4, 5};
        HeapElement heapElement = new HeapElement(sortedArray);
        int key = heapElement.key();
        assertEquals(1, key);
        int min = heapElement.popFirst();
        assertEquals(1, min);
        key = heapElement.key();
        assertEquals(2, key);
    }

    @Test
    public void heapElementWithOneNumberArray() {
        int[] sortedArray = {1};
        HeapElement heapElement = new HeapElement(sortedArray);
        int key = heapElement.key();
        assertEquals(1, key);
        int min = heapElement.popFirst();
        assertEquals(1, min);
        key = heapElement.key();
        assertEquals(Integer.MAX_VALUE, key);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void poppingPastLastElementThrowsException() {
        int[] sortedArray = {1};
        HeapElement heapElement = new HeapElement(sortedArray);
        heapElement.popFirst();
        heapElement.popFirst();
    }

    @Test
    public void createHeapFromHeapElement() {
        int[] sortedArray = {1, 2};
        HeapElement[] heapElements = new HeapElement[1];
        heapElements[0] = new HeapElement(sortedArray);
        MinHeap minHeap = new MinHeap(heapElements);
        assertEquals(1, minHeap.popMin());
        assertEquals(2, minHeap.popMin());
    }

    @Test
    public void createHeapFromTwoHeapElements() {
        HeapElement[] heapElements = new HeapElement[2];
        int[] sortedArray1 = {2, 4};
        int[] sortedArray2 = {1, 3};
        heapElements[0] = new HeapElement(sortedArray1);
        heapElements[1] = new HeapElement(sortedArray2);
        MinHeap minHeap = new MinHeap(heapElements);
        assertEquals(1, minHeap.popMin());
        assertEquals(2, minHeap.popMin());
        assertEquals(3, minHeap.popMin());
        assertEquals(4, minHeap.popMin());
    }

    @Test
    public void createHeapFromMoreHeapElements() {
        HeapElement[] heapElements = new HeapElement[5];
        int[][] sortedArrays = {
                {2, 4},
                {1, 3},
                {6, 8},
                {5, 7},
                {9, 10}
        };
        for (int i = 0; i < 5; i++) {
            heapElements[i] = new HeapElement(sortedArrays[i]);
        }
        MinHeap minHeap = new MinHeap(heapElements);
        assertEquals(1, minHeap.popMin());
        assertEquals(2, minHeap.popMin());
        assertEquals(3, minHeap.popMin());
        assertEquals(4, minHeap.popMin());
        assertEquals(5, minHeap.popMin());
        assertEquals(6, minHeap.popMin());
        assertEquals(7, minHeap.popMin());
        assertEquals(8, minHeap.popMin());
        assertEquals(9, minHeap.popMin());
        assertEquals(10, minHeap.popMin());
    }
}
