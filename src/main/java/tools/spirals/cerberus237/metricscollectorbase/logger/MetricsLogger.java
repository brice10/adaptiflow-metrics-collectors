package tools.spirals.cerberus237.metricscollectorbase.logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Logger class to start and stop the periodically log the contents of the SERVICE_METRICS map to a file.
 * @author Arléon Zemtsop (Cerberus)
 */
public class MetricsLogger {

    private static final long LOGGING_INTERVAL_MS = 300000; // Log every 60 seconds
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Starts the metrics logging daemon.
     */
    public static void startLogging() {
        scheduler.scheduleAtFixedRate(new MetricsLoggingDaemon(), 0, LOGGING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the metrics logging daemon.
     */
    public static void stopLogging() {
        scheduler.shutdown();
    }
}