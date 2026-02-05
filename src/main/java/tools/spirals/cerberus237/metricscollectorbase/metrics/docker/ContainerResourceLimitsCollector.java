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
 * Collector for Docker container resource limits.
 * <p>
 * This collector retrieves the configured resource limits for a Docker container,
 * including CPU limits, memory limits, and I/O constraints. Understanding these
 * limits is crucial for adaptive systems to make informed scaling decisions.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerResourceLimitsCollector limitsCollector = 
 *         new ContainerResourceLimitsCollector("my-container");
 * 
 * Optional&lt;ContainerInfo.ResourceLimits&gt; limits = limitsCollector.get();
 * limits.ifPresent(l -> {
 *     System.out.println("Memory Limit: " + l.getMemoryLimitMB() + " MB");
 *     System.out.println("CPU Limit: " + l.getCpuLimit() + " cores");
 * });
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerResourceLimitsCollector implements IMetricsCollector<Optional<ContainerInfo.ResourceLimits>> {

    private final ContainerInfoCollector infoCollector;
    private final String containerId;

    /**
     * Constructs a ContainerResourceLimitsCollector for the specified container.
     *
     * @param containerId The ID or name of the container.
     */
    public ContainerResourceLimitsCollector(String containerId) {
        this.containerId = containerId;
        this.infoCollector = new ContainerInfoCollector(containerId);
    }

    /**
     * Retrieves the resource limits for the container.
     *
     * @return Optional containing ResourceLimits if available.
     */
    @Override
    public Optional<ContainerInfo.ResourceLimits> get() {
        return getResourceLimits();
    }

    /**
     * Retrieves resource limits from container inspection.
     *
     * @return Optional containing ResourceLimits.
     */
    public Optional<ContainerInfo.ResourceLimits> getResourceLimits() {
        return infoCollector.getContainerInfo()
                .map(ContainerInfo::getResourceLimits);
    }

    /**
     * Retrieves the memory limit in bytes.
     *
     * @return Memory limit in bytes, or 0 if unlimited.
     */
    public long getMemoryLimitBytes() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getMemoryLimit)
                .orElse(0L);
    }

    /**
     * Retrieves the memory limit in megabytes.
     *
     * @return Memory limit in MB, or 0 if unlimited.
     */
    public double getMemoryLimitMB() {
        return getMemoryLimitBytes() / (1024.0 * 1024.0);
    }

    /**
     * Retrieves the memory limit in gigabytes.
     *
     * @return Memory limit in GB, or 0 if unlimited.
     */
    public double getMemoryLimitGB() {
        return getMemoryLimitBytes() / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * Retrieves the memory reservation (soft limit) in bytes.
     *
     * @return Memory reservation in bytes.
     */
    public long getMemoryReservationBytes() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getMemoryReservation)
                .orElse(0L);
    }

    /**
     * Retrieves the CPU limit as number of cores.
     * Calculated from NanoCPUs or CpuQuota/CpuPeriod.
     *
     * @return CPU limit in cores, or 0 if unlimited.
     */
    public double getCpuLimit() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getCpuLimit)
                .orElse(0.0);
    }

    /**
     * Retrieves CPU shares (relative weight).
     *
     * @return CPU shares, default is 1024.
     */
    public long getCpuShares() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getCpuShares)
                .orElse(1024L);
    }

    /**
     * Retrieves the CPUset (pinned CPUs).
     *
     * @return CPUset string (e.g., "0-3" or "0,2").
     */
    public String getCpusetCpus() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getCpusetCpus)
                .orElse("");
    }

    /**
     * Retrieves the PIDs limit.
     *
     * @return Maximum number of processes, or -1 if unlimited.
     */
    public int getPidsLimit() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getPidsLimit)
                .orElse(-1);
    }

    /**
     * Retrieves the block I/O weight.
     *
     * @return Block I/O weight (10-1000), default 0.
     */
    public long getBlkioWeight() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::getBlkioWeight)
                .orElse(0L);
    }

    /**
     * Checks if OOM killer is disabled.
     *
     * @return true if OOM killer is disabled.
     */
    public boolean isOomKillDisabled() {
        return getResourceLimits()
                .map(ContainerInfo.ResourceLimits::isOomKillDisable)
                .orElse(false);
    }

    /**
     * Checks if memory limit is configured.
     *
     * @return true if a memory limit is set.
     */
    public boolean hasMemoryLimit() {
        long limit = getMemoryLimitBytes();
        return limit > 0;
    }

    /**
     * Checks if CPU limit is configured.
     *
     * @return true if a CPU limit is set.
     */
    public boolean hasCpuLimit() {
        return getCpuLimit() > 0;
    }

    /**
     * Checks if the container has any resource limits configured.
     *
     * @return true if any limits are set.
     */
    public boolean hasAnyLimits() {
        return hasMemoryLimit() || hasCpuLimit();
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
