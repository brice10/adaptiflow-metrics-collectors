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
import com.github.dockerjava.api.model.Container;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collector for listing Docker containers.
 * <p>
 * This collector retrieves a list of Docker containers with optional filtering
 * capabilities. It supports filtering by state (running, stopped, all) and by labels.
 * This information is useful for service discovery and container orchestration
 * in adaptive systems.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * // List all running containers
 * ContainerListCollector collector = new ContainerListCollector();
 * List&lt;ContainerListCollector.ContainerSummary&gt; containers = collector.get();
 * 
 * // List all containers including stopped ones
 * ContainerListCollector allCollector = new ContainerListCollector(true);
 * List&lt;ContainerListCollector.ContainerSummary&gt; allContainers = allCollector.get();
 * 
 * // List containers with specific label
 * ContainerListCollector filtered = new ContainerListCollector()
 *         .withLabel("app", "myapp");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class ContainerListCollector implements IMetricsCollector<List<ContainerListCollector.ContainerSummary>> {

    private final DockerClient dockerClient;
    private final boolean showAll;
    private Map<String, String> labelFilter;
    private String nameFilter;
    private String ancestorFilter;
    private List<String> statusFilter;

    /**
     * Constructs a ContainerListCollector that lists only running containers.
     */
    public ContainerListCollector() {
        this(false);
    }

    /**
     * Constructs a ContainerListCollector with specified filter.
     *
     * @param showAll If true, includes stopped containers.
     */
    public ContainerListCollector(boolean showAll) {
        this.dockerClient = DockerClientProvider.getInstance().getClient();
        this.showAll = showAll;
    }

    /**
     * Adds a label filter.
     *
     * @param key   Label key.
     * @param value Label value.
     * @return This collector for method chaining.
     */
    public ContainerListCollector withLabel(String key, String value) {
        if (this.labelFilter == null) {
            this.labelFilter = new java.util.HashMap<>();
        }
        this.labelFilter.put(key, value);
        return this;
    }

    /**
     * Adds a name filter (supports regex).
     *
     * @param name Container name pattern.
     * @return This collector for method chaining.
     */
    public ContainerListCollector withName(String name) {
        this.nameFilter = name;
        return this;
    }

    /**
     * Adds an ancestor (image) filter.
     *
     * @param ancestor Image name or ID.
     * @return This collector for method chaining.
     */
    public ContainerListCollector withAncestor(String ancestor) {
        this.ancestorFilter = ancestor;
        return this;
    }

    /**
     * Adds status filter.
     *
     * @param statuses List of statuses (created, restarting, running, removing, paused, exited, dead).
     * @return This collector for method chaining.
     */
    public ContainerListCollector withStatus(String... statuses) {
        this.statusFilter = Arrays.asList(statuses);
        return this;
    }

    /**
     * Retrieves the list of containers.
     *
     * @return List of ContainerSummary objects.
     */
    @Override
    public List<ContainerSummary> get() {
        return getContainers();
    }

    /**
     * Retrieves containers with applied filters.
     *
     * @return List of container summaries.
     */
    public List<ContainerSummary> getContainers() {
        try {
            var cmd = dockerClient.listContainersCmd()
                    .withShowAll(showAll);

            if (labelFilter != null && !labelFilter.isEmpty()) {
                for (Map.Entry<String, String> entry : labelFilter.entrySet()) {
                    cmd.withLabelFilter(Collections.singletonList(entry.getKey() + "=" + entry.getValue()));
                }
            }

            if (nameFilter != null) {
                cmd.withNameFilter(Collections.singletonList(nameFilter));
            }

            if (ancestorFilter != null) {
                cmd.withAncestorFilter(Collections.singletonList(ancestorFilter));
            }

            if (statusFilter != null && !statusFilter.isEmpty()) {
                cmd.withStatusFilter(statusFilter);
            }

            List<Container> containers = cmd.exec();
            return containers.stream()
                    .map(this::toSummary)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Returns the count of containers matching the filter.
     *
     * @return Number of containers.
     */
    public int getCount() {
        return getContainers().size();
    }

    /**
     * Converts Docker Container to ContainerSummary.
     */
    private ContainerSummary toSummary(Container container) {
        return new ContainerSummary(
                container.getId(),
                container.getNames() != null && container.getNames().length > 0 ?
                        cleanName(container.getNames()[0]) : null,
                container.getImage(),
                container.getImageId(),
                container.getCommand(),
                container.getCreated(),
                container.getStatus(),
                container.getState(),
                container.getPorts() != null ? Arrays.stream(container.getPorts())
                        .map(p -> new PortMapping(
                                p.getPrivatePort(),
                                p.getPublicPort(),
                                p.getType(),
                                p.getIp()
                        ))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                container.getLabels() != null ? container.getLabels() : Collections.emptyMap(),
                container.getSizeRw(),
                container.getSizeRootFs(),
                container.getNetworkSettings() != null && container.getNetworkSettings().getNetworks() != null ?
                        container.getNetworkSettings().getNetworks().keySet().stream()
                                .collect(Collectors.toList()) : Collections.emptyList()
        );
    }

    private String cleanName(String name) {
        return name != null && name.startsWith("/") ? name.substring(1) : name;
    }

    /**
     * Summary information for a container.
     */
    public static class ContainerSummary {
        private final String id;
        private final String name;
        private final String image;
        private final String imageId;
        private final String command;
        private final Long created;
        private final String status;
        private final String state;
        private final List<PortMapping> ports;
        private final Map<String, String> labels;
        private final Long sizeRw;
        private final Long sizeRootFs;
        private final List<String> networks;

        public ContainerSummary(String id, String name, String image, String imageId,
                               String command, Long created, String status, String state,
                               List<PortMapping> ports, Map<String, String> labels,
                               Long sizeRw, Long sizeRootFs, List<String> networks) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.imageId = imageId;
            this.command = command;
            this.created = created;
            this.status = status;
            this.state = state;
            this.ports = ports;
            this.labels = labels;
            this.sizeRw = sizeRw;
            this.sizeRootFs = sizeRootFs;
            this.networks = networks;
        }

        public String getId() { return id; }
        public String getShortId() { return id != null && id.length() > 12 ? id.substring(0, 12) : id; }
        public String getName() { return name; }
        public String getImage() { return image; }
        public String getImageId() { return imageId; }
        public String getCommand() { return command; }
        public Long getCreated() { return created; }
        public String getStatus() { return status; }
        public String getState() { return state; }
        public List<PortMapping> getPorts() { return ports; }
        public Map<String, String> getLabels() { return labels; }
        public Long getSizeRw() { return sizeRw; }
        public Long getSizeRootFs() { return sizeRootFs; }
        public List<String> getNetworks() { return networks; }

        public boolean isRunning() {
            return "running".equalsIgnoreCase(state);
        }

        @Override
        public String toString() {
            return String.format("Container{id=%s, name=%s, image=%s, state=%s, status=%s}",
                    getShortId(), name, image, state, status);
        }
    }

    /**
     * Port mapping information.
     */
    public static class PortMapping {
        private final Integer privatePort;
        private final Integer publicPort;
        private final String type;
        private final String ip;

        public PortMapping(Integer privatePort, Integer publicPort, String type, String ip) {
            this.privatePort = privatePort;
            this.publicPort = publicPort;
            this.type = type;
            this.ip = ip;
        }

        public Integer getPrivatePort() { return privatePort; }
        public Integer getPublicPort() { return publicPort; }
        public String getType() { return type; }
        public String getIp() { return ip; }

        @Override
        public String toString() {
            return publicPort != null ?
                    String.format("%s:%d->%d/%s", ip != null ? ip : "0.0.0.0", publicPort, privatePort, type) :
                    String.format("%d/%s", privatePort, type);
        }
    }
}
