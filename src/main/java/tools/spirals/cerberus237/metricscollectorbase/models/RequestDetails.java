package tools.spirals.cerberus237.metricscollectorbase.models;

import java.util.Date;

/**
 * Represents details of a single request.
 *
 * Arléon Zemtsop (Cerberus)
 */
public class RequestDetails {
    private Date receivedAt;
    private String sourceHost;
    private int sourcePort;
    private String targetHost;
    private int targetPort;
    private String endpoint;
    private long requestTime;
    private boolean success;

    public RequestDetails() {
    }

    public RequestDetails(Date receivedAt, String sourceHost, int sourcePort, String targetHost, int targetPort,
                          String endpoint, long requestTime, boolean success) {
        this.receivedAt = receivedAt;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.endpoint = endpoint;
        this.requestTime = requestTime;
        this.success = success;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return String.format("RequestDetails{receivedAt=%s, sourceHost=%s, sourcePort=%d, targetHost=%s, targetPort=%d, endpoint=%s, requestTime=%d, success=%b}",
                receivedAt, sourceHost, sourcePort, targetHost, targetPort, endpoint, requestTime, success);
    }
}
