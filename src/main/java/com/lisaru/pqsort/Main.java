package com.lisaru.pqsort;

import mpi.MPI;

import java.util.Random;

public class Main {
    public static int[] generateRandomArray(int arraySize, int bound) {
        int[] array = new int[arraySize];
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < arraySize; i++) {
            array[i] = random.nextInt(bound);
        }
        return array;
    }

    public static int[] generateTextbookExampleArray() {
        int[] array = {
                15, 46, 48, 93, 39, 6, 72, 91, 14,
                36, 69, 40, 89, 61, 97, 12, 21, 54,
                53, 97, 84, 58, 32, 27, 33, 72, 20
        };
        return array;
    }

    public static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        MPI.Init(args);
        int worldRank = MPI.COMM_WORLD.Rank();
        int worldSize = MPI.COMM_WORLD.Size();

        int debugRank = 2;

        int[] array = null;
        int[] localArraySizes = new int[worldSize];
        int[] localArrayOffsets = null;

        //region generate random array and scatter into local arrays
        if (worldRank == 0) {
            array = generateRandomArray(100, 1000);
            System.out.println("Initial array: ");
            printArray(array);
            if (worldSize == 1) {
                QuickSort.sort(array, 0, array.length - 1);
                MPI.Finalize();
                return;
            }
            int div = array.length / worldSize;
            int remainder = array.length % worldSize;
            for (int i = 0; i < worldSize; i++) {
                localArraySizes[i] = div + (i < remainder ? 1 : 0);
            }
            localArrayOffsets = new int[worldSize];
            localArrayOffsets[0] = 0;
            for (int i = 1; i < worldSize; i++) {
                localArrayOffsets[i] = localArrayOffsets[i - 1] +
                        localArraySizes[i - 1];
            }
        }

        int[] localArraySizeRecvBuf = new int[1];
        MPI.COMM_WORLD.Scatter(localArraySizes, 0, 1, MPI.INT,
                localArraySizeRecvBuf, 0, 1, MPI.INT, 0);
        int localArraySize = localArraySizeRecvBuf[0];
        int[] localArray = new int[localArraySize];
        MPI.COMM_WORLD.Scatterv(array, 0, localArraySizes, localArrayOffsets, MPI.INT,
                localArray, 0, localArraySize, MPI.INT, 0);

        //endregion

        if (worldRank == debugRank) {
            System.out.println("Local array: ");
            printArray(localArray);
        }
        QuickSort.sort(localArray, 0, localArraySize - 1);
        if (worldRank == debugRank) {
            System.out.println("Sorted Local array: ");
            printArray(localArray);
        }

        //region take samples from the sorted local array and gather them in P0
        int samplingStride = localArraySize / worldSize;
        int[] localSamples = new int[worldSize];
        for (int i = 0; i < worldSize; i++) {
            localSamples[i] = localArray[i * samplingStride];
        }
        int[] gatheredSamples = new int[worldSize * worldSize];
        MPI.COMM_WORLD.Gather(localSamples, 0, worldSize, MPI.INT,
                gatheredSamples, 0, worldSize, MPI.INT, 0);
        //endregion

        if (worldRank == debugRank) {
            System.out.println("My samples array: ");
            printArray(localSamples);
        }

        //region P0: sort samples, then choose and broadcast pivots
        if (worldRank == 0) {
            QuickSort.sort(gatheredSamples, 0, worldSize * worldSize - 1);
        }
        int[] pivots = new int[worldSize - 1];
        if (worldRank == 0) {
            for (int i = 0; i < worldSize - 1; i++) {
                pivots[i] = gatheredSamples[(i + 1) * worldSize];
            }
        }
        MPI.COMM_WORLD.Bcast(pivots, 0, worldSize - 1, MPI.INT, 0);
        //endregion

        if (worldRank == debugRank) {
            System.out.println("Pivots: ");
            printArray(pivots);
        }

        //region partition sorted local array using pivots
        int[] partitionOffsets = new int[worldSize];
        partitionOffsets[0] = 0;
        for (int i = 0; i < pivots.length; i++) {
            partitionOffsets[i + 1] = BinarySearch.search(localArray, 0, localArraySize - 1, pivots[i]);
        }
        int[] partitionSizes = new int[worldSize];
        for (int i = 0; i < worldSize - 1; i++) {
            partitionSizes[i] = partitionOffsets[i + 1] - partitionOffsets[i];
        }
        partitionSizes[worldSize - 1] = localArraySize - partitionOffsets[worldSize - 1];
        //endregion

        if (worldRank == debugRank) {
            System.out.println("Partition sizes: ");
            printArray(partitionSizes);
        }

        if (worldRank == debugRank) {
            System.out.println("Partition offsets: ");
            printArray(partitionOffsets);
        }

        //region gather partitions into corresponding processes
        int[] gatheredPartitionSizes = new int[worldSize];
        MPI.COMM_WORLD.Alltoall(partitionSizes, 0, 1, MPI.INT,
                gatheredPartitionSizes, 0, 1, MPI.INT);

        if (worldRank == debugRank) {
            System.out.println("Gathered partition sizes: ");
            printArray(gatheredPartitionSizes);
        }

        int[] gatheredPartitionOffsets = new int[worldSize];
        int gatheredPartitionSizeTotal = 0;
        for (int i = 0; i < worldSize; i++) {
            gatheredPartitionOffsets[i] = gatheredPartitionSizeTotal;
            gatheredPartitionSizeTotal += gatheredPartitionSizes[i];
        }
        int[] gatheredPartitions = new int[gatheredPartitionSizeTotal];
        MPI.COMM_WORLD.Alltoallv(localArray, 0, partitionSizes, partitionOffsets, MPI.INT,
                gatheredPartitions, 0, gatheredPartitionSizes, gatheredPartitionOffsets, MPI.INT);
        //endregion

        if (worldRank == debugRank) {
            System.out.println("Gathered partitions: ");
            printArray(gatheredPartitions);
        }

        //region merge the already sorted partitions using a min-heap
        HeapElement[] heapElements = new HeapElement[worldSize];
        for (int i = 0; i < worldSize - 1; i++) {
            heapElements[i] = new HeapElement(gatheredPartitions);
            heapElements[i].setStartPosition(gatheredPartitionOffsets[i]);
            heapElements[i].setEndPosition(gatheredPartitionOffsets[i + 1]);
        }
        heapElements[worldSize - 1] = new HeapElement(gatheredPartitions);
        heapElements[worldSize - 1].setStartPosition(gatheredPartitionOffsets[worldSize - 1]);
        heapElements[worldSize - 1].setEndPosition(gatheredPartitionSizeTotal);

        MinHeap minHeap = new MinHeap(heapElements);
        int[] mergeResult = new int[gatheredPartitionSizeTotal];
        for (int i = 0; i < gatheredPartitionSizeTotal; i++) {
            mergeResult[i] = minHeap.popMin();
        }

        if (worldRank == debugRank) {
            System.out.println("Merged partitions: ");
            printArray(mergeResult);
        }
        //endregion

        // region concatenate results back into root
        int[] mergeResultSizes = new int[worldSize];
        int[] mergeResultOffsets = new int[worldSize];

        MPI.COMM_WORLD.Gather(new int[]{mergeResult.length}, 0, 1, MPI.INT,
                mergeResultSizes, 0, 1, MPI.INT, 0);

        if (worldRank == 0) {
            mergeResultOffsets = new int[worldSize];
            mergeResultOffsets[0] = 0;
            for (int i = 1; i < worldSize; i++) {
                mergeResultOffsets[i] = mergeResultOffsets[i - 1] + mergeResultSizes[i - 1];
            }
        }

        MPI.COMM_WORLD.Gatherv(mergeResult, 0, mergeResult.length, MPI.INT,
                array, 0, mergeResultSizes, mergeResultOffsets, MPI.INT, 0);
        //endregion

        if (worldRank == 0) {
            System.out.println("Sorted array: ");
            printArray(array);
        }

        MPI.Finalize();
    }
}