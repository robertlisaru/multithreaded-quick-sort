package com.lisaru.pqsort;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuickSortTest {
    @Test
    public void partitionOneElement() {
        int[] array = {5};
        int pos = QuickSort.partition(array, 0, 0);
        assertEquals("position 0 is returned", 0, pos);
    }

    @Test
    public void partitionTwoElements() {
        int[] array = {6, 5};
        int pos = QuickSort.partition(array, 0, 1);
        assertEquals("position 0 is returned", 0, pos);
        assertTrue("pivot is swapped into correct place", array[0] == 5 && array[1] == 6);
    }

    @Test
    public void partitionMoreElements() {
        int[] array = {30, 2, 5, 1, 3, 6, 8, 10, 21, 34, 33, 7, 7, 100};
        int pivot = QuickSort.choosePivot(array, 0, array.length - 1);
        int pos = QuickSort.partition(array, 0, array.length - 1);
        for (int i = 0; i <= pos; i++) {
            assertTrue("left side elements smaller than or equal to pivot", array[i] <= pivot);
        }
        for (int i = pos + 1; i < array.length; i++) {
            assertTrue("right side elements greater than or equal to pivot", array[i] >= pivot);
        }
    }

    @Test
    public void partitionRandomElements() {
        Random rand = new Random(System.currentTimeMillis());
        int n = rand.nextInt(100);
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = rand.nextInt(100);
        }
        int pivot = QuickSort.choosePivot(array, 0, array.length - 1);
        int pos = QuickSort.partition(array, 0, array.length - 1);
        for (int i = 0; i <= pos; i++) {
            assertTrue("left side elements smaller than or equal to pivot", array[i] <= pivot);
        }
        for (int i = pos + 1; i < array.length; i++) {
            assertTrue("right side elements greater than or equal to pivot", array[i] >= pivot);
        }
    }
}
