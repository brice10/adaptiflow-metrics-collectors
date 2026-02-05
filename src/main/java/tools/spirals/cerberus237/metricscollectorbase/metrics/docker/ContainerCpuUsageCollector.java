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
 * Collector for Docker container CPU usage percentage.
 * <p>
 * This collector provides a simplified interface to retrieve only the CPU usage
 * percentage of a Docker container. It is particularly useful for monitoring
 * and alerting scenarios where only CPU metrics are needed.
 * </p>
 *
 * <p>
 * The CPU percentage is calculated based on the delta between consecutive
 * measurements, taking into account the number of available CPUs.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerCpuUsageCollector cpuCollector = new ContainerCpuUsageCollector("my-container");
 * Double cpuUsage = cpuCollector.get();
 * System.out.println("CPU Usage: " + cpuUsage + "%");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerCpuUsageCollector implements IMetricsCollector<Double> {

    private final ContainerStatsCollector statsCollector;
    private final String containerId;

    /**
     * Constructs a ContainerCpuUsageCollector for the specified container.
     *
     * @param containerId The ID or name of the container to monitor.
     */
    public ContainerCpuUsageCollector(String containerId) {
        this.containerId = containerId;
        this.statsCollector = new ContainerStatsCollector(containerId);
    }

    /**
     * Retrieves the current CPU usage percentage of the container.
     *
     * @return The CPU usage as a percentage (0.0 to 100.0 * numCPUs).
     */
    @Override
    public Double get() {
        return getCpuUsage();
    }

    /**
     * Retrieves the current CPU usage percentage.
     *
     * @return CPU usage percentage.
     */
    public double getCpuUsage() {
        return statsCollector.getStats()
                .map(ContainerStats::getCpuStats)
                .map(ContainerStats.CpuStats::getCpuPercentage)
                .orElse(0.0);
    }

    /**
     * Checks if CPU usage exceeds the specified threshold.
     *
     * @param thresholdPercent The threshold percentage.
     * @return true if CPU usage exceeds the threshold.
     */
    public boolean isCpuUsageAbove(double thresholdPercent) {
        return getCpuUsage() > thresholdPercent;
    }

    /**
     * Retrieves detailed CPU statistics.
     *
     * @return Optional containing CpuStats if available.
     */
    public Optional<ContainerStats.CpuStats> getDetailedStats() {
        return statsCollector.getStats()
                .map(ContainerStats::getCpuStats);
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
