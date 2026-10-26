import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.GarbageCollectorMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HeapVortex {

    private static final MemoryMXBean MEMORY =
            ManagementFactory.getMemoryMXBean();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final double WARNING_THRESHOLD = 80.0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("       HEAPVORTEX JVM PROFILER");
        System.out.println("========================================");

        for (int i = 1; i <= 6; i++) {
            displayMemoryReport(i);
            Thread.sleep(3000);
        }

        displayGarbageCollectionStatistics();
        System.out.println("\nProfiling session completed.");
    }

    private static void displayMemoryReport(int number) {
        MemoryUsage heap = MEMORY.getHeapMemoryUsage();

        long used = heap.getUsed();
        long committed = heap.getCommitted();
        long maximum = heap.getMax();

        double percentage = maximum > 0
                ? used * 100.0 / maximum
                : 0.0;

        System.out.println("\nSnapshot: " + number);
        System.out.println("Time: " + LocalDateTime.now().format(FORMATTER));
        System.out.printf("Used heap: %.2f MB%n", toMB(used));
        System.out.printf("Committed heap: %.2f MB%n", toMB(committed));

        if (maximum > 0) {
            System.out.printf("Maximum heap: %.2f MB%n", toMB(maximum));
        } else {
            System.out.println("Maximum heap: Not available");
        }

        System.out.printf("Heap utilization: %.2f%%%n", percentage);

        if (percentage >= WARNING_THRESHOLD) {
            System.out.println("ALERT: High heap utilization detected.");
        } else {
            System.out.println("Status: Heap utilization below threshold.");
        }
    }

    private static void displayGarbageCollectionStatistics() {
        System.out.println("\n--- Garbage Collection Statistics ---");

        List<GarbageCollectorMXBean> collectors =
                ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean collector : collectors) {
            System.out.println("Collector: " + collector.getName());
            System.out.println("Collection count: "
                    + collector.getCollectionCount());
            System.out.println("Collection time: "
                    + collector.getCollectionTime() + " ms");
        }
    }

    private static double toMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
