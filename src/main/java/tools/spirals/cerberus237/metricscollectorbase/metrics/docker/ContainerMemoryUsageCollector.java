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
import tools.spirals.cerberus237.metricscollectorbase.metrics.docker.model.ContainerStats;

import java.util.Optional;

/**
 * Collector for Docker container memory usage.
 * <p>
 * This collector provides a simplified interface to retrieve memory metrics
 * of a Docker container. It supports both absolute values and percentage-based
 * measurements, making it suitable for various monitoring scenarios.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerMemoryUsageCollector memCollector = new ContainerMemoryUsageCollector("my-container");
 * 
 * // Get usage percentage
 * Double usagePercent = memCollector.get();
 * System.out.println("Memory Usage: " + usagePercent + "%");
 * 
 * // Get absolute values
 * System.out.println("Used: " + memCollector.getUsedMemoryMB() + " MB");
 * System.out.println("Limit: " + memCollector.getMemoryLimitMB() + " MB");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerMemoryUsageCollector implements IMetricsCollector<Double> {

    private final ContainerStatsCollector statsCollector;
    private final String containerId;

    /**
     * Constructs a ContainerMemoryUsageCollector for the specified container.
     *
     * @param containerId The ID or name of the container to monitor.
     */
    public ContainerMemoryUsageCollector(String containerId) {
        this.containerId = containerId;
        this.statsCollector = new ContainerStatsCollector(containerId);
    }

    /**
     * Retrieves the current memory usage percentage of the container.
     *
     * @return The memory usage as a percentage (0.0 to 100.0).
     */
    @Override
    public Double get() {
        return getMemoryUsagePercent();
    }

    /**
     * Retrieves the current memory usage percentage.
     *
     * @return Memory usage percentage.
     */
    public double getMemoryUsagePercent() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats)
                .map(ContainerStats.MemoryStats::getUsagePercentage)
                .orElse(0.0);
    }

    /**
     * Retrieves the current memory usage in bytes.
     *
     * @return Memory usage in bytes.
     */
    public long getUsedMemoryBytes() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats)
                .map(ContainerStats.MemoryStats::getUsage)
                .orElse(0L);
    }

    /**
     * Retrieves the current memory usage in megabytes.
     *
     * @return Memory usage in MB.
     */
    public double getUsedMemoryMB() {
        return getUsedMemoryBytes() / (1024.0 * 1024.0);
    }

    /**
     * Retrieves the memory limit in bytes.
     *
     * @return Memory limit in bytes.
     */
    public long getMemoryLimitBytes() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats)
                .map(ContainerStats.MemoryStats::getLimit)
                .orElse(0L);
    }

    /**
     * Retrieves the memory limit in megabytes.
     *
     * @return Memory limit in MB.
     */
    public double getMemoryLimitMB() {
        return getMemoryLimitBytes() / (1024.0 * 1024.0);
    }

    /**
     * Retrieves the available memory in bytes.
     *
     * @return Available memory in bytes.
     */
    public long getAvailableMemoryBytes() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats)
                .map(ContainerStats.MemoryStats::getAvailableMemory)
                .orElse(0L);
    }

    /**
     * Retrieves the available memory in megabytes.
     *
     * @return Available memory in MB.
     */
    public double getAvailableMemoryMB() {
        return getAvailableMemoryBytes() / (1024.0 * 1024.0);
    }

    /**
     * Retrieves the maximum memory usage recorded.
     *
     * @return Maximum memory usage in bytes.
     */
    public long getMaxMemoryUsageBytes() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats)
                .map(ContainerStats.MemoryStats::getMaxUsage)
                .orElse(0L);
    }

    /**
     * Retrieves the cache memory usage.
     *
     * @return Cache memory in bytes.
     */
    public long getCacheMemoryBytes() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats)
                .map(ContainerStats.MemoryStats::getCache)
                .orElse(0L);
    }

    /**
     * Checks if memory usage exceeds the specified threshold.
     *
     * @param thresholdPercent The threshold percentage.
     * @return true if memory usage exceeds the threshold.
     */
    public boolean isMemoryUsageAbove(double thresholdPercent) {
        return getMemoryUsagePercent() > thresholdPercent;
    }

    /**
     * Checks if the container is approaching memory limit.
     *
     * @param warningThresholdPercent The warning threshold (default 80%).
     * @return true if memory usage is above the warning threshold.
     */
    public boolean isMemoryWarning(double warningThresholdPercent) {
        return getMemoryUsagePercent() >= warningThresholdPercent;
    }

    /**
     * Retrieves detailed memory statistics.
     *
     * @return Optional containing MemoryStats if available.
     */
    public Optional<ContainerStats.MemoryStats> getDetailedStats() {
        return statsCollector.getStats()
                .map(ContainerStats::getMemoryStats);
    }

    /**
     * Returns the container ID being monitored.
     *
     * @return The container ID.
     */
    public String getContainerId() {
        return containerId;
    }
}
