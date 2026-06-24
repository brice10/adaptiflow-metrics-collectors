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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.BlkioStatEntry;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StatisticNetworksConfig;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;
import tools.spirals.cerberus237.metricscollectorbase.metrics.docker.model.ContainerStats;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Collector for Docker container runtime statistics.
 * <p>
 * This collector retrieves real-time statistics from a Docker container including
 * CPU usage, memory consumption, network I/O, and block I/O metrics. These metrics
 * are essential for monitoring container performance and making adaptive decisions.
 * </p>
 *
 * <p>
 * The collector can operate in two modes:
 * </p>
 * <ul>
 *     <li><strong>Single-shot mode:</strong> Retrieves statistics once using {@link #get()}</li>
 *     <li><strong>Streaming mode:</strong> Continuously monitors statistics using {@link #stream(StatisticsCallback)}</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerStatsCollector collector = new ContainerStatsCollector("container_id");
 * Optional&lt;ContainerStats&gt; stats = collector.get();
 * stats.ifPresent(s -> {
 *     System.out.println("CPU Usage: " + s.getCpuStats().getCpuPercentage() + "%");
 *     System.out.println("Memory Usage: " + s.getMemoryStats().getUsageMB() + " MB");
 * });
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerStatsCollector implements IMetricsCollector<Optional<ContainerStats>> {

    private final String containerId;
    private final DockerClient dockerClient;
    private final long timeoutSeconds;
    
    // Previous stats for CPU percentage calculation
    private Long previousCpuUsage = null;
    private Long previousSystemCpuUsage = null;

    /**
     * Constructs a ContainerStatsCollector for the specified container.
     *
     * @param containerId The ID or name of the container to monitor.
     */
    public ContainerStatsCollector(String containerId) {
        this(containerId, 5);
    }

    /**
     * Constructs a ContainerStatsCollector with custom timeout.
     *
     * @param containerId    The ID or name of the container to monitor.
     * @param timeoutSeconds Timeout in seconds for statistics retrieval.
     */
    public ContainerStatsCollector(String containerId, long timeoutSeconds) {
        this.containerId = containerId;
        this.dockerClient = DockerClientProvider.getInstance().getClient();
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Retrieves the current statistics for the container.
     *
     * @return An Optional containing ContainerStats if successful.
     */
    @Override
    public Optional<ContainerStats> get() {
        return getStats();
    }

    /**
     * Retrieves container statistics with a single API call.
     *
     * @return Optional containing parsed container statistics.
     */
    public Optional<ContainerStats> getStats() {
        final AtomicReference<Statistics> statsRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            dockerClient.statsCmd(containerId)
                    .withNoStream(true)
                    .exec(new ResultCallback<Statistics>() {
                        @Override
                        public void onStart(Closeable closeable) {}

                        @Override
                        public void onNext(Statistics stats) {
                            statsRef.set(stats);
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            latch.countDown();
                        }

                        @Override
                        public void onComplete() {
                            latch.countDown();
                        }

                        @Override
                        public void close() throws IOException {}
                    });

            if (latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
                Statistics stats = statsRef.get();
                if (stats != null) {
                    return Optional.of(parseStatistics(stats));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Streams container statistics continuously.
     *
     * @param callback Callback to receive statistics updates.
     * @return A Closeable that can be used to stop the stream.
     */
    public Closeable stream(StatisticsCallback callback) {
        return dockerClient.statsCmd(containerId)
                .withNoStream(false)
                .exec(new ResultCallback<Statistics>() {
                    private Closeable stream;

                    @Override
                    public void onStart(Closeable closeable) {
                        this.stream = closeable;
                    }

                    @Override
                    public void onNext(Statistics stats) {
                        try {
                            ContainerStats containerStats = parseStatistics(stats);
                            callback.onStats(containerStats);
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }

                    @Override
                    public void close() throws IOException {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                });
    }

    /**
     * Parses Docker Statistics into ContainerStats model.
     */
    private ContainerStats parseStatistics(Statistics stats) {
        return new ContainerStats(
                containerId,
                stats.toString(),
                Instant.now(),
                parseCpuStats(stats),
                parseMemoryStats(stats),
                parseNetworkStats(stats),
                parseBlockIoStats(stats),
                stats.getPidsStats() != null ? 
                    (stats.getPidsStats().getCurrent() != null ? 
                        stats.getPidsStats().getCurrent().intValue() : null) : null
        );
    }

    /**
     * Parses CPU statistics and calculates CPU percentage.
     */
    private ContainerStats.CpuStats parseCpuStats(Statistics stats) {
        if (stats.getCpuStats() == null) {
            return new ContainerStats.CpuStats(0L, 0L, 0L, 0L, 0, 0.0, 0L, 0L);
        }

        var cpuStats = stats.getCpuStats();
        var cpuUsage = cpuStats.getCpuUsage();

        Long totalUsage = cpuUsage != null ? cpuUsage.getTotalUsage() : 0L;
        Long systemCpuUsage = cpuStats.getSystemCpuUsage();
        Long userUsage = cpuUsage != null && cpuUsage.getUsageInUsermode() != null ? 
                        cpuUsage.getUsageInUsermode() : 0L;
        Long kernelUsage = cpuUsage != null && cpuUsage.getUsageInKernelmode() != null ? 
                          cpuUsage.getUsageInKernelmode() : 0L;
        Integer onlineCpus = cpuStats.getOnlineCpus() != null ? 
                            cpuStats.getOnlineCpus().intValue() : 
                            (cpuUsage != null && cpuUsage.getPercpuUsage() != null ? 
                             cpuUsage.getPercpuUsage().size() : 1);

        // Calculate CPU percentage
        Double cpuPercentage = calculateCpuPercentage(totalUsage, systemCpuUsage, onlineCpus);

        // Throttling stats
        Long throttledTime = 0L;
        Long throttledPeriods = 0L;
        if (cpuStats.getThrottlingData() != null) {
            throttledTime = cpuStats.getThrottlingData().getThrottledTime();
            throttledPeriods = cpuStats.getThrottlingData().getThrottledPeriods();
        }

        return new ContainerStats.CpuStats(
                totalUsage,
                systemCpuUsage,
                userUsage,
                kernelUsage,
                onlineCpus,
                cpuPercentage,
                throttledTime,
                throttledPeriods
        );
    }

    /**
     * Calculates CPU percentage using delta between measurements.
     */
    private Double calculateCpuPercentage(Long currentCpuUsage, Long currentSystemCpuUsage, 
                                          Integer onlineCpus) {
        if (previousCpuUsage == null || previousSystemCpuUsage == null) {
            previousCpuUsage = currentCpuUsage;
            previousSystemCpuUsage = currentSystemCpuUsage;
            return 0.0;
        }

        long cpuDelta = currentCpuUsage - previousCpuUsage;
        long systemDelta = currentSystemCpuUsage - previousSystemCpuUsage;

        previousCpuUsage = currentCpuUsage;
        previousSystemCpuUsage = currentSystemCpuUsage;

        if (systemDelta > 0 && cpuDelta > 0) {
            return ((double) cpuDelta / systemDelta) * onlineCpus * 100.0;
        }
        return 0.0;
    }

    /**
     * Parses memory statistics.
     */
    private ContainerStats.MemoryStats parseMemoryStats(Statistics stats) {
        if (stats.getMemoryStats() == null) {
            return new ContainerStats.MemoryStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 
                                                   0L, 0L, 0L, 0L, 0.0, 0L);
        }

        var memStats = stats.getMemoryStats();
        var memStatsDetail = memStats.getStats();

        Long usage = memStats.getUsage();
        Long maxUsage = memStats.getMaxUsage();
        Long limit = memStats.getLimit();
        
        // Extract detailed stats if available
        Long cache = memStatsDetail != null ? memStatsDetail.getCache() : 0L;
        Long rss = memStatsDetail != null ? memStatsDetail.getRss() : 0L;
        Long rssHuge = memStatsDetail != null ? memStatsDetail.getRssHuge() : 0L;
        Long mappedFile = memStatsDetail != null ? memStatsDetail.getMappedFile() : 0L;
        Long activeAnon = memStatsDetail != null ? memStatsDetail.getActiveAnon() : 0L;
        Long inactiveAnon = memStatsDetail != null ? memStatsDetail.getInactiveAnon() : 0L;
        Long activeFile = memStatsDetail != null ? memStatsDetail.getActiveFile() : 0L;
        Long inactiveFile = memStatsDetail != null ? memStatsDetail.getInactiveFile() : 0L;

        // Calculate usage percentage
        Double usagePercentage = (limit != null && limit > 0 && usage != null) ? 
                                 (usage.doubleValue() / limit.doubleValue()) * 100.0 : 0.0;
        Long availableMemory = (limit != null && usage != null) ? limit - usage : 0L;

        return new ContainerStats.MemoryStats(
                usage, maxUsage, limit, cache, rss, rssHuge, mappedFile,
                activeAnon, inactiveAnon, activeFile, inactiveFile,
                usagePercentage, availableMemory
        );
    }

    /**
     * Parses network statistics from all interfaces.
     */
    private ContainerStats.NetworkStats parseNetworkStats(Statistics stats) {
        if (stats.getNetworks() == null || stats.getNetworks().isEmpty()) {
            return new ContainerStats.NetworkStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, new HashMap<>());
        }

        long totalRxBytes = 0, totalRxPackets = 0, totalRxErrors = 0, totalRxDropped = 0;
        long totalTxBytes = 0, totalTxPackets = 0, totalTxErrors = 0, totalTxDropped = 0;
        Map<String, ContainerStats.NetworkStats.InterfaceStats> interfaceStats = new HashMap<>();

        for (Map.Entry<String, StatisticNetworksConfig> entry : stats.getNetworks().entrySet()) {
            String ifName = entry.getKey();
            StatisticNetworksConfig netConfig = entry.getValue();

            if (netConfig != null) {
                totalRxBytes += netConfig.getRxBytes() != null ? netConfig.getRxBytes() : 0;
                totalRxPackets += netConfig.getRxPackets() != null ? netConfig.getRxPackets() : 0;
                totalRxErrors += netConfig.getRxErrors() != null ? netConfig.getRxErrors() : 0;
                totalRxDropped += netConfig.getRxDropped() != null ? netConfig.getRxDropped() : 0;
                totalTxBytes += netConfig.getTxBytes() != null ? netConfig.getTxBytes() : 0;
                totalTxPackets += netConfig.getTxPackets() != null ? netConfig.getTxPackets() : 0;
                totalTxErrors += netConfig.getTxErrors() != null ? netConfig.getTxErrors() : 0;
                totalTxDropped += netConfig.getTxDropped() != null ? netConfig.getTxDropped() : 0;

                interfaceStats.put(ifName, new ContainerStats.NetworkStats.InterfaceStats(
                        ifName,
                        netConfig.getRxBytes(),
                        netConfig.getTxBytes()
                ));
            }
        }

        return new ContainerStats.NetworkStats(
                totalRxBytes, totalRxPackets, totalRxErrors, totalRxDropped,
                totalTxBytes, totalTxPackets, totalTxErrors, totalTxDropped,
                interfaceStats
        );
    }

    /**
     * Parses block I/O statistics.
     */
    private ContainerStats.BlockIoStats parseBlockIoStats(Statistics stats) {
        if (stats.getBlkioStats() == null) {
            return new ContainerStats.BlockIoStats(0L, 0L, 0L, 0L);
        }

        var blkioStats = stats.getBlkioStats();
        long readBytes = 0, writeBytes = 0, readOps = 0, writeOps = 0;

        // Parse service bytes (read/write bytes)
        List<BlkioStatEntry> serviceBytes = blkioStats.getIoServiceBytesRecursive();
        if (serviceBytes != null) {
            for (BlkioStatEntry entry : serviceBytes) {
                if (entry.getOp() != null) {
                    String op = entry.getOp().toLowerCase();
                    if ("read".equals(op)) {
                        readBytes += entry.getValue() != null ? entry.getValue() : 0;
                    } else if ("write".equals(op)) {
                        writeBytes += entry.getValue() != null ? entry.getValue() : 0;
                    }
                }
            }
        }

        // Parse service operations (read/write ops)
        List<BlkioStatEntry> serviceOps = blkioStats.getIoServicedRecursive();
        if (serviceOps != null) {
            for (BlkioStatEntry entry : serviceOps) {
                if (entry.getOp() != null) {
                    String op = entry.getOp().toLowerCase();
                    if ("read".equals(op)) {
                        readOps += entry.getValue() != null ? entry.getValue() : 0;
                    } else if ("write".equals(op)) {
                        writeOps += entry.getValue() != null ? entry.getValue() : 0;
                    }
                }
            }
        }

        return new ContainerStats.BlockIoStats(readBytes, writeBytes, readOps, writeOps);
    }

    /**
     * Callback interface for streaming statistics.
     */
    public interface StatisticsCallback {
        void onStats(ContainerStats stats);
        void onError(Throwable throwable);
        void onComplete();
    }
}
