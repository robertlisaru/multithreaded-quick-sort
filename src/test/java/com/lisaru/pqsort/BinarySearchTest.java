package com.lisaru.pqsort;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BinarySearchTest {
    @Test
    public void searchIntoOneElement() {
        int[] sortedArray = {5};
        int pos = BinarySearch.search(sortedArray, 0, 0, 6);
        assertEquals(1, pos);
    }

    @Test
    public void searchIntoTwoElements() {
        int[] sortedArray = {5, 6};
        int pos = BinarySearch.search(sortedArray, 0, 1, 6);
        assertEquals(2, pos);
    }

    @Test
    public void searchInto3Elements() {
        int[] sortedArray = {5, 6, 7};
        int pos = BinarySearch.search(sortedArray, 0, 2, 6);
        assertEquals(2, pos);
    }

    @Test
    public void searchInto3ElementsLowerThanPivot() {
        int[] sortedArray = {3, 4, 5};
        int pos = BinarySearch.search(sortedArray, 0, 2, 6);
        assertEquals(3, pos);
    }

    @Test
    public void searchInto3ElementsEqualToPivot() {
        int[] sortedArray = {6, 6, 6};
        int pos = BinarySearch.search(sortedArray, 0, 2, 6);
        assertEquals(3, pos);
    }

    @Test
    public void searchInto3IdenticalElementsGreaterThanPivot() {
        int[] sortedArray = {7, 8, 9};
        int pos = BinarySearch.search(sortedArray, 0, 2, 6);
        assertEquals(0, pos);
    }

    @Test
    public void searchIntoMoreElements() {
        int[] sortedArray = {1, 2, 3, 4, 5, 8, 9, 10};
        int pos = BinarySearch.search(sortedArray, 0, sortedArray.length - 1, 6);
        assertEquals(5, pos);
        pos = BinarySearch.search(sortedArray, 0, sortedArray.length - 1, 0);
        assertEquals(0, pos);
        pos = BinarySearch.search(sortedArray, 0, sortedArray.length - 1, 1);
        assertEquals(1, pos);
        pos = BinarySearch.search(sortedArray, 0, sortedArray.length - 1, 10);
        assertEquals(8, pos);
    }
}
