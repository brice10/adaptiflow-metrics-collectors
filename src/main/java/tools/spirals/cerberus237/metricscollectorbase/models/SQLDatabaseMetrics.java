package tools.spirals.cerberus237.metricscollectorbase.models;

/**
 *
 *  @author Arléon Zemtsop (Cerberus)
 */
public class SQLDatabaseMetrics {
    private Long responseTime;
    private Boolean networkStatus;
    private Integer activeConnections;
    private Integer pendingQueries;

    public SQLDatabaseMetrics() {}

    public SQLDatabaseMetrics(Long responseTime, Boolean networkStatus, Integer activeConnections, Integer pendingQueries) {
        this.responseTime = responseTime;
        this.networkStatus = networkStatus;
        this.activeConnections = activeConnections;
        this.pendingQueries = pendingQueries;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public Boolean getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(Boolean networkStatus) {
        this.networkStatus = networkStatus;
    }

    public Integer getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Integer getPendingQueries() {
        return pendingQueries;
    }

    public void setPendingQueries(Integer pendingQueries) {
        this.pendingQueries = pendingQueries;
    }

    @Override
    public String toString() {
        return String.format(
                "SQLDatabaseMetrics{responseTime=%d, networkStatus=%s, activeConnections=%d, pendingQueries=%d}",
                responseTime, networkStatus, activeConnections, pendingQueries
        );
    }
}
