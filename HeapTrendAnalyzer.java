package com.heapvortex;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Tracks heap samples and flags sustained growth that may indicate a leak.
 * A rising heap alone does not prove that an application has a memory leak.
 */
public final class HeapTrendAnalyzer {
    private static final int WINDOW_SIZE = 8;
    private static final double GROWTH_WARNING_MIB = 16.0;
    private static final double HIGH_USAGE_RATIO = 0.85;

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final Deque<Long> recentSamples = new ArrayDeque<>();

    public static void main(String[] args) {
        int sampleCount = parseSampleCount(args);
        HeapTrendAnalyzer analyzer = new HeapTrendAnalyzer();

        System.out.println("HeapVortex Heap Trend Analyzer");
        System.out.println("Collecting " + sampleCount + " heap samples...");
        for (int sample = 1; sample <= sampleCount; sample++) {
            analyzer.capture(sample);
            if (sample < sampleCount) {
                sleepOneSecond();
            }
        }
        System.out.println("Heap trend analysis finished.");
    }

    private static int parseSampleCount(String[] args) {
        if (args.length == 0) return 10;
        try {
            int value = Integer.parseInt(args[0]);
            if (value < 2 || value > 500) {
                System.out.println("Choose between 2 and 500 samples; using 10.");
                return 10;
            }
            return value;
        } catch (NumberFormatException exception) {
            System.out.println("Invalid sample count; using 10.");
            return 10;
        }
    }

    private void capture(int sampleNumber) {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long usedBytes = heap.getUsed();
        long maxBytes = heap.getMax();
        recentSamples.addLast(usedBytes);
        if (recentSamples.size() > WINDOW_SIZE) recentSamples.removeFirst();

        System.out.println();
        System.out.printf(Locale.ROOT, "Sample %d at %s%n",
                sampleNumber, Instant.now());
        System.out.printf(Locale.ROOT, "Heap used: %.2f MiB%n", toMiB(usedBytes));
        System.out.printf(Locale.ROOT, "Heap committed: %.2f MiB%n",
                toMiB(heap.getCommitted()));

        double utilization = maxBytes > 0 ? (double) usedBytes / maxBytes : -1.0;
        if (maxBytes > 0) {
            System.out.printf(Locale.ROOT, "Heap maximum: %.2f MiB%n", toMiB(maxBytes));
            System.out.printf(Locale.ROOT, "Heap utilization: %.1f%%%n", utilization * 100);
        } else {
            System.out.println("Heap maximum: not reported by this JVM");
        }
        reportTrend(utilization);
    }

    private void reportTrend(double utilization) {
        if (recentSamples.size() < 2) {
            System.out.println("Trend: collecting baseline samples");
            return;
        }

        long first = recentSamples.getFirst();
        long latest = recentSamples.getLast();
        double growthMiB = toMiB(latest - first);
        System.out.printf(Locale.ROOT,
                "Window growth across %d samples: %+.2f MiB%n",
                recentSamples.size(), growthMiB);

        if (utilization >= HIGH_USAGE_RATIO) {
            System.out.println("Assessment: HIGH HEAP UTILIZATION");
        } else if (growthMiB >= GROWTH_WARNING_MIB
                && recentSamples.size() == WINDOW_SIZE) {
            System.out.println("Assessment: SUSTAINED GROWTH - investigate retained objects");
        } else if (growthMiB < 0) {
            System.out.println("Assessment: heap usage decreased during this window");
        } else {
            System.out.println("Assessment: no growth warning in the current window");
        }
        System.out.println("Confirm suspected leaks with heap dumps and retention analysis.");
    }

    private static double toMiB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    private static void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.out.println("Sampling interrupted.");
        }
    }
}
