#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <mpi.h>

int *generateRandomArray(int length, int min_value_inclusive, int max_value_exclusive) {
    srand(time(0));
    int *array = new int[length];
    for (int i = 0; i < length; i++) {
        array[i] = min_value_inclusive + rand() % (max_value_exclusive - min_value_inclusive);
    }
    return array;
}

void printArray(int *array, int length) {
    for (int i = 0; i < length; i++) {
        printf("%d ", array[i]);
    }
    printf("\n");
}

int partition(int *array, int start, int end) {
    int pivot = array[start];
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
        int aux = array[i];
        array[i] = array[j];
        array[j] = aux;
    }
}

void quickSort(int *array, int start, int end) {
    if (start < end) {
        int pivot_index = partition(array, start, end);
        quickSort(array, start, pivot_index);
        quickSort(array, pivot_index + 1, end);
    }
}

/**
 * A wrapper for a sorted list.
 * Represents nodes of a min-heap used to merge sorted lists.
 * The key of the node is the first element in the sorted list.
 */
class HeapNode {
private:
    int *list;
    int listSize;
    int remainingElements;

public:
    int key() {
        return remainingElements > 0 ? list[listSize - remainingElements] : INT_MAX;
    }

    int popFirst() {
        int firstElement = key();
        remainingElements--;
        return firstElement;
    }

    HeapNode(int *sortedList, int sortedListSize) {
        this->list = sortedList;
        this->listSize = sortedListSize;
        this->remainingElements = sortedListSize;
    }
};

/**
 * A min-heap used for merging more sorted lists together.
 * Each node represents a sorted list with the first element in the list as key.
 */
class Heap {
private:
    HeapNode **nodes; //an array of references to nodes
    int size = 0;

public:
    explicit Heap(HeapNode **nodes, int size) {
        this->nodes = nodes;
        this->size = size;
        for (int i = size / 2 - 1; i >= 0; i--) {
            bubbleDown(i);
        }
    }

    void bubbleDown(int pos) {
        int left, right, minChild;
        while (pos <= (size - 2) / 2) { //while not a leaf
            left = pos * 2 + 1;
            right = pos * 2 + 2;
            minChild = left;
            if (right < size && nodes[right]->key() < nodes[left]->key()) {
                minChild = right;
            }
            if (nodes[pos]->key() > nodes[minChild]->key()) {
                HeapNode *aux = nodes[pos];
                nodes[pos] = nodes[minChild];
                nodes[minChild] = aux;
                pos = minChild;
            } else {
                return;
            }
        }
    }

    int popMin() {
        int min = nodes[0]->popFirst();
        bubbleDown(0);
        return min;
    }
};

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);
    int worldSize;
    MPI_Comm_size(MPI_COMM_WORLD, &worldSize);
    int worldRank;
    MPI_Comm_rank(MPI_COMM_WORLD, &worldRank);

    int *array = nullptr;
    int arraySize = 0;
    int *subArraySendcounts = nullptr;
    int *subArrayDispls = nullptr;
    double start_time = 0.0;

    //region P0: generate random array and scatter into sub-arrays
    if (worldRank == 0) {
        arraySize = 10000000;
        array = generateRandomArray(arraySize, 0, arraySize);

        start_time = MPI_Wtime();

        if (worldSize == 1) {
            quickSort(array, 0, arraySize - 1);

            double end_time = MPI_Wtime();
            printf("%f", end_time - start_time);

            MPI_Finalize();
            return 0;
        }

        int div = arraySize / worldSize;
        int remainder = arraySize % worldSize;
        subArraySendcounts = new int[worldSize];
        subArrayDispls = new int[worldSize];
        for (int i = 0; i < worldSize; i++) {
            subArraySendcounts[i] = div + (i < remainder ? 1 : 0);
        }
        subArrayDispls[0] = 0;
        for (int i = 1; i < worldSize; i++) {
            subArrayDispls[i] = subArrayDispls[i - 1] + subArraySendcounts[i - 1];
        }
    }

    int subArraySize = 0;
    MPI_Scatter(subArraySendcounts, 1, MPI_INT, &subArraySize, 1, MPI_INT, 0, MPI_COMM_WORLD);
    int *subArray = new int[subArraySize];
    MPI_Scatterv(array, subArraySendcounts, subArrayDispls, MPI_INT, subArray, subArraySize, MPI_INT, 0,
                 MPI_COMM_WORLD);
    //endregion

    quickSort(subArray, 0, subArraySize - 1);

    //region take samples from the sorted sub-array and gather them in P0
    int samplingStride = subArraySize / worldSize;
    int *mySamples = new int[worldSize];
    for (int i = 0; i < worldSize; i++) {
        mySamples[i] = subArray[i * samplingStride];
    }
    int *gatheredSamples = nullptr;
    if (worldRank == 0) {
        gatheredSamples = new int[worldSize * worldSize];
    }
    MPI_Gather(mySamples, worldSize, MPI_INT, gatheredSamples, worldSize, MPI_INT, 0, MPI_COMM_WORLD);
    //endregion

    //region P0: sort samples, then choose and broadcast pivots
    if (worldRank == 0) {
        quickSort(gatheredSamples, 0, worldSize * worldSize - 1);
    }
    int *pivots = new int[worldSize - 1];
    if (worldRank == 0) {
        for (int i = 0; i < worldSize - 1; i++) {
            pivots[i] = gatheredSamples[(i + 1) * worldSize];
        }
    }
    MPI_Bcast(pivots, worldSize - 1, MPI_INT, 0, MPI_COMM_WORLD);
    //endregion

    //region partition the sorted sub-array using the pivots
    int **partitions = new int *[worldSize];
    int *partitionSizes = new int[worldSize]();
    int totalPartitionedSize = 0;
    int it = 0;
    for (int currentPartition = 0; currentPartition < worldSize - 1; currentPartition++) {
        partitions[currentPartition] = subArray + it;
        while (subArray[it] <= pivots[currentPartition]) {
            it++;
        }
        partitionSizes[currentPartition] = it - totalPartitionedSize;
        totalPartitionedSize += partitionSizes[currentPartition];
    }
    partitions[worldSize - 1] = subArray + it;
    partitionSizes[worldSize - 1] = subArraySize - totalPartitionedSize;
    //endregion

    //region gather partitions into corresponding processes
    int *myPartitionSizes = new int[worldSize];
    for (int i = 0; i < worldSize; i++) {
        MPI_Gather(partitionSizes + i, 1, MPI_INT, myPartitionSizes, 1, MPI_INT, i, MPI_COMM_WORLD);
    }

    int *myPartitionDispls = new int[worldSize + 1];
    myPartitionDispls[0] = 0;
    int myPartitionsTotalSize = 0;
    for (int i = 0; i < worldSize; i++) {
        myPartitionsTotalSize += myPartitionSizes[i];
        myPartitionDispls[i + 1] = myPartitionsTotalSize;
    }
    int *dataStart = new int[myPartitionsTotalSize];

    for (int i = 0; i < worldSize; i++) {
        MPI_Gatherv(partitions[i], partitionSizes[i], MPI_INT, dataStart, myPartitionSizes,
                    myPartitionDispls, MPI_INT, i, MPI_COMM_WORLD);
    }
    int **my_partitions = new int *[worldSize];
    int offset = 0;
    for (int i = 0; i < worldSize; i++) {
        my_partitions[i] = dataStart + offset;
        offset += myPartitionSizes[i];
    }
    //endregion

    //region merge the already sorted partitions using a min-heap
    auto **heapNodes = new HeapNode *[worldSize];
    for (int i = 0; i < worldSize; i++) {
        auto *n = new HeapNode(my_partitions[i], myPartitionSizes[i]);
        heapNodes[i] = n;
    }
    auto *heap = new Heap(heapNodes, worldSize);
    int *sortedSubArray = new int[myPartitionsTotalSize];
    for (int i = 0; i < myPartitionsTotalSize; i++) {
        sortedSubArray[i] = heap->popMin();
    }
    //endregion

    // region gather sorted sub-arrays back into root
    int *sortedSubArrayRecvcounts = nullptr;
    int *sortedSubArrayDispls = nullptr;

    if (worldRank == 0) {
        sortedSubArrayRecvcounts = new int[worldSize];
    }

    MPI_Gather(&myPartitionsTotalSize, 1, MPI_INT, sortedSubArrayRecvcounts, 1, MPI_INT, 0, MPI_COMM_WORLD);

    if (worldRank == 0) {
        sortedSubArrayDispls = new int[worldSize];
        sortedSubArrayDispls[0] = 0;
        for (int i = 1; i < worldSize; i++) {
            sortedSubArrayDispls[i] = sortedSubArrayDispls[i - 1] + sortedSubArrayRecvcounts[i - 1];
        }
    }

    MPI_Gatherv(sortedSubArray, myPartitionsTotalSize, MPI_INT, array, sortedSubArrayRecvcounts, sortedSubArrayDispls,
                MPI_INT, 0, MPI_COMM_WORLD);
    //endregion

    if (worldRank == 0) {
        double end_time = MPI_Wtime();
        printf("%f", end_time - start_time);
    }

    MPI_Finalize();
    return 0;
}
