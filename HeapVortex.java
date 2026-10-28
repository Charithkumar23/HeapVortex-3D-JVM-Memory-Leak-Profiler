package com.heapvortex;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class HeapVortex {
    private static final MemoryMXBean MEMORY =
            ManagementFactory.getMemoryMXBean();
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private HeapVortex() {
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("HeapVortex JVM Diagnostics");
        System.out.println("Started: " + LocalDateTime.now().format(FORMAT));

        int snapshots = args.length > 0 ? parseSnapshotCount(args[0]) : 5;
        for (int i = 1; i <= snapshots; i++) {
            printHeapSnapshot(i);
            if (i < snapshots) {
                Thread.sleep(2000);
            }
        }

        printGarbageCollectors();
    }

    private static int parseSnapshotCount(String value) {
        try {
            int count = Integer.parseInt(value);
            if (count < 1 || count > 100) {
                throw new IllegalArgumentException("Snapshots must be between 1 and 100.");
            }
            return count;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Snapshot count must be an integer.", exception);
        }
    }

    private static void printHeapSnapshot(int number) {
        MemoryUsage heap = MEMORY.getHeapMemoryUsage();
        long used = heap.getUsed();
        long committed = heap.getCommitted();
        long maximum = heap.getMax();

        System.out.println();
        System.out.println("Snapshot #" + number);
        System.out.println("Captured: " + LocalDateTime.now().format(FORMAT));
        System.out.printf("Used heap: %.2f MB%n", toMegabytes(used));
        System.out.printf("Committed heap: %.2f MB%n", toMegabytes(committed));

        if (maximum > 0) {
            double utilization = used * 100.0 / maximum;
            System.out.printf("Maximum heap: %.2f MB%n", toMegabytes(maximum));
            System.out.printf("Heap utilization: %.2f%%%n", utilization);
            if (utilization >= 80.0) {
                System.out.println("Warning: heap utilization is at least 80%.");
            }
        } else {
            System.out.println("Maximum heap: JVM does not report a fixed limit.");
        }
    }

    private static void printGarbageCollectors() {
        System.out.println();
        System.out.println("Garbage collector statistics");
        List<GarbageCollectorMXBean> collectors =
                ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean collector : collectors) {
            System.out.println("Collector: " + collector.getName());
            System.out.println("Collection count: " + collector.getCollectionCount());
            System.out.println("Collection time (ms): " + collector.getCollectionTime());
        }
    }

    private static double toMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
