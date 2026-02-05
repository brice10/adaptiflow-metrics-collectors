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
package tools.spirals.cerberus237.metricscollectorbase.metrics.docker.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Data model representing comprehensive Docker container statistics.
 * <p>
 * This class encapsulates all relevant metrics collected from a Docker container,
 * including CPU, memory, network, and block I/O statistics. It serves as a unified
 * data transfer object for container metrics.
 * </p>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerStats {

    private final String containerId;
    private final String containerName;
    private final Instant timestamp;
    private final CpuStats cpuStats;
    private final MemoryStats memoryStats;
    private final NetworkStats networkStats;
    private final BlockIoStats blockIoStats;
    private final Integer pidsCount;

    /**
     * Constructs a new ContainerStats instance.
     *
     * @param containerId   The container ID.
     * @param containerName The container name.
     * @param timestamp     The time when the stats were collected.
     * @param cpuStats      CPU statistics.
     * @param memoryStats   Memory statistics.
     * @param networkStats  Network statistics.
     * @param blockIoStats  Block I/O statistics.
     * @param pidsCount     Number of running processes.
     */
    public ContainerStats(String containerId, String containerName, Instant timestamp,
                          CpuStats cpuStats, MemoryStats memoryStats,
                          NetworkStats networkStats, BlockIoStats blockIoStats,
                          Integer pidsCount) {
        this.containerId = containerId;
        this.containerName = containerName;
        this.timestamp = timestamp;
        this.cpuStats = cpuStats;
        this.memoryStats = memoryStats;
        this.networkStats = networkStats;
        this.blockIoStats = blockIoStats;
        this.pidsCount = pidsCount;
    }

    // Getters
    public String getContainerId() { return containerId; }
    public String getContainerName() { return containerName; }
    public Instant getTimestamp() { return timestamp; }
    public CpuStats getCpuStats() { return cpuStats; }
    public MemoryStats getMemoryStats() { return memoryStats; }
    public NetworkStats getNetworkStats() { return networkStats; }
    public BlockIoStats getBlockIoStats() { return blockIoStats; }
    public Integer getPidsCount() { return pidsCount; }

    /**
     * CPU statistics for a container.
     */
    public static class CpuStats {
        private final Long totalUsage;
        private final Long systemCpuUsage;
        private final Long userCpuUsage;
        private final Long kernelCpuUsage;
        private final Integer onlineCpus;
        private final Double cpuPercentage;
        private final Long throttledTime;
        private final Long throttledPeriods;

        public CpuStats(Long totalUsage, Long systemCpuUsage, Long userCpuUsage,
                       Long kernelCpuUsage, Integer onlineCpus, Double cpuPercentage,
                       Long throttledTime, Long throttledPeriods) {
            this.totalUsage = totalUsage;
            this.systemCpuUsage = systemCpuUsage;
            this.userCpuUsage = userCpuUsage;
            this.kernelCpuUsage = kernelCpuUsage;
            this.onlineCpus = onlineCpus;
            this.cpuPercentage = cpuPercentage;
            this.throttledTime = throttledTime;
            this.throttledPeriods = throttledPeriods;
        }

        public Long getTotalUsage() { return totalUsage; }
        public Long getSystemCpuUsage() { return systemCpuUsage; }
        public Long getUserCpuUsage() { return userCpuUsage; }
        public Long getKernelCpuUsage() { return kernelCpuUsage; }
        public Integer getOnlineCpus() { return onlineCpus; }
        public Double getCpuPercentage() { return cpuPercentage; }
        public Long getThrottledTime() { return throttledTime; }
        public Long getThrottledPeriods() { return throttledPeriods; }

        @Override
        public String toString() {
            return String.format("CpuStats{usage=%.2f%%, onlineCpus=%d, throttledTime=%d}",
                    cpuPercentage != null ? cpuPercentage : 0.0,
                    onlineCpus != null ? onlineCpus : 0,
                    throttledTime != null ? throttledTime : 0);
        }
    }

    /**
     * Memory statistics for a container.
     */
    public static class MemoryStats {
        private final Long usage;
        private final Long maxUsage;
        private final Long limit;
        private final Long cache;
        private final Long rss;
        private final Long rssHuge;
        private final Long mappedFile;
        private final Long activeAnon;
        private final Long inactiveAnon;
        private final Long activeFile;
        private final Long inactiveFile;
        private final Double usagePercentage;
        private final Long availableMemory;

        public MemoryStats(Long usage, Long maxUsage, Long limit, Long cache,
                          Long rss, Long rssHuge, Long mappedFile,
                          Long activeAnon, Long inactiveAnon,
                          Long activeFile, Long inactiveFile,
                          Double usagePercentage, Long availableMemory) {
            this.usage = usage;
            this.maxUsage = maxUsage;
            this.limit = limit;
            this.cache = cache;
            this.rss = rss;
            this.rssHuge = rssHuge;
            this.mappedFile = mappedFile;
            this.activeAnon = activeAnon;
            this.inactiveAnon = inactiveAnon;
            this.activeFile = activeFile;
            this.inactiveFile = inactiveFile;
            this.usagePercentage = usagePercentage;
            this.availableMemory = availableMemory;
        }

        public Long getUsage() { return usage; }
        public Long getMaxUsage() { return maxUsage; }
        public Long getLimit() { return limit; }
        public Long getCache() { return cache; }
        public Long getRss() { return rss; }
        public Long getRssHuge() { return rssHuge; }
        public Long getMappedFile() { return mappedFile; }
        public Long getActiveAnon() { return activeAnon; }
        public Long getInactiveAnon() { return inactiveAnon; }
        public Long getActiveFile() { return activeFile; }
        public Long getInactiveFile() { return inactiveFile; }
        public Double getUsagePercentage() { return usagePercentage; }
        public Long getAvailableMemory() { return availableMemory; }

        /**
         * Returns memory usage in megabytes.
         */
        public double getUsageMB() {
            return usage != null ? usage / (1024.0 * 1024.0) : 0.0;
        }

        /**
         * Returns memory limit in megabytes.
         */
        public double getLimitMB() {
            return limit != null ? limit / (1024.0 * 1024.0) : 0.0;
        }

        @Override
        public String toString() {
            return String.format("MemoryStats{usage=%.2fMB, limit=%.2fMB, percentage=%.2f%%}",
                    getUsageMB(), getLimitMB(),
                    usagePercentage != null ? usagePercentage : 0.0);
        }
    }

    /**
     * Network statistics for a container.
     */
    public static class NetworkStats {
        private final Long rxBytes;
        private final Long rxPackets;
        private final Long rxErrors;
        private final Long rxDropped;
        private final Long txBytes;
        private final Long txPackets;
        private final Long txErrors;
        private final Long txDropped;
        private final Map<String, InterfaceStats> interfaceStats;

        public NetworkStats(Long rxBytes, Long rxPackets, Long rxErrors, Long rxDropped,
                           Long txBytes, Long txPackets, Long txErrors, Long txDropped,
                           Map<String, InterfaceStats> interfaceStats) {
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.rxErrors = rxErrors;
            this.rxDropped = rxDropped;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.txErrors = txErrors;
            this.txDropped = txDropped;
            this.interfaceStats = interfaceStats;
        }

        public Long getRxBytes() { return rxBytes; }
        public Long getRxPackets() { return rxPackets; }
        public Long getRxErrors() { return rxErrors; }
        public Long getRxDropped() { return rxDropped; }
        public Long getTxBytes() { return txBytes; }
        public Long getTxPackets() { return txPackets; }
        public Long getTxErrors() { return txErrors; }
        public Long getTxDropped() { return txDropped; }
        public Map<String, InterfaceStats> getInterfaceStats() { return interfaceStats; }

        /**
         * Returns received bytes in megabytes.
         */
        public double getRxMB() {
            return rxBytes != null ? rxBytes / (1024.0 * 1024.0) : 0.0;
        }

        /**
         * Returns transmitted bytes in megabytes.
         */
        public double getTxMB() {
            return txBytes != null ? txBytes / (1024.0 * 1024.0) : 0.0;
        }

        @Override
        public String toString() {
            return String.format("NetworkStats{rx=%.2fMB, tx=%.2fMB, rxErrors=%d, txErrors=%d}",
                    getRxMB(), getTxMB(),
                    rxErrors != null ? rxErrors : 0,
                    txErrors != null ? txErrors : 0);
        }

        /**
         * Statistics for a specific network interface.
         */
        public static class InterfaceStats {
            private final String name;
            private final Long rxBytes;
            private final Long txBytes;

            public InterfaceStats(String name, Long rxBytes, Long txBytes) {
                this.name = name;
                this.rxBytes = rxBytes;
                this.txBytes = txBytes;
            }

            public String getName() { return name; }
            public Long getRxBytes() { return rxBytes; }
            public Long getTxBytes() { return txBytes; }
        }
    }

    /**
     * Block I/O statistics for a container.
     */
    public static class BlockIoStats {
        private final Long readBytes;
        private final Long writeBytes;
        private final Long readOps;
        private final Long writeOps;

        public BlockIoStats(Long readBytes, Long writeBytes, Long readOps, Long writeOps) {
            this.readBytes = readBytes;
            this.writeBytes = writeBytes;
            this.readOps = readOps;
            this.writeOps = writeOps;
        }

        public Long getReadBytes() { return readBytes; }
        public Long getWriteBytes() { return writeBytes; }
        public Long getReadOps() { return readOps; }
        public Long getWriteOps() { return writeOps; }

        /**
         * Returns read bytes in megabytes.
         */
        public double getReadMB() {
            return readBytes != null ? readBytes / (1024.0 * 1024.0) : 0.0;
        }

        /**
         * Returns written bytes in megabytes.
         */
        public double getWriteMB() {
            return writeBytes != null ? writeBytes / (1024.0 * 1024.0) : 0.0;
        }

        @Override
        public String toString() {
            return String.format("BlockIoStats{read=%.2fMB, write=%.2fMB, readOps=%d, writeOps=%d}",
                    getReadMB(), getWriteMB(),
                    readOps != null ? readOps : 0,
                    writeOps != null ? writeOps : 0);
        }
    }

    @Override
    public String toString() {
        return String.format("ContainerStats{id=%s, name=%s, cpu=%s, memory=%s, network=%s, blockIo=%s, pids=%d}",
                containerId != null ? containerId.substring(0, Math.min(12, containerId.length())) : "unknown",
                containerName,
                cpuStats,
                memoryStats,
                networkStats,
                blockIoStats,
                pidsCount != null ? pidsCount : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainerStats that = (ContainerStats) o;
        return Objects.equals(containerId, that.containerId) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerId, timestamp);
    }
}
