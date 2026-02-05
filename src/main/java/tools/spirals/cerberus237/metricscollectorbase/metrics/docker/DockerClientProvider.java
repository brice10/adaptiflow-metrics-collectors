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
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;

/**
 * Singleton provider for Docker client instances.
 * <p>
 * This class manages the lifecycle of the Docker client connection and provides
 * a centralized access point for all Docker metric collectors. It handles the
 * configuration and initialization of the Docker client using the docker-java library.
 * </p>
 *
 * <p>
 * The provider automatically detects the Docker host from environment variables
 * or uses the default Unix socket on Linux systems.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * DockerClient client = DockerClientProvider.getInstance().getClient();
 * // Use the client for Docker operations
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class DockerClientProvider {

    private static volatile DockerClientProvider instance;
    private final DockerClient dockerClient;
    private final DockerClientConfig config;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the Docker client with default configuration.
     */
    private DockerClientProvider() {
        this.config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * Returns the singleton instance of the DockerClientProvider.
     *
     * @return The singleton DockerClientProvider instance.
     */
    public static DockerClientProvider getInstance() {
        if (instance == null) {
            synchronized (DockerClientProvider.class) {
                if (instance == null) {
                    instance = new DockerClientProvider();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the configured Docker client.
     *
     * @return The DockerClient instance.
     */
    public DockerClient getClient() {
        return dockerClient;
    }

    /**
     * Returns the Docker client configuration.
     *
     * @return The DockerClientConfig instance.
     */
    public DockerClientConfig getConfig() {
        return config;
    }

    /**
     * Tests if the Docker daemon is reachable.
     *
     * @return true if Docker daemon is reachable, false otherwise.
     */
    public boolean isDockerReachable() {
        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the Docker client connection.
     * Should be called when the application shuts down.
     */
    public void close() {
        try {
            if (dockerClient != null) {
                dockerClient.close();
            }
        } catch (Exception e) {
            // Log error but don't throw
            e.printStackTrace();
        }
    }
}
