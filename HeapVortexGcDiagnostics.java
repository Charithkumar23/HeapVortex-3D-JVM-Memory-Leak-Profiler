package com.heapvortex;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * HeapVortex runtime diagnostics for JVM memory pools and garbage collectors.
 * Run from the project root after compiling this class into the output folder.
 */
public final class HeapVortexGcDiagnostics {
    private static final double WARNING_THRESHOLD = 0.80;
    private static final double CRITICAL_THRESHOLD = 0.95;

    private HeapVortexGcDiagnostics() {
    }

    public static void main(String[] args) {
        int snapshots = parseSnapshotCount(args);
        System.out.println("HeapVortex JVM Memory Diagnostics");
        System.out.println("Started: " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        System.out.println("Snapshots requested: " + snapshots);

        for (int index = 1; index <= snapshots; index++) {
            printSnapshot(index);
            if (index < snapshots) {
                pauseBetweenSnapshots();
            }
        }
        System.out.println("Diagnostics completed.");
    }

    private static int parseSnapshotCount(String[] args) {
        if (args.length == 0) {
            return 1;
        }
        try {
            int count = Integer.parseInt(args[0]);
            if (count < 1 || count > 100) {
                System.out.println("Snapshot count must be between 1 and 100; using 1.");
                return 1;
            }
            return count;
        } catch (NumberFormatException exception) {
            System.out.println("Invalid snapshot count; using 1.");
            return 1;
        }
    }

    private static void printSnapshot(int number) {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long committed = runtime.totalMemory();
        long maximum = runtime.maxMemory();

        System.out.println();
        System.out.println("==================================================");
        System.out.printf(Locale.ROOT, "Snapshot %d | %s%n",
                number, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        printHeapMetric("Used heap", used);
        printHeapMetric("Committed heap", committed);
        printHeapMetric("Maximum heap", maximum);

        double utilization = maximum > 0 ? (double) used / maximum : 0.0;
        System.out.printf(Locale.ROOT, "Maximum-heap utilization: %.2f%%%n", utilization * 100);
        System.out.println("Heap status: " + classifyUtilization(utilization));
        printMemoryPools();
        printGarbageCollectors();
        System.out.println("Live thread count: " + Thread.activeCount());
    }

    private static void printHeapMetric(String label, long bytes) {
        System.out.printf(Locale.ROOT, "%-18s %,d bytes (%.2f MiB)%n",
                label + ":", bytes, bytes / (1024.0 * 1024.0));
    }

    private static String classifyUtilization(double utilization) {
        if (utilization >= CRITICAL_THRESHOLD) {
            return "CRITICAL - heap usage is near the configured maximum";
        }
        if (utilization >= WARNING_THRESHOLD) {
            return "WARNING - inspect allocation rate and retained objects";
        }
        return "HEALTHY - below the configured warning threshold";
    }

    private static void printMemoryPools() {
        System.out.println("-- Memory pools --");
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            MemoryUsage usage = pool.getUsage();
            if (usage == null) {
                continue;
            }
            long used = usage.getUsed();
            long max = usage.getMax();
            String limit = max > 0
                    ? String.format(Locale.ROOT, "%.1f%%", 100.0 * used / max)
                    : "maximum unavailable";
            System.out.printf(Locale.ROOT,
                    "%s | used: %,d bytes | committed: %,d bytes | limit: %s%n",
                    pool.getName(), used, usage.getCommitted(), limit);
        }
    }

    private static void printGarbageCollectors() {
        System.out.println("-- Garbage collectors --");
        List<GarbageCollectorMXBean> collectors =
                ManagementFactory.getGarbageCollectorMXBeans();
        long totalCollections = 0;
        long totalCollectionMillis = 0;

        for (GarbageCollectorMXBean collector : collectors) {
            long count = collector.getCollectionCount();
            long millis = collector.getCollectionTime();
            if (count >= 0) {
                totalCollections += count;
            }
            if (millis >= 0) {
                totalCollectionMillis += millis;
            }
            System.out.printf(Locale.ROOT, "%s | collections: %d | elapsed: %d ms%n",
                    collector.getName(), count, millis);
        }
        System.out.printf(Locale.ROOT, "Total reported collections: %d%n", totalCollections);
        System.out.printf(Locale.ROOT, "Total reported GC time: %d ms%n", totalCollectionMillis);
    }

    private static void pauseBetweenSnapshots() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.out.println("Snapshot sequence interrupted.");
        }
    }
}
