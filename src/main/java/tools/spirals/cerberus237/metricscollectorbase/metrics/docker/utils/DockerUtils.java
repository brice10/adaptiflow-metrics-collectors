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
package tools.spirals.cerberus237.metricscollectorbase.metrics.docker.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import tools.spirals.cerberus237.metricscollectorbase.metrics.docker.DockerClientProvider;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class providing helper methods for Docker operations.
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public final class DockerUtils {

    private static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";

    private DockerUtils() {
    }

    public static DockerClient createDefaultClient() {
        return createClient(DEFAULT_DOCKER_HOST);
    }

    public static DockerClient createClient(String dockerHost) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    public static boolean isDockerAvailable(DockerClient client) {
        try {
            client.pingCmd().exec();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Container> listRunningContainers(DockerClient client) {
        return client.listContainersCmd().withShowAll(false).exec();
    }

    public static List<Container> listAllContainers(DockerClient client) {
        return client.listContainersCmd().withShowAll(true).exec();
    }

    public static List<Container> findContainersByImage(DockerClient client, String imageName) {
        return listAllContainers(client).stream()
                .filter(c -> c.getImage().equals(imageName) || c.getImage().startsWith(imageName + ":"))
                .collect(Collectors.toList());
    }

    public static boolean containerExists(DockerClient client, String containerId) {
        return listAllContainers(client).stream()
                .anyMatch(c -> c.getId().equals(containerId) || c.getId().startsWith(containerId));
    }

    public static Optional<String> getContainerState(DockerClient client, String containerId) {
        return listAllContainers(client).stream()
                .filter(c -> c.getId().equals(containerId) || c.getId().startsWith(containerId))
                .map(Container::getState)
                .findFirst();
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }

    public static long parseSize(String size) {
        if (size == null || size.isEmpty()) return 0;
        size = size.trim().toLowerCase();
        long multiplier = 1;
        if (size.endsWith("k") || size.endsWith("kb")) {
            multiplier = 1024;
            size = size.replaceAll("[kK][bB]?$", "");
        } else if (size.endsWith("m") || size.endsWith("mb")) {
            multiplier = 1024 * 1024;
            size = size.replaceAll("[mM][bB]?$", "");
        } else if (size.endsWith("g") || size.endsWith("gb")) {
            multiplier = 1024L * 1024 * 1024;
            size = size.replaceAll("[gG][bB]?$", "");
        }
        return Long.parseLong(size.trim()) * multiplier;
    }

    public static boolean isRunningInContainer() {
        try {
            java.nio.file.Path dockerEnvPath = java.nio.file.Paths.get("/.dockerenv");
            return java.nio.file.Files.exists(dockerEnvPath);
        } catch (Exception e) {
            return false;
        }
    }

        /**
     * Finds a container by its ID or name.
     *
     * @param containerIdOrName the container ID or name to search for
     * @return the Container id if found
     */
    public static String findContainer(String containerIdOrName) {
        List<Container> containers = DockerClientProvider.getInstance().getClient().listContainersCmd()
                .withShowAll(true)
                .exec();

        for (Container container : containers) {
            if (container.getId().equals(containerIdOrName) || 
                container.getId().startsWith(containerIdOrName)) {
                return container.getId();
            }
            for (String name : container.getNames()) {
                // Container names are prefixed with '/'
                String normalizedName = name.startsWith("/") ? name.substring(1) : name;
                if (normalizedName.equals(containerIdOrName) || 
                    normalizedName.startsWith(containerIdOrName)) {
                    return container.getId();
                }
            }
        }
        return null;
    }
}
