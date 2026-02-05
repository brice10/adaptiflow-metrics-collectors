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
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.SwarmInfo;
import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;
import tools.spirals.cerberus237.metricscollectorbase.metrics.docker.model.DockerHostInfo;

import java.util.Collections;
import java.util.Optional;

/**
 * Collector for Docker daemon and host information.
 * <p>
 * This collector retrieves comprehensive information about the Docker daemon
 * and the host system, including Docker version, available resources,
 * container counts, and Swarm status. This information is essential for
 * understanding the platform capabilities and making deployment decisions.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * DockerHostInfoCollector collector = new DockerHostInfoCollector();
 * Optional&lt;DockerHostInfo&gt; info = collector.get();
 * info.ifPresent(i -> {
 *     System.out.println("Docker Version: " + i.getDockerVersion());
 *     System.out.println("CPUs: " + i.getCpus());
 *     System.out.println("Memory: " + i.getMemoryTotalGB() + " GB");
 *     System.out.println("Running Containers: " + i.getContainersRunning());
 * });
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class DockerHostInfoCollector implements IMetricsCollector<Optional<DockerHostInfo>> {

    private final DockerClient dockerClient;

    /**
     * Constructs a DockerHostInfoCollector using the default Docker client.
     */
    public DockerHostInfoCollector() {
        this.dockerClient = DockerClientProvider.getInstance().getClient();
    }

    /**
     * Retrieves information about the Docker daemon and host.
     *
     * @return An Optional containing DockerHostInfo if successful.
     */
    @Override
    public Optional<DockerHostInfo> get() {
        return getHostInfo();
    }

    /**
     * Retrieves and parses Docker daemon information.
     *
     * @return Optional containing parsed host information.
     */
    public Optional<DockerHostInfo> getHostInfo() {
        try {
            Info info = dockerClient.infoCmd().exec();
            return Optional.of(parseInfo(info));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Parses Docker Info into DockerHostInfo model.
     */
    private DockerHostInfo parseInfo(Info info) {
        return DockerHostInfo.builder()
                .dockerVersion(info.getServerVersion())
                .apiVersion(info.getServerVersion())
                .os(info.getOsType())
                .arch(info.getArchitecture())
                .kernelVersion(info.getKernelVersion())
                .operatingSystem(info.getOperatingSystem())
                .osType(info.getOsType())
                .osVersion(info.getKernelVersion())
                .hostname(info.getName())
                .cpus(info.getNCPU())
                .memoryTotal(info.getMemTotal())
                .storageDriver(info.getDriver())
                .loggingDriver(info.getLoggingDriver())
                .cgroupDriver(info.getDriver())
                .containersTotal(info.getContainers())
                .containersRunning(info.getContainersRunning())
                .containersPaused(info.getContainersPaused())
                .containersStopped(info.getContainersStopped())
                .imagesTotal(info.getImages())
                .dockerRootDir(info.getDockerRootDir())
                .experimentalBuild(info.getExperimentalBuild())
                .isolation(info.getIsolation() != null ? info.getIsolation() : null)
                .securityOptions(info.getSecurityOptions() != null ? 
                        info.getSecurityOptions() : Collections.emptyList())
                .serverVersion(info.getServerVersion())
                .clusterStore(info.getClusterStore())
                .clusterAdvertise(info.getClusterAdvertise())
                .swarmInfo(parseSwarmInfo(info.getSwarm()))
                .build();
    }

    /**
     * Parses Swarm cluster information.
     */
    private DockerHostInfo.SwarmInfo parseSwarmInfo(SwarmInfo swarm) {
        if (swarm == null) {
            return null;
        }

        return new DockerHostInfo.SwarmInfo(
                swarm.getNodeID(),
                swarm.getNodeAddr(),
                swarm.getLocalNodeState() != null ? swarm.getLocalNodeState().getValue() : null,
                swarm.getControlAvailable(),
                swarm.getError(),
                swarm.getManagers(),
                swarm.getNodes()
        );
    }

    /**
     * Checks if Docker is running in Swarm mode.
     *
     * @return true if Swarm mode is active.
     */
    public boolean isSwarmActive() {
        return getHostInfo()
                .map(DockerHostInfo::getSwarmInfo)
                .map(DockerHostInfo.SwarmInfo::isSwarmActive)
                .orElse(false);
    }

    /**
     * Returns the Docker version string.
     *
     * @return The Docker version or "unknown" if unavailable.
     */
    public String getDockerVersion() {
        return getHostInfo()
                .map(DockerHostInfo::getDockerVersion)
                .orElse("unknown");
    }

    /**
     * Returns the total number of CPUs available to Docker.
     *
     * @return The number of CPUs or 0 if unavailable.
     */
    public int getAvailableCpus() {
        return getHostInfo()
                .map(DockerHostInfo::getCpus)
                .orElse(0);
    }

    /**
     * Returns the total memory available to Docker in bytes.
     *
     * @return The total memory or 0 if unavailable.
     */
    public long getTotalMemory() {
        return getHostInfo()
                .map(DockerHostInfo::getMemoryTotal)
                .orElse(0L);
    }

    /**
     * Returns the count of currently running containers.
     *
     * @return The number of running containers or 0 if unavailable.
     */
    public int getRunningContainersCount() {
        return getHostInfo()
                .map(DockerHostInfo::getContainersRunning)
                .orElse(0);
    }
}
