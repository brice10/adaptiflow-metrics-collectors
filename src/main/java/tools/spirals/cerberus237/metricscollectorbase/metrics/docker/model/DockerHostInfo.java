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

import java.util.List;
import java.util.Objects;

/**
 * Data model representing Docker daemon and host information.
 * <p>
 * This class contains comprehensive information about the Docker daemon,
 * including version, system resources, and runtime configuration.
 * </p>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class DockerHostInfo {

    private final String dockerVersion;
    private final String apiVersion;
    private final String minApiVersion;
    private final String gitCommit;
    private final String goVersion;
    private final String os;
    private final String arch;
    private final String kernelVersion;
    private final String operatingSystem;
    private final String osType;
    private final String osVersion;
    private final String hostname;
    private final Integer cpus;
    private final Long memoryTotal;
    private final String storageDriver;
    private final String loggingDriver;
    private final String cgroupDriver;
    private final String cgroupVersion;
    private final Integer containersTotal;
    private final Integer containersRunning;
    private final Integer containersPaused;
    private final Integer containersStopped;
    private final Integer imagesTotal;
    private final String dockerRootDir;
    private final Boolean experimentalBuild;
    private final Boolean liveRestoreEnabled;
    private final String isolation;
    private final List<String> securityOptions;
    private final String serverVersion;
    private final String clusterStore;
    private final String clusterAdvertise;
    private final SwarmInfo swarmInfo;

    /**
     * Builder pattern for DockerHostInfo.
     */
    public static class Builder {
        private String dockerVersion;
        private String apiVersion;
        private String minApiVersion;
        private String gitCommit;
        private String goVersion;
        private String os;
        private String arch;
        private String kernelVersion;
        private String operatingSystem;
        private String osType;
        private String osVersion;
        private String hostname;
        private Integer cpus;
        private Long memoryTotal;
        private String storageDriver;
        private String loggingDriver;
        private String cgroupDriver;
        private String cgroupVersion;
        private Integer containersTotal;
        private Integer containersRunning;
        private Integer containersPaused;
        private Integer containersStopped;
        private Integer imagesTotal;
        private String dockerRootDir;
        private Boolean experimentalBuild;
        private Boolean liveRestoreEnabled;
        private String isolation;
        private List<String> securityOptions;
        private String serverVersion;
        private String clusterStore;
        private String clusterAdvertise;
        private SwarmInfo swarmInfo;

        public Builder dockerVersion(String v) { this.dockerVersion = v; return this; }
        public Builder apiVersion(String v) { this.apiVersion = v; return this; }
        public Builder minApiVersion(String v) { this.minApiVersion = v; return this; }
        public Builder gitCommit(String v) { this.gitCommit = v; return this; }
        public Builder goVersion(String v) { this.goVersion = v; return this; }
        public Builder os(String v) { this.os = v; return this; }
        public Builder arch(String v) { this.arch = v; return this; }
        public Builder kernelVersion(String v) { this.kernelVersion = v; return this; }
        public Builder operatingSystem(String v) { this.operatingSystem = v; return this; }
        public Builder osType(String v) { this.osType = v; return this; }
        public Builder osVersion(String v) { this.osVersion = v; return this; }
        public Builder hostname(String v) { this.hostname = v; return this; }
        public Builder cpus(Integer v) { this.cpus = v; return this; }
        public Builder memoryTotal(Long v) { this.memoryTotal = v; return this; }
        public Builder storageDriver(String v) { this.storageDriver = v; return this; }
        public Builder loggingDriver(String v) { this.loggingDriver = v; return this; }
        public Builder cgroupDriver(String v) { this.cgroupDriver = v; return this; }
        public Builder cgroupVersion(String v) { this.cgroupVersion = v; return this; }
        public Builder containersTotal(Integer v) { this.containersTotal = v; return this; }
        public Builder containersRunning(Integer v) { this.containersRunning = v; return this; }
        public Builder containersPaused(Integer v) { this.containersPaused = v; return this; }
        public Builder containersStopped(Integer v) { this.containersStopped = v; return this; }
        public Builder imagesTotal(Integer v) { this.imagesTotal = v; return this; }
        public Builder dockerRootDir(String v) { this.dockerRootDir = v; return this; }
        public Builder experimentalBuild(Boolean v) { this.experimentalBuild = v; return this; }
        public Builder liveRestoreEnabled(Boolean v) { this.liveRestoreEnabled = v; return this; }
        public Builder isolation(String v) { this.isolation = v; return this; }
        public Builder securityOptions(List<String> v) { this.securityOptions = v; return this; }
        public Builder serverVersion(String v) { this.serverVersion = v; return this; }
        public Builder clusterStore(String v) { this.clusterStore = v; return this; }
        public Builder clusterAdvertise(String v) { this.clusterAdvertise = v; return this; }
        public Builder swarmInfo(SwarmInfo v) { this.swarmInfo = v; return this; }

        public DockerHostInfo build() {
            return new DockerHostInfo(this);
        }
    }

    private DockerHostInfo(Builder builder) {
        this.dockerVersion = builder.dockerVersion;
        this.apiVersion = builder.apiVersion;
        this.minApiVersion = builder.minApiVersion;
        this.gitCommit = builder.gitCommit;
        this.goVersion = builder.goVersion;
        this.os = builder.os;
        this.arch = builder.arch;
        this.kernelVersion = builder.kernelVersion;
        this.operatingSystem = builder.operatingSystem;
        this.osType = builder.osType;
        this.osVersion = builder.osVersion;
        this.hostname = builder.hostname;
        this.cpus = builder.cpus;
        this.memoryTotal = builder.memoryTotal;
        this.storageDriver = builder.storageDriver;
        this.loggingDriver = builder.loggingDriver;
        this.cgroupDriver = builder.cgroupDriver;
        this.cgroupVersion = builder.cgroupVersion;
        this.containersTotal = builder.containersTotal;
        this.containersRunning = builder.containersRunning;
        this.containersPaused = builder.containersPaused;
        this.containersStopped = builder.containersStopped;
        this.imagesTotal = builder.imagesTotal;
        this.dockerRootDir = builder.dockerRootDir;
        this.experimentalBuild = builder.experimentalBuild;
        this.liveRestoreEnabled = builder.liveRestoreEnabled;
        this.isolation = builder.isolation;
        this.securityOptions = builder.securityOptions;
        this.serverVersion = builder.serverVersion;
        this.clusterStore = builder.clusterStore;
        this.clusterAdvertise = builder.clusterAdvertise;
        this.swarmInfo = builder.swarmInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getDockerVersion() { return dockerVersion; }
    public String getApiVersion() { return apiVersion; }
    public String getMinApiVersion() { return minApiVersion; }
    public String getGitCommit() { return gitCommit; }
    public String getGoVersion() { return goVersion; }
    public String getOs() { return os; }
    public String getArch() { return arch; }
    public String getKernelVersion() { return kernelVersion; }
    public String getOperatingSystem() { return operatingSystem; }
    public String getOsType() { return osType; }
    public String getOsVersion() { return osVersion; }
    public String getHostname() { return hostname; }
    public Integer getCpus() { return cpus; }
    public Long getMemoryTotal() { return memoryTotal; }
    public String getStorageDriver() { return storageDriver; }
    public String getLoggingDriver() { return loggingDriver; }
    public String getCgroupDriver() { return cgroupDriver; }
    public String getCgroupVersion() { return cgroupVersion; }
    public Integer getContainersTotal() { return containersTotal; }
    public Integer getContainersRunning() { return containersRunning; }
    public Integer getContainersPaused() { return containersPaused; }
    public Integer getContainersStopped() { return containersStopped; }
    public Integer getImagesTotal() { return imagesTotal; }
    public String getDockerRootDir() { return dockerRootDir; }
    public Boolean isExperimentalBuild() { return experimentalBuild; }
    public Boolean isLiveRestoreEnabled() { return liveRestoreEnabled; }
    public String getIsolation() { return isolation; }
    public List<String> getSecurityOptions() { return securityOptions; }
    public String getServerVersion() { return serverVersion; }
    public String getClusterStore() { return clusterStore; }
    public String getClusterAdvertise() { return clusterAdvertise; }
    public SwarmInfo getSwarmInfo() { return swarmInfo; }

    /**
     * Returns total memory in gigabytes.
     */
    public double getMemoryTotalGB() {
        return memoryTotal != null ? memoryTotal / (1024.0 * 1024.0 * 1024.0) : 0.0;
    }

    /**
     * Swarm cluster information.
     */
    public static class SwarmInfo {
        private final String nodeId;
        private final String nodeAddr;
        private final String localNodeState;
        private final Boolean controlAvailable;
        private final String error;
        private final Integer managers;
        private final Integer nodes;

        public SwarmInfo(String nodeId, String nodeAddr, String localNodeState,
                        Boolean controlAvailable, String error, Integer managers, Integer nodes) {
            this.nodeId = nodeId;
            this.nodeAddr = nodeAddr;
            this.localNodeState = localNodeState;
            this.controlAvailable = controlAvailable;
            this.error = error;
            this.managers = managers;
            this.nodes = nodes;
        }

        public String getNodeId() { return nodeId; }
        public String getNodeAddr() { return nodeAddr; }
        public String getLocalNodeState() { return localNodeState; }
        public Boolean isControlAvailable() { return controlAvailable; }
        public String getError() { return error; }
        public Integer getManagers() { return managers; }
        public Integer getNodes() { return nodes; }

        public boolean isSwarmActive() {
            return "active".equalsIgnoreCase(localNodeState);
        }
    }

    @Override
    public String toString() {
        return String.format("DockerHostInfo{version=%s, os=%s, arch=%s, cpus=%d, memory=%.2fGB, containers=%d/%d running}",
                dockerVersion, operatingSystem, arch, cpus != null ? cpus : 0,
                getMemoryTotalGB(),
                containersRunning != null ? containersRunning : 0,
                containersTotal != null ? containersTotal : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DockerHostInfo that = (DockerHostInfo) o;
        return Objects.equals(hostname, that.hostname) &&
               Objects.equals(dockerVersion, that.dockerVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, dockerVersion);
    }
}
