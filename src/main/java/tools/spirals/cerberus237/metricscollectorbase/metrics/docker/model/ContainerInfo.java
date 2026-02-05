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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data model representing detailed Docker container information.
 * <p>
 * This class contains comprehensive information about a Docker container
 * including its configuration, state, resource limits, and network settings.
 * </p>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerInfo {

    private final String id;
    private final String name;
    private final String image;
    private final String imageId;
    private final String state;
    private final String status;
    private final Instant created;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final Integer exitCode;
    private final Boolean running;
    private final Boolean paused;
    private final Boolean restarting;
    private final ResourceLimits resourceLimits;
    private final NetworkSettings networkSettings;
    private final List<Mount> mounts;
    private final Map<String, String> labels;
    private final Map<String, String> environmentVariables;
    private final String platform;
    private final String driver;
    private final Integer sizeRw;
    private final Integer sizeRootFs;
    private final Integer restartCount;
    private final String healthStatus;

    /**
     * Builder pattern for ContainerInfo.
     */
    public static class Builder {
        private String id;
        private String name;
        private String image;
        private String imageId;
        private String state;
        private String status;
        private Instant created;
        private Instant startedAt;
        private Instant finishedAt;
        private Integer exitCode;
        private Boolean running;
        private Boolean paused;
        private Boolean restarting;
        private ResourceLimits resourceLimits;
        private NetworkSettings networkSettings;
        private List<Mount> mounts;
        private Map<String, String> labels;
        private Map<String, String> environmentVariables;
        private String platform;
        private String driver;
        private Integer sizeRw;
        private Integer sizeRootFs;
        private Integer restartCount;
        private String healthStatus;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder image(String image) { this.image = image; return this; }
        public Builder imageId(String imageId) { this.imageId = imageId; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder created(Instant created) { this.created = created; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder finishedAt(Instant finishedAt) { this.finishedAt = finishedAt; return this; }
        public Builder exitCode(Integer exitCode) { this.exitCode = exitCode; return this; }
        public Builder running(Boolean running) { this.running = running; return this; }
        public Builder paused(Boolean paused) { this.paused = paused; return this; }
        public Builder restarting(Boolean restarting) { this.restarting = restarting; return this; }
        public Builder resourceLimits(ResourceLimits resourceLimits) { this.resourceLimits = resourceLimits; return this; }
        public Builder networkSettings(NetworkSettings networkSettings) { this.networkSettings = networkSettings; return this; }
        public Builder mounts(List<Mount> mounts) { this.mounts = mounts; return this; }
        public Builder labels(Map<String, String> labels) { this.labels = labels; return this; }
        public Builder environmentVariables(Map<String, String> env) { this.environmentVariables = env; return this; }
        public Builder platform(String platform) { this.platform = platform; return this; }
        public Builder driver(String driver) { this.driver = driver; return this; }
        public Builder sizeRw(Integer sizeRw) { this.sizeRw = sizeRw; return this; }
        public Builder sizeRootFs(Integer sizeRootFs) { this.sizeRootFs = sizeRootFs; return this; }
        public Builder restartCount(Integer restartCount) { this.restartCount = restartCount; return this; }
        public Builder healthStatus(String healthStatus) { this.healthStatus = healthStatus; return this; }

        public ContainerInfo build() {
            return new ContainerInfo(this);
        }
    }

    private ContainerInfo(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.image = builder.image;
        this.imageId = builder.imageId;
        this.state = builder.state;
        this.status = builder.status;
        this.created = builder.created;
        this.startedAt = builder.startedAt;
        this.finishedAt = builder.finishedAt;
        this.exitCode = builder.exitCode;
        this.running = builder.running;
        this.paused = builder.paused;
        this.restarting = builder.restarting;
        this.resourceLimits = builder.resourceLimits;
        this.networkSettings = builder.networkSettings;
        this.mounts = builder.mounts;
        this.labels = builder.labels;
        this.environmentVariables = builder.environmentVariables;
        this.platform = builder.platform;
        this.driver = builder.driver;
        this.sizeRw = builder.sizeRw;
        this.sizeRootFs = builder.sizeRootFs;
        this.restartCount = builder.restartCount;
        this.healthStatus = builder.healthStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getId() { return id; }
    public String getShortId() { return id != null && id.length() > 12 ? id.substring(0, 12) : id; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public String getImageId() { return imageId; }
    public String getState() { return state; }
    public String getStatus() { return status; }
    public Instant getCreated() { return created; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public Integer getExitCode() { return exitCode; }
    public Boolean isRunning() { return running; }
    public Boolean isPaused() { return paused; }
    public Boolean isRestarting() { return restarting; }
    public ResourceLimits getResourceLimits() { return resourceLimits; }
    public NetworkSettings getNetworkSettings() { return networkSettings; }
    public List<Mount> getMounts() { return mounts; }
    public Map<String, String> getLabels() { return labels; }
    public Map<String, String> getEnvironmentVariables() { return environmentVariables; }
    public String getPlatform() { return platform; }
    public String getDriver() { return driver; }
    public Integer getSizeRw() { return sizeRw; }
    public Integer getSizeRootFs() { return sizeRootFs; }
    public Integer getRestartCount() { return restartCount; }
    public String getHealthStatus() { return healthStatus; }

    /**
     * Resource limits configuration for a container.
     */
    public static class ResourceLimits {
        private final Long memoryLimit;
        private final Long memoryReservation;
        private final Long memorySwap;
        private final Long cpuShares;
        private final Long cpuPeriod;
        private final Long cpuQuota;
        private final String cpusetCpus;
        private final String cpusetMems;
        private final Long nanoCpus;
        private final Integer pidsLimit;
        private final Long blkioWeight;
        private final Boolean oomKillDisable;

        public ResourceLimits(Long memoryLimit, Long memoryReservation, Long memorySwap,
                             Long cpuShares, Long cpuPeriod, Long cpuQuota,
                             String cpusetCpus, String cpusetMems, Long nanoCpus,
                             Integer pidsLimit, Long blkioWeight, Boolean oomKillDisable) {
            this.memoryLimit = memoryLimit;
            this.memoryReservation = memoryReservation;
            this.memorySwap = memorySwap;
            this.cpuShares = cpuShares;
            this.cpuPeriod = cpuPeriod;
            this.cpuQuota = cpuQuota;
            this.cpusetCpus = cpusetCpus;
            this.cpusetMems = cpusetMems;
            this.nanoCpus = nanoCpus;
            this.pidsLimit = pidsLimit;
            this.blkioWeight = blkioWeight;
            this.oomKillDisable = oomKillDisable;
        }

        public Long getMemoryLimit() { return memoryLimit; }
        public Long getMemoryReservation() { return memoryReservation; }
        public Long getMemorySwap() { return memorySwap; }
        public Long getCpuShares() { return cpuShares; }
        public Long getCpuPeriod() { return cpuPeriod; }
        public Long getCpuQuota() { return cpuQuota; }
        public String getCpusetCpus() { return cpusetCpus; }
        public String getCpusetMems() { return cpusetMems; }
        public Long getNanoCpus() { return nanoCpus; }
        public Integer getPidsLimit() { return pidsLimit; }
        public Long getBlkioWeight() { return blkioWeight; }
        public Boolean isOomKillDisable() { return oomKillDisable; }

        public double getMemoryLimitMB() {
            return memoryLimit != null ? memoryLimit / (1024.0 * 1024.0) : 0.0;
        }

        public double getCpuLimit() {
            if (nanoCpus != null && nanoCpus > 0) {
                return nanoCpus / 1_000_000_000.0;
            }
            if (cpuQuota != null && cpuPeriod != null && cpuPeriod > 0) {
                return (double) cpuQuota / cpuPeriod;
            }
            return 0.0;
        }

        @Override
        public String toString() {
            return String.format("ResourceLimits{memoryLimit=%.2fMB, cpuLimit=%.2f}",
                    getMemoryLimitMB(), getCpuLimit());
        }
    }

    /**
     * Network settings for a container.
     */
    public static class NetworkSettings {
        private final String ipAddress;
        private final String gateway;
        private final String macAddress;
        private final Map<String, NetworkInfo> networks;
        private final Map<String, List<PortBinding>> ports;

        public NetworkSettings(String ipAddress, String gateway, String macAddress,
                              Map<String, NetworkInfo> networks, Map<String, List<PortBinding>> ports) {
            this.ipAddress = ipAddress;
            this.gateway = gateway;
            this.macAddress = macAddress;
            this.networks = networks;
            this.ports = ports;
        }

        public String getIpAddress() { return ipAddress; }
        public String getGateway() { return gateway; }
        public String getMacAddress() { return macAddress; }
        public Map<String, NetworkInfo> getNetworks() { return networks; }
        public Map<String, List<PortBinding>> getPorts() { return ports; }

        public static class NetworkInfo {
            private final String networkId;
            private final String ipAddress;
            private final String gateway;
            private final Integer ipPrefixLen;

            public NetworkInfo(String networkId, String ipAddress, String gateway, Integer ipPrefixLen) {
                this.networkId = networkId;
                this.ipAddress = ipAddress;
                this.gateway = gateway;
                this.ipPrefixLen = ipPrefixLen;
            }

            public String getNetworkId() { return networkId; }
            public String getIpAddress() { return ipAddress; }
            public String getGateway() { return gateway; }
            public Integer getIpPrefixLen() { return ipPrefixLen; }
        }

        public static class PortBinding {
            private final String hostIp;
            private final Integer hostPort;

            public PortBinding(String hostIp, Integer hostPort) {
                this.hostIp = hostIp;
                this.hostPort = hostPort;
            }

            public String getHostIp() { return hostIp; }
            public Integer getHostPort() { return hostPort; }
        }
    }

    /**
     * Mount point information for a container.
     */
    public static class Mount {
        private final String type;
        private final String source;
        private final String destination;
        private final String mode;
        private final Boolean rw;
        private final String propagation;

        public Mount(String type, String source, String destination, String mode,
                    Boolean rw, String propagation) {
            this.type = type;
            this.source = source;
            this.destination = destination;
            this.mode = mode;
            this.rw = rw;
            this.propagation = propagation;
        }

        public String getType() { return type; }
        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public String getMode() { return mode; }
        public Boolean isReadWrite() { return rw; }
        public String getPropagation() { return propagation; }
    }

    @Override
    public String toString() {
        return String.format("ContainerInfo{id=%s, name=%s, image=%s, state=%s, status=%s}",
                getShortId(), name, image, state, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainerInfo that = (ContainerInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
