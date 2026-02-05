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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Collector that detects whether the current application is running inside a Docker container.
 * <p>
 * This collector uses multiple detection strategies to determine if the application
 * is containerized:
 * </p>
 * <ul>
 *     <li>Checks for the presence of /.dockerenv file</li>
 *     <li>Inspects /proc/1/cgroup for docker or containerd references</li>
 *     <li>Checks for container-specific environment variables</li>
 *     <li>Inspects /proc/self/mountinfo for overlay filesystem</li>
 * </ul>
 *
 * <p>
 * This information is critical for adaptive systems that need to adjust their behavior
 * based on the deployment environment (e.g., resource limits, networking, storage).
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * DockerEnvironmentDetector detector = new DockerEnvironmentDetector();
 * boolean isRunningInDocker = detector.get();
 * if (isRunningInDocker) {
 *     System.out.println("Application is running inside a Docker container");
 * }
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class DockerEnvironmentDetector implements IMetricsCollector<Boolean> {

    private static final String DOCKERENV_PATH = "/.dockerenv";
    private static final String CGROUP_PATH = "/proc/1/cgroup";
    private static final String MOUNTINFO_PATH = "/proc/self/mountinfo";

    /**
     * Determines whether the application is running inside a Docker container.
     *
     * @return true if running inside Docker, false otherwise.
     */
    @Override
    public Boolean get() {
        return isRunningInDocker();
    }

    /**
     * Performs comprehensive Docker environment detection using multiple strategies.
     *
     * @return true if any detection strategy confirms Docker environment.
     */
    public boolean isRunningInDocker() {
        return hasDockerEnvFile() 
            || hasCgroupDockerReference() 
            || hasContainerEnvironmentVariables()
            || hasOverlayFilesystem();
    }

    /**
     * Checks for the presence of the /.dockerenv file.
     * This file is created by Docker when starting a container.
     *
     * @return true if /.dockerenv exists.
     */
    public boolean hasDockerEnvFile() {
        return new File(DOCKERENV_PATH).exists();
    }

    /**
     * Inspects /proc/1/cgroup for Docker or containerd references.
     * Container runtimes typically create specific cgroup hierarchies.
     *
     * @return true if cgroup indicates Docker/container environment.
     */
    public boolean hasCgroupDockerReference() {
        try {
            Path cgroupPath = Paths.get(CGROUP_PATH);
            if (Files.exists(cgroupPath)) {
                String content = new String(Files.readAllBytes(cgroupPath));
                return content.contains("docker") 
                    || content.contains("kubepods") 
                    || content.contains("containerd")
                    || content.contains("/lxc/")
                    || content.contains("/ecs/");
            }
        } catch (IOException e) {
            // Unable to read cgroup, return false
        }
        return false;
    }

    /**
     * Checks for container-specific environment variables.
     *
     * @return true if container-related environment variables are present.
     */
    public boolean hasContainerEnvironmentVariables() {
        // Check for common container environment variables
        String[] containerEnvVars = {
            "KUBERNETES_SERVICE_HOST",
            "DOCKER_CONTAINER",
            "container",
            "HOSTNAME"  // Often set to container ID in Docker
        };

        for (String envVar : containerEnvVars) {
            String value = System.getenv(envVar);
            if (value != null && !value.isEmpty()) {
                // HOSTNAME check: Docker sets this to a short container ID format
                if ("HOSTNAME".equals(envVar) && value.matches("^[a-f0-9]{12}$")) {
                    return true;
                }
                if (!"HOSTNAME".equals(envVar)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks /proc/self/mountinfo for overlay filesystem indicators.
     * Docker typically uses overlay or overlay2 storage drivers.
     *
     * @return true if overlay filesystem is detected.
     */
    public boolean hasOverlayFilesystem() {
        try {
            Path mountInfoPath = Paths.get(MOUNTINFO_PATH);
            if (Files.exists(mountInfoPath)) {
                String content = new String(Files.readAllBytes(mountInfoPath));
                return content.contains("overlay") || content.contains("aufs");
            }
        } catch (IOException e) {
            // Unable to read mountinfo, return false
        }
        return false;
    }

    /**
     * Returns detailed detection results for debugging purposes.
     *
     * @return A formatted string with all detection method results.
     */
    public String getDetectionDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Docker Environment Detection Results:\n");
        sb.append("  - /.dockerenv file present: ").append(hasDockerEnvFile()).append("\n");
        sb.append("  - Cgroup Docker reference: ").append(hasCgroupDockerReference()).append("\n");
        sb.append("  - Container env variables: ").append(hasContainerEnvironmentVariables()).append("\n");
        sb.append("  - Overlay filesystem: ").append(hasOverlayFilesystem()).append("\n");
        sb.append("  - Final determination: ").append(isRunningInDocker());
        return sb.toString();
    }
}
