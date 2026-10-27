import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HeapVortexMemoryPools {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    HEAPVORTEX MEMORY POOL REPORT");
        System.out.println("========================================");
        System.out.println("Generated: " + LocalDateTime.now().format(FORMATTER));
        printMemoryPools();
        printRuntimeMemory();
    }

    private static void printMemoryPools() {
        List<MemoryPoolMXBean> pools =
                ManagementFactory.getMemoryPoolMXBeans();

        for (MemoryPoolMXBean pool : pools) {
            System.out.println("\nMemory Pool: " + pool.getName());
            System.out.println("Type: " + pool.getType());

            MemoryUsage usage = pool.getUsage();
            if (usage == null) {
                System.out.println("Usage data is unavailable.");
                continue;
            }

            System.out.printf("Used: %.2f MB%n", toMB(usage.getUsed()));
            System.out.printf("Committed: %.2f MB%n", toMB(usage.getCommitted()));

            if (usage.getMax() >= 0) {
                System.out.printf("Maximum: %.2f MB%n", toMB(usage.getMax()));
                double utilization = usage.getMax() > 0
                        ? usage.getUsed() * 100.0 / usage.getMax()
                        : 0.0;
                System.out.printf("Utilization: %.2f%%%n", utilization);
            } else {
                System.out.println("Maximum: Not specified by the JVM");
            }

            System.out.println("Usage threshold supported: "
                    + pool.isUsageThresholdSupported());
        }
    }

    private static void printRuntimeMemory() {
        Runtime runtime = Runtime.getRuntime();

        long allocated = runtime.totalMemory();
        long available = runtime.maxMemory();
        long free = runtime.freeMemory();
        long used = allocated - free;

        System.out.println("\n--- Runtime Memory Summary ---");
        System.out.printf("Used memory: %.2f MB%n", toMB(used));
        System.out.printf("Allocated memory: %.2f MB%n", toMB(allocated));
        System.out.printf("Free allocated memory: %.2f MB%n", toMB(free));
        System.out.printf("Maximum runtime memory: %.2f MB%n", toMB(available));
    }

    private static double toMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
