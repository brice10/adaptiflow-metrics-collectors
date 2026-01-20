package tools.spirals.cerberus237.metricscollectorbase.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents metrics for a single service instance.
 *
 * Arléon Zemtsop (Cerberus)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceMetrics {
    private String serviceName;
    private final AtomicInteger receivedRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicLong totalRequestTime = new AtomicLong(0);
    private final Queue<RequestDetails> requestWindow = new ConcurrentLinkedQueue<>();
    private double errorRate = 0;

    public ServiceMetrics() {
    }

    public ServiceMetrics(String serviceName) {
        this.serviceName = serviceName;
    }

    public void logRequest(Date receivedAt, String sourceHost, int sourcePort, String targetHost, int targetPort,
                           String endpoint, long requestTime, boolean success) {
        receivedRequests.incrementAndGet();
        if (!success) {
            failedRequests.incrementAndGet();
        }
        totalRequestTime.addAndGet(requestTime);
        requestWindow.add(new RequestDetails(receivedAt, sourceHost, sourcePort,
                targetHost, targetPort, endpoint,
                requestTime, success));
        cleanOldRequests(60000);
    }


    private void cleanOldRequests(long windowMillis) {
        long cutoff = System.currentTimeMillis() - windowMillis;
        requestWindow.removeIf(detail ->
                detail.getReceivedAt().getTime() < cutoff
        );
    }

    public double getErrorRate() {
        errorRate = (receivedRequests.get() == 0) ? 0 : (double) failedRequests.get() / receivedRequests.get();
        return errorRate;
    }

    public double getErrorRate(long timeWindowMillis) {
        cleanOldRequests(timeWindowMillis);
        long failures = requestWindow.stream()
                .filter(d -> !d.isSuccess())
                .count();
        return requestWindow.isEmpty() ? 0 :
                (double) failures / requestWindow.size();
    }

    public double getRequestRatePerSecond(long timeWindowMillis) {
        cleanOldRequests(timeWindowMillis);
        int count = requestWindow.size();
        return (timeWindowMillis == 0) ? 0 :
                (count / (timeWindowMillis / 1000.0));
    }

    public List<RequestDetails> getRequestDetails() {
        return new ArrayList<>(requestWindow);
    }

    public int getReceivedRequests() {
        return receivedRequests.get();
    }

    public int getFailedRequests() {
        return failedRequests.get();
    }

    public long getTotalRequestTime() {
        return totalRequestTime.get();
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return "ServiceMetrics{" +
                "serviceName='" + serviceName + '\'' +
                ", receivedRequests=" + receivedRequests +
                ", failedRequests=" + failedRequests +
                ", totalRequestTime=" + totalRequestTime +
                ", errorRate=" + errorRate +
                ", requestRatePerSecond=" + this.getRequestRatePerSecond(60000) +
                ", requestDetails=" + requestWindow +
                '}';
    }
}

