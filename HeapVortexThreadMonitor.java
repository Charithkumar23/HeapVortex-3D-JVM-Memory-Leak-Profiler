import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HeapVortexThreadMonitor {

    private static final ThreadMXBean THREADS =
            ManagementFactory.getThreadMXBean();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        printThreadSummary();
        printThreadDetails();
    }

    private static void printThreadSummary() {
        System.out.println("========================================");
        System.out.println("   HEAPVORTEX JVM THREAD MONITOR");
        System.out.println("========================================");
        System.out.println("Captured at: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("Live threads: " + THREADS.getThreadCount());
        System.out.println("Peak live threads: " + THREADS.getPeakThreadCount());
        System.out.println("Daemon threads: " + THREADS.getDaemonThreadCount());
        System.out.println("Total started threads: " + THREADS.getTotalStartedThreadCount());
        System.out.println("Thread CPU time supported: " + THREADS.isThreadCpuTimeSupported());
    }

    private static void printThreadDetails() {
        System.out.println("\\n--- Live Thread Details ---");
        long[] ids = THREADS.getAllThreadIds();
        ThreadInfo[] details = THREADS.getThreadInfo(ids);

        for (ThreadInfo info : details) {
            if (info == null) {
                continue;
            }

            System.out.println("\\nThread: " + info.getThreadName());
            System.out.println("ID: " + info.getThreadId());
            System.out.println("State: " + info.getThreadState());
            System.out.println("Blocked count: " + info.getBlockedCount());
            System.out.println("Waited count: " + info.getWaitedCount());
        }
    }
}
