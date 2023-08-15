## Parallel Quick-Sort

**Parallel sorting by regular sampling** as described in [this lecture](https://www.uio.no/studier/emner/matnat/ifi/INF3380/v10/undervisningsmateriale/inf3380-week12.pdf), on page 14.

## Algorithm
Let's assume we're using `3 cores` to sort an `n=30` array:

`[23, 3, 7, 16, 18, 26, 29, 30, 22, 19, 2, 4, 20, 28, 6, 5, 9, 1, 10, 21, 13, 17, 24, 8, 25, 15, 11, 27, 12, 14]`

1. Array is split into 3 segments:
- Core 1 gets: `[23, 3, 7, 16, 18, 26, 29, 30, 22, 19]`
- Core 2 gets: `[2, 4, 20, 28, 6, 5, 9, 1, 10, 21]`
- Core 3 gets: `[13, 17, 24, 8, 25, 15, 11, 27, 12, 14]`

2. Each core sorts its segment using usual quick-sort:
- Core 1: `[3, 7, 16, 18, 19, 22, 23, 26, 29, 30]`
- Core 2: `[1, 2, 4, 5, 6, 9, 10, 20, 21, 28]`
- Core 3: `[8, 11, 12, 13, 14, 15, 17, 24, 25, 27]`
- Now we need to merge these 3 sorted arrays into one big sorted array (which is the final output).
- But we need to do so in a parallel manner, and split the work as evenly as possible.

3. Each core gathers some elements (samples), evenly spaced, from its segment:
- Core 1 samples: `[3, 18, 23]`
- Core 2 samples: `[1, 5, 10]`
- Core 3 samples: `[8, 13, 17]`

4. Gathered samples are merged and sorted:
- `[1, 3, 5, 8, 10, 13, 17, 18, 23]`
- the final pivots are selected by evenly dividing the samples array: `[8, 17]`

5. Each core splits its segment using the pivots `8` and `17` into 3 smaller partitions:
- Core 1: `[3, 7]`, `[16]`, `[18, 19, 22, 23, 26, 29, 30]`
- Core 2: `[1, 2, 4, 5, 6]`, `[9, 10]`, `[20, 21, 28]`
- Core 3: `[8]`, `[ 11, 12, 13, 14, 15, 17]`, `[24, 25, 27]`

6. The smaller partitions are sent to their corresponding core:
- Core 1 gets the low value partitions: `[3, 7]`, `[1, 2, 4, 5, 6]`, `[8]`  
- Core 2 gets the mid value partitions: `[16]`, `[9, 10]`, `[ 11, 12, 13, 14, 15, 17]` 
- Core 3 gets the high value partitions: `[18, 19, 22, 23, 26, 29, 30]`, `[20, 21, 28]`, `[24, 25, 27]`

7. Each core merges its partitions:
- Core 1: `[1, 2, 3, 4, 5, 6, 7, 8]`
- Core 2: `[9, 10, 11, 12, 13, 14, 15, 16, 17]` 
- Core 3: `[18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]`

8. Results from each core are concatenated to obtain the final sorted array:
- `[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]`

### Benchmarks

```
CPU: i5 2410m
Array size: 10M
Cores: 1 	Avg_time:1.67
Cores: 4 	Avg_time:0.93
Ratio: 56%
```

```
CPU: i3 4130
Array size: 10M
Cores: 1 	Avg_time:1.34
Cores: 4 	Avg_time:0.73
Ratio: 54%
```

```
CPU: P8700
Array size: 10M
Cores: 1 	Avg_time:2.05
Cores: 2 	Avg_time:1.43
Ratio: 70%
```

Performance improvement is not proportional with the number of cores. It's less than that, because there is some overhead cost to splitting the array and the interprocess communication, and core load balancing is not guaranteed to be even. But still a significant improvement.
