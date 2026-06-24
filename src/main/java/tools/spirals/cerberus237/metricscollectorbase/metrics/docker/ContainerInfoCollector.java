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
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;
import tools.spirals.cerberus237.metricscollectorbase.metrics.docker.model.ContainerInfo;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collector for detailed Docker container information.
 * <p>
 * This collector retrieves comprehensive information about a Docker container
 * including its configuration, state, resource limits, network settings, and mounts.
 * This information is crucial for understanding the container's environment and
 * making informed adaptive decisions.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ContainerInfoCollector collector = new ContainerInfoCollector("my-container");
 * Optional&lt;ContainerInfo&gt; info = collector.get();
 * info.ifPresent(i -> {
 *     System.out.println("Container: " + i.getName());
 *     System.out.println("Image: " + i.getImage());
 *     System.out.println("State: " + i.getState());
 *     System.out.println("Memory Limit: " + i.getResourceLimits().getMemoryLimitMB() + " MB");
 * });
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerInfoCollector implements IMetricsCollector<Optional<ContainerInfo>> {

    private final String containerId;
    private final DockerClient dockerClient;

    /**
     * Constructs a ContainerInfoCollector for the specified container.
     *
     * @param containerId The ID or name of the container.
     */
    public ContainerInfoCollector(String containerId) {
        this.containerId = containerId;
        this.dockerClient = DockerClientProvider.getInstance().getClient();
    }

    /**
     * Retrieves detailed information about the container.
     *
     * @return An Optional containing ContainerInfo if successful.
     */
    @Override
    public Optional<ContainerInfo> get() {
        return getContainerInfo();
    }

    /**
     * Retrieves and parses container inspection data.
     *
     * @return Optional containing parsed container information.
     */
    public Optional<ContainerInfo> getContainerInfo() {
        try {
            InspectContainerResponse inspection = dockerClient.inspectContainerCmd(containerId).exec();
            return Optional.of(parseInspection(inspection));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Parses the Docker inspection response into ContainerInfo.
     */
    private ContainerInfo parseInspection(InspectContainerResponse inspection) {
        InspectContainerResponse.ContainerState state = inspection.getState();
        ContainerConfig config = inspection.getConfig();
        HostConfig hostConfig = inspection.getHostConfig();
        NetworkSettings networkSettings = inspection.getNetworkSettings();

        return ContainerInfo.builder()
                .id(inspection.getId())
                .name(cleanContainerName(inspection.getName()))
                .image(config != null ? config.getImage() : null)
                .imageId(inspection.getImageId())
                .state(state != null ? state.getStatus() : null)
                .status(buildStatusString(state))
                .created(parseDateTime(inspection.getCreated()))
                .startedAt(state != null ? parseDateTime(state.getStartedAt()) : null)
                .finishedAt(state != null ? parseDateTime(state.getFinishedAt()) : null)
                .exitCode(state != null ? state.getExitCodeLong() != null ? 
                         state.getExitCodeLong().intValue() : null : null)
                .running(state != null ? state.getRunning() : false)
                .paused(state != null ? state.getPaused() : false)
                .restarting(state != null ? state.getRestarting() : false)
                .resourceLimits(parseResourceLimits(hostConfig))
                .networkSettings(parseNetworkSettings(networkSettings))
                .mounts(parseMounts(inspection.getMounts()))
                .labels(config != null ? config.getLabels() : Collections.emptyMap())
                .environmentVariables(parseEnvironmentVariables(config))
                .platform(inspection.getPlatform())
                .driver(inspection.getDriver())
                .sizeRw(inspection.getSizeRw())
                .sizeRootFs(inspection.getSizeRootFs())
                .restartCount(inspection.getRestartCount())
                .healthStatus(parseHealthStatus(state))
                .build();
    }

    /**
     * Removes leading slash from container name.
     */
    private String cleanContainerName(String name) {
        if (name != null && name.startsWith("/")) {
            return name.substring(1);
        }
        return name;
    }

    /**
     * Builds a human-readable status string.
     */
    private String buildStatusString(InspectContainerResponse.ContainerState state) {
        if (state == null) return "unknown";
        
        if (Boolean.TRUE.equals(state.getRunning())) {
            return "Up";
        } else if (Boolean.TRUE.equals(state.getPaused())) {
            return "Paused";
        } else if (Boolean.TRUE.equals(state.getRestarting())) {
            return "Restarting";
        } else {
            return "Exited (" + (state.getExitCodeLong() != null ? state.getExitCodeLong() : 0) + ")";
        }
    }

    /**
     * Parses Docker timestamp string to Instant.
     */
    private Instant parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty() || 
            dateTimeStr.startsWith("0001-01-01")) {
            return null;
        }
        try {
            // Docker uses RFC3339Nano format
            return ZonedDateTime.parse(dateTimeStr, 
                    DateTimeFormatter.ISO_DATE_TIME).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses resource limits from HostConfig.
     */
    private ContainerInfo.ResourceLimits parseResourceLimits(HostConfig hostConfig) {
        if (hostConfig == null) {
            return new ContainerInfo.ResourceLimits(
                    null, null, null, null, null, null,
                    null, null, null, null, null, null
            );
        }

        return new ContainerInfo.ResourceLimits(
                hostConfig.getMemory(),
                hostConfig.getMemoryReservation(),
                hostConfig.getMemorySwap(),
                hostConfig.getCpuShares() != null ? hostConfig.getCpuShares().longValue() : null,
                hostConfig.getCpuPeriod(),
                hostConfig.getCpuQuota(),
                hostConfig.getCpusetCpus(),
                hostConfig.getCpusetMems(),
                hostConfig.getNanoCPUs(),
                hostConfig.getPidsLimit() != null ? hostConfig.getPidsLimit().intValue() : null,
                hostConfig.getBlkioWeight() != null ? hostConfig.getBlkioWeight().longValue() : null,
                hostConfig.getOomKillDisable()
        );
    }

    /**
     * Parses network settings.
     */
    private ContainerInfo.NetworkSettings parseNetworkSettings(NetworkSettings networkSettings) {
        if (networkSettings == null) {
            return new ContainerInfo.NetworkSettings(null, null, null, 
                    Collections.emptyMap(), Collections.emptyMap());
        }

        // Parse network details
        Map<String, ContainerInfo.NetworkSettings.NetworkInfo> networks = new HashMap<>();
        if (networkSettings.getNetworks() != null) {
            for (Map.Entry<String, ContainerNetwork> entry : networkSettings.getNetworks().entrySet()) {
                ContainerNetwork net = entry.getValue();
                networks.put(entry.getKey(), new ContainerInfo.NetworkSettings.NetworkInfo(
                        net.getNetworkID(),
                        net.getIpAddress(),
                        net.getGateway(),
                        net.getIpPrefixLen()
                ));
            }
        }

        // Parse port bindings
        Map<String, List<ContainerInfo.NetworkSettings.PortBinding>> ports = new HashMap<>();
        if (networkSettings.getPorts() != null) {
            for (Map.Entry<ExposedPort, Ports.Binding[]> entry : networkSettings.getPorts().getBindings().entrySet()) {
                String portKey = entry.getKey().toString();
                List<ContainerInfo.NetworkSettings.PortBinding> bindings = new ArrayList<>();
                if (entry.getValue() != null) {
                    for (Ports.Binding binding : entry.getValue()) {
                        bindings.add(new ContainerInfo.NetworkSettings.PortBinding(
                                binding.getHostIp(),
                                binding.getHostPortSpec() != null ? 
                                    Integer.parseInt(binding.getHostPortSpec()) : null
                        ));
                    }
                }
                ports.put(portKey, bindings);
            }
        }

        return new ContainerInfo.NetworkSettings(
                networkSettings.getIpAddress(),
                networkSettings.getGateway(),
                networkSettings.getMacAddress(),
                networks,
                ports
        );
    }

    /**
     * Parses mount information.
     */
    private List<ContainerInfo.Mount> parseMounts(List<InspectContainerResponse.Mount> mounts) {
        if (mounts == null) {
            return Collections.emptyList();
        }

        return mounts.stream()
                .map(m -> new ContainerInfo.Mount(
                        m.getName() != null ? m.getName() : null,
                        m.getSource(),
                        m.getDestination() != null ? m.getDestination().getPath() : null,
                        m.getMode(),
                        m.getRW(),
                        m.getDriver() != null ? m.getDriver() : null
                ))
                .collect(Collectors.toList());
    }

    /**
     * Parses environment variables from container config.
     */
    private Map<String, String> parseEnvironmentVariables(ContainerConfig config) {
        if (config == null || config.getEnv() == null) {
            return Collections.emptyMap();
        }

        Map<String, String> envVars = new HashMap<>();
        for (String env : config.getEnv()) {
            int idx = env.indexOf('=');
            if (idx > 0) {
                envVars.put(env.substring(0, idx), env.substring(idx + 1));
            }
        }
        return envVars;
    }

    /**
     * Parses health check status.
     */
    private String parseHealthStatus(InspectContainerResponse.ContainerState state) {
        if (state == null || state.getHealth() == null) {
            return "none";
        }
        return state.getHealth().getStatus();
    }
}
