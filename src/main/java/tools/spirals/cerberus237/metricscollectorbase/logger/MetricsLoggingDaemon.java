package tools.spirals.cerberus237.metricscollectorbase.logger;

import tools.spirals.cerberus237.metricscollectorbase.filters.RequestMetricsController;
import tools.spirals.cerberus237.metricscollectorbase.models.ServiceMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Runnable class to periodically log the contents of the SERVICE_METRICS map to a file.
 * @author Arléon Zemtsop (Cerberus)
 */
public class MetricsLoggingDaemon implements Runnable {

    private static final String LOG_FILE_PATH = "service_metrics.log"; // Path to the log file
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        try {
            // Get the current timestamp
            String timestamp = DATE_FORMAT.format(new Date());

            // Open the log file in append mode
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
                // Write the timestamp to the log file
                writer.println("Metrics Log - " + timestamp);
                writer.println("==========================================");

                // Retrieve all metrics from the SERVICE_METRICS map
                Map<String, ServiceMetrics> allMetrics = RequestMetricsController.getAllMetrics();

                // Write each service's metrics to the log file
                for (Map.Entry<String, ServiceMetrics> entry : allMetrics.entrySet()) {
                    String serviceName = entry.getKey();
                    ServiceMetrics metrics = entry.getValue();

                    writer.println("Service: " + serviceName);
                    writer.println("  Received Requests: " + metrics.getReceivedRequests());
                    writer.println("  Failed Requests: " + metrics.getFailedRequests());
                    writer.println("  Total Request Time: " + metrics.getTotalRequestTime() + " ms");
                    writer.println("  Error Rate: " + metrics.getErrorRate());
                    writer.println("  Request Rate (last minute): " + metrics.getRequestRatePerSecond(60000) + " req/s");
                    writer.println("  Request Details: " + metrics.getRequestDetails());
                    writer.println("------------------------------------------");
                }

                writer.println(); // Add a blank line for readability

                RequestMetricsController.resetMetrics();
            }
        } catch (IOException e) {
            // Log the error and continue running
            System.err.println("Failed to write metrics to log file: " + e.getMessage());
        }
    }
}