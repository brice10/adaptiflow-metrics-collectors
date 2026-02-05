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
 * Collector for Docker container network I/O metrics.
 * <p>
 * This collector provides network traffic statistics for a Docker container,
 * including bytes received/transmitted, packet counts, and error rates.
 * Network metrics are essential for understanding container communication
 * patterns and detecting network-related issues.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerNetworkCollector netCollector = new ContainerNetworkCollector("my-container");
 * ContainerStats.NetworkStats stats = netCollector.get().orElse(null);
 * 
 * if (stats != null) {
 *     System.out.println("Received: " + stats.getRxMB() + " MB");
 *     System.out.println("Transmitted: " + stats.getTxMB() + " MB");
 *     System.out.println("RX Errors: " + stats.getRxErrors());
 * }
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerNetworkCollector implements IMetricsCollector<Optional<ContainerStats.NetworkStats>> {

    private final ContainerStatsCollector statsCollector;
    private final String containerId;
    
    // Previous values for rate calculation
    private Long previousRxBytes = null;
    private Long previousTxBytes = null;
    private long previousTimestamp = 0;

    /**
     * Constructs a ContainerNetworkCollector for the specified container.
     *
     * @param containerId The ID or name of the container to monitor.
     */
    public ContainerNetworkCollector(String containerId) {
        this.containerId = containerId;
        this.statsCollector = new ContainerStatsCollector(containerId);
    }

    /**
     * Retrieves the current network statistics of the container.
     *
     * @return Optional containing NetworkStats if available.
     */
    @Override
    public Optional<ContainerStats.NetworkStats> get() {
        return getNetworkStats();
    }

    /**
     * Retrieves network statistics.
     *
     * @return Optional containing NetworkStats.
     */
    public Optional<ContainerStats.NetworkStats> getNetworkStats() {
        return statsCollector.getStats()
                .map(ContainerStats::getNetworkStats);
    }

    /**
     * Retrieves total bytes received.
     *
     * @return Bytes received.
     */
    public long getRxBytes() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getRxBytes)
                .orElse(0L);
    }

    /**
     * Retrieves total bytes transmitted.
     *
     * @return Bytes transmitted.
     */
    public long getTxBytes() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getTxBytes)
                .orElse(0L);
    }

    /**
     * Retrieves received bytes in megabytes.
     *
     * @return Received data in MB.
     */
    public double getRxMB() {
        return getRxBytes() / (1024.0 * 1024.0);
    }

    /**
     * Retrieves transmitted bytes in megabytes.
     *
     * @return Transmitted data in MB.
     */
    public double getTxMB() {
        return getTxBytes() / (1024.0 * 1024.0);
    }

    /**
     * Retrieves total packets received.
     *
     * @return Packets received.
     */
    public long getRxPackets() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getRxPackets)
                .orElse(0L);
    }

    /**
     * Retrieves total packets transmitted.
     *
     * @return Packets transmitted.
     */
    public long getTxPackets() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getTxPackets)
                .orElse(0L);
    }

    /**
     * Retrieves receive error count.
     *
     * @return Number of receive errors.
     */
    public long getRxErrors() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getRxErrors)
                .orElse(0L);
    }

    /**
     * Retrieves transmit error count.
     *
     * @return Number of transmit errors.
     */
    public long getTxErrors() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getTxErrors)
                .orElse(0L);
    }

    /**
     * Retrieves receive dropped packet count.
     *
     * @return Number of dropped receive packets.
     */
    public long getRxDropped() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getRxDropped)
                .orElse(0L);
    }

    /**
     * Retrieves transmit dropped packet count.
     *
     * @return Number of dropped transmit packets.
     */
    public long getTxDropped() {
        return getNetworkStats()
                .map(ContainerStats.NetworkStats::getTxDropped)
                .orElse(0L);
    }

    /**
     * Calculates the current receive rate in bytes per second.
     * Requires multiple calls to calculate rate.
     *
     * @return Receive rate in bytes/second.
     */
    public double getRxRateBytesPerSec() {
        long currentRx = getRxBytes();
        long currentTime = System.currentTimeMillis();
        
        if (previousRxBytes == null || previousTimestamp == 0) {
            previousRxBytes = currentRx;
            previousTimestamp = currentTime;
            return 0.0;
        }
        
        long timeDelta = currentTime - previousTimestamp;
        if (timeDelta <= 0) {
            return 0.0;
        }
        
        long bytesDelta = currentRx - previousRxBytes;
        double rate = (bytesDelta * 1000.0) / timeDelta;
        
        previousRxBytes = currentRx;
        previousTimestamp = currentTime;
        
        return rate;
    }

    /**
     * Calculates the current transmit rate in bytes per second.
     * Requires multiple calls to calculate rate.
     *
     * @return Transmit rate in bytes/second.
     */
    public double getTxRateBytesPerSec() {
        long currentTx = getTxBytes();
        long currentTime = System.currentTimeMillis();
        
        if (previousTxBytes == null) {
            previousTxBytes = currentTx;
            return 0.0;
        }
        
        long timeDelta = currentTime - previousTimestamp;
        if (timeDelta <= 0) {
            return 0.0;
        }
        
        long bytesDelta = currentTx - previousTxBytes;
        double rate = (bytesDelta * 1000.0) / timeDelta;
        
        previousTxBytes = currentTx;
        
        return rate;
    }

    /**
     * Checks if there are network errors.
     *
     * @return true if any errors exist.
     */
    public boolean hasNetworkErrors() {
        return getRxErrors() > 0 || getTxErrors() > 0;
    }

    /**
     * Returns the total error count.
     *
     * @return Total number of errors.
     */
    public long getTotalErrors() {
        return getRxErrors() + getTxErrors();
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
