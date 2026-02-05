/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.spirals.cerberus237.metricscollectorbase.metrics.docker;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;
import tools.spirals.cerberus237.metricscollectorbase.metrics.docker.model.ContainerInfo;

import java.util.Optional;

/**
 * Collector for Docker container health status.
 * <p>
 * This collector monitors the health check status of a Docker container.
 * Docker health checks are configured in the Dockerfile or at runtime to
 * periodically verify that the container is functioning correctly.
 * </p>
 *
 * <p>
 * Health statuses include:
 * </p>
 * <ul>
 *     <li><strong>starting:</strong> Health check is being initialized</li>
 *     <li><strong>healthy:</strong> Container is passing health checks</li>
 *     <li><strong>unhealthy:</strong> Container is failing health checks</li>
 *     <li><strong>none:</strong> No health check is configured</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerHealthCollector healthCollector = new ContainerHealthCollector("my-container");
 * 
 * String status = healthCollector.get();
 * System.out.println("Health Status: " + status);
 * 
 * if (healthCollector.isHealthy()) {
 *     System.out.println("Container is healthy!");
 * } else if (healthCollector.isUnhealthy()) {
 *     System.out.println("WARNING: Container is unhealthy!");
 * }
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerHealthCollector implements IMetricsCollector<String> {

    /** Health status when container is passing health checks. */
    public static final String STATUS_HEALTHY = "healthy";
    
    /** Health status when container is failing health checks. */
    public static final String STATUS_UNHEALTHY = "unhealthy";
    
    /** Health status when health check is initializing. */
    public static final String STATUS_STARTING = "starting";
    
    /** Health status when no health check is configured. */
    public static final String STATUS_NONE = "none";

    private final ContainerInfoCollector infoCollector;
    private final String containerId;

    /**
     * Constructs a ContainerHealthCollector for the specified container.
     *
     * @param containerId The ID or name of the container.
     */
    public ContainerHealthCollector(String containerId) {
        this.containerId = containerId;
        this.infoCollector = new ContainerInfoCollector(containerId);
    }

    /**
     * Retrieves the current health status of the container.
     *
     * @return The health status string (healthy, unhealthy, starting, or none).
     */
    @Override
    public String get() {
        return getHealthStatus();
    }

    /**
     * Retrieves the health status from container inspection.
     *
     * @return Health status string.
     */
    public String getHealthStatus() {
        return infoCollector.getContainerInfo()
                .map(ContainerInfo::getHealthStatus)
                .orElse(STATUS_NONE);
    }

    /**
     * Checks if the container is healthy.
     *
     * @return true if the container health status is "healthy".
     */
    public boolean isHealthy() {
        return STATUS_HEALTHY.equalsIgnoreCase(getHealthStatus());
    }

    /**
     * Checks if the container is unhealthy.
     *
     * @return true if the container health status is "unhealthy".
     */
    public boolean isUnhealthy() {
        return STATUS_UNHEALTHY.equalsIgnoreCase(getHealthStatus());
    }

    /**
     * Checks if the health check is starting.
     *
     * @return true if the container health status is "starting".
     */
    public boolean isStarting() {
        return STATUS_STARTING.equalsIgnoreCase(getHealthStatus());
    }

    /**
     * Checks if no health check is configured.
     *
     * @return true if no health check is defined for the container.
     */
    public boolean hasNoHealthCheck() {
        String status = getHealthStatus();
        return STATUS_NONE.equalsIgnoreCase(status) || status == null || status.isEmpty();
    }

    /**
     * Checks if a health check is configured (regardless of status).
     *
     * @return true if a health check is defined.
     */
    public boolean hasHealthCheck() {
        return !hasNoHealthCheck();
    }

    /**
     * Checks if the container is running.
     *
     * @return true if the container is running.
     */
    public boolean isRunning() {
        return infoCollector.getContainerInfo()
                .map(ContainerInfo::isRunning)
                .orElse(false);
    }

    /**
     * Retrieves the container state (running, paused, etc.).
     *
     * @return Container state string.
     */
    public String getContainerState() {
        return infoCollector.getContainerInfo()
                .map(ContainerInfo::getState)
                .orElse("unknown");
    }

    /**
     * Retrieves the container status (human-readable).
     *
     * @return Container status string.
     */
    public String getContainerStatus() {
        return infoCollector.getContainerInfo()
                .map(ContainerInfo::getStatus)
                .orElse("unknown");
    }

    /**
     * Retrieves the number of container restarts.
     *
     * @return Restart count.
     */
    public int getRestartCount() {
        return infoCollector.getContainerInfo()
                .map(ContainerInfo::getRestartCount)
                .orElse(0);
    }

    /**
     * Checks if the container has restarted.
     *
     * @return true if restart count is greater than 0.
     */
    public boolean hasRestarted() {
        return getRestartCount() > 0;
    }

    /**
     * Returns a comprehensive health summary.
     *
     * @return Health summary object.
     */
    public HealthSummary getHealthSummary() {
        Optional<ContainerInfo> info = infoCollector.getContainerInfo();
        
        return new HealthSummary(
                containerId,
                info.map(ContainerInfo::getName).orElse("unknown"),
                getHealthStatus(),
                info.map(ContainerInfo::getState).orElse("unknown"),
                info.map(ContainerInfo::isRunning).orElse(false),
                info.map(ContainerInfo::getRestartCount).orElse(0)
        );
    }

    /**
     * Returns the container ID being monitored.
     *
     * @return The container ID.
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * Health summary data class.
     */
    public static class HealthSummary {
        private final String containerId;
        private final String containerName;
        private final String healthStatus;
        private final String state;
        private final boolean running;
        private final int restartCount;

        public HealthSummary(String containerId, String containerName, String healthStatus,
                            String state, boolean running, int restartCount) {
            this.containerId = containerId;
            this.containerName = containerName;
            this.healthStatus = healthStatus;
            this.state = state;
            this.running = running;
            this.restartCount = restartCount;
        }

        public String getContainerId() { return containerId; }
        public String getContainerName() { return containerName; }
        public String getHealthStatus() { return healthStatus; }
        public String getState() { return state; }
        public boolean isRunning() { return running; }
        public int getRestartCount() { return restartCount; }

        public boolean isHealthy() {
            return running && STATUS_HEALTHY.equalsIgnoreCase(healthStatus);
        }

        @Override
        public String toString() {
            return String.format("HealthSummary{container=%s, health=%s, state=%s, running=%s, restarts=%d}",
                    containerName, healthStatus, state, running, restartCount);
        }
    }
}
