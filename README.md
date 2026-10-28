# HeapVortex JVM Diagnostics

A small Java prototype for inspecting JVM heap usage and garbage-collector activity.

## Requirements
- JDK 17 or newer

## Run
From this project directory:

```bash
javac -d out src/main/java/com/heapvortex/HeapVortex.java
java -cp out com.heapvortex.HeapVortex
```

Pass an optional snapshot count (1–100):

```bash
java -cp out com.heapvortex.HeapVortex 8
```

## Features
- Timestamped heap snapshots
- Used, committed, and maximum heap measurements
- Heap utilization warning at 80% of a reported maximum
- Garbage-collector count and elapsed-time statistics

This is a diagnostic prototype, not a definitive memory-leak detector. A leak diagnosis requires examining trends over time and the application's object-retention behavior.
