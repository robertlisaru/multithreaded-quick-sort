## Parallel Quick-Sort

Parallel Quick-Sort as described here, on page 14:
https://www.uio.no/studier/emner/matnat/ifi/INF3380/v10/undervisningsmateriale/inf3380-week12.pdf

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
