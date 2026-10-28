package com.heapvortex;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * HeapVortex JVM diagnostics prototype.
 *
 * Collects heap snapshots, reports memory-pool usage, summarizes sample
 * trends, and prints garbage-collector counters available through JMX.
 *
 * This diagnostic sample does not prove a memory leak. Use repeated runs
 * under a representative workload and inspect heap dumps to investigate
 * retained objects.
 *
 * Compile from the repository root:
 * javac src/main/java/com/heapvortex/HeapVortex.java
 *
 * Run from the repository root:
 * java -cp src/main/java com.heapvortex.HeapVortex
 */
public class HeapVortex {
    private static final int DEFAULT_SNAPSHOT_COUNT = 8;
    private static final long DEFAULT_INTERVAL_MS = 1000L;
    private static final double WARNING_THRESHOLD = 0.80;

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<Snapshot> snapshots = new ArrayList<>();

    public static void main(String[] args) {
        int snapshotCount = parsePositiveInt(args, 0, DEFAULT_SNAPSHOT_COUNT);
        long intervalMs = parsePositiveLong(args, 1, DEFAULT_INTERVAL_MS);

        try {
            new HeapVortex().run(snapshotCount, intervalMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Monitoring interrupted; stopping safely.");
        }
    }

    public void run(int snapshotCount, long intervalMs) throws InterruptedException {
        if (snapshotCount < 1) {
            throw new IllegalArgumentException("Snapshot count must be at least 1.");
        }
        if (intervalMs < 0) {
            throw new IllegalArgumentException("Interval cannot be negative.");
        }

        printHeader(snapshotCount, intervalMs);

        for (int index = 1; index <= snapshotCount; index++) {
            Snapshot snapshot = captureSnapshot(index);
            snapshots.add(snapshot);
            printSnapshot(snapshot);

            if (snapshot.utilization >= WARNING_THRESHOLD) {
                System.out.printf(Locale.ROOT,
                        "WARNING: heap usage is at or above %.0f%% of the JVM maximum.%n",
                        WARNING_THRESHOLD * 100.0);
            }

            if (index < snapshotCount && intervalMs > 0) {
                Thread.sleep(intervalMs);
            }
        }

        printSummary();
        printMemoryPools();
        printGarbageCollectorStats();
        System.out.println("Monitoring finished at: " + Instant.now());
    }

    private void printHeader(int count, long intervalMs) {
        System.out.println("==========================================");
        System.out.println("          HeapVortex JVM Monitor");
        System.out.println("==========================================");
        System.out.println("Started at: " + Instant.now());
        System.out.println("Snapshot count: " + count);
        System.out.println("Interval: " + intervalMs + " ms");
        System.out.println("Warning threshold: " + (WARNING_THRESHOLD * 100.0) + "%");
        System.out.println();
    }

    private Snapshot captureSnapshot(int index) {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long used = heap.getUsed();
        long committed = heap.getCommitted();
        long max = heap.getMax();
        double utilization = max > 0 ? (double) used / max : 0.0;

        return new Snapshot(index, Instant.now(), used, committed, max, utilization);
    }

    private void printSnapshot(Snapshot snapshot) {
        System.out.printf(Locale.ROOT,
                "Snapshot %d | %s | used=%s | committed=%s | max=%s | usage=%.2f%%%n",
                snapshot.index,
                snapshot.timestamp,
                formatBytes(snapshot.used),
                formatBytes(snapshot.committed),
                formatBytes(snapshot.max),
                snapshot.utilization * 100.0);
    }

    private void printSummary() {
        if (snapshots.isEmpty()) {
            System.out.println("No snapshots were collected.");
            return;
        }

        double average = snapshots.stream()
                .mapToDouble(item -> item.utilization)
                .average()
                .orElse(0.0);

        Snapshot peak = snapshots.stream()
                .max(Comparator.comparingDouble(item -> item.utilization))
                .orElseThrow();

        Snapshot first = snapshots.get(0);
        Snapshot last = snapshots.get(snapshots.size() - 1);
        double change = last.utilization - first.utilization;

        System.out.println();
        System.out.println("--- Heap Usage Summary ---");
        System.out.printf(Locale.ROOT, "Average utilization: %.2f%%%n", average * 100.0);
        System.out.printf(Locale.ROOT, "Peak utilization: %.2f%%%n", peak.utilization * 100.0);
        System.out.printf(Locale.ROOT, "Peak snapshot: %d%n", peak.index);
        System.out.printf(Locale.ROOT, "First-to-last change: %+.2f percentage points%n",
                change * 100.0);

        if (change >= 0.05) {
            System.out.println("Trend: usage increased noticeably during this sample.");
            System.out.println("Next step: repeat under load and inspect retained objects.");
        } else if (change <= -0.05) {
            System.out.println("Trend: usage decreased noticeably during this sample.");
        } else {
            System.out.println("Trend: no large first-to-last change during this sample.");
        }
    }

    private void printMemoryPools() {
        System.out.println();
        System.out.println("--- JVM Memory Pools ---");

        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        if (pools.isEmpty()) {
            System.out.println("No memory-pool metrics are available.");
            return;
        }

        for (MemoryPoolMXBean pool : pools) {
            MemoryUsage usage = pool.getUsage();
            if (usage == null) {
                System.out.printf("%s | usage unavailable%n", pool.getName());
                continue;
            }

            String type = pool.getType() == MemoryType.HEAP ? "HEAP" : "NON_HEAP";
            System.out.printf(Locale.ROOT,
                    "%s | type=%s | used=%s | committed=%s | max=%s%n",
                    pool.getName(),
                    type,
                    formatBytes(usage.getUsed()),
                    formatBytes(usage.getCommitted()),
                    formatBytes(usage.getMax()));
        }
    }

    private void printGarbageCollectorStats() {
        System.out.println();
        System.out.println("--- Garbage Collector Statistics ---");

        List<GarbageCollectorMXBean> collectors =
                ManagementFactory.getGarbageCollectorMXBeans();

        if (collectors.isEmpty()) {
            System.out.println("No garbage-collector metrics are available.");
            return;
        }

        for (GarbageCollectorMXBean collector : collectors) {
            System.out.printf(Locale.ROOT,
                    "%s | collectionCount=%d | collectionTimeMs=%d%n",
                    collector.getName(),
                    collector.getCollectionCount(),
                    collector.getCollectionTime());
        }
    }

    private static int parsePositiveInt(String[] args, int index, int fallback) {
        if (args.length <= index) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(args[index]);
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException exception) {
            System.err.println("Invalid snapshot count; using default " + fallback + ".");
            return fallback;
        }
    }

    private static long parsePositiveLong(String[] args, int index, long fallback) {
        if (args.length <= index) {
            return fallback;
        }
        try {
            long parsed = Long.parseLong(args[index]);
            return parsed >= 0 ? parsed : fallback;
        } catch (NumberFormatException exception) {
            System.err.println("Invalid interval; using default " + fallback + ".");
            return fallback;
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "unavailable";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }

        double kibibytes = bytes / 1024.0;
        if (kibibytes < 1024.0) {
            return String.format(Locale.ROOT, "%.2f KiB", kibibytes);
        }

        double mebibytes = kibibytes / 1024.0;
        if (mebibytes < 1024.0) {
            return String.format(Locale.ROOT, "%.2f MiB", mebibytes);
        }

        return String.format(Locale.ROOT, "%.2f GiB", mebibytes / 1024.0);
    }

    private static final class Snapshot {
        private final int index;
        private final Instant timestamp;
        private final long used;
        private final long committed;
        private final long max;
        private final double utilization;

        private Snapshot(int index, Instant timestamp, long used,
                         long committed, long max, double utilization) {
            this.index = index;
            this.timestamp = timestamp;
            this.used = used;
            this.committed = committed;
            this.max = max;
            this.utilization = utilization;
        }
    }
}
