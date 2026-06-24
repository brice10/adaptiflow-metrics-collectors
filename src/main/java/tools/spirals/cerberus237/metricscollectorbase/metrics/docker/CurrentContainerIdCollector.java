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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

/**
 * Collector that retrieves the Docker container ID of the current container.
 * <p>
 * This collector attempts to determine the container ID using multiple strategies:
 * </p>
 * <ul>
 *     <li>Reading from /proc/self/cgroup</li>
 *     <li>Reading from /proc/1/cpuset</li>
 *     <li>Checking the HOSTNAME environment variable</li>
 *     <li>Reading from /proc/self/mountinfo</li>
 * </ul>
 *
 * <p>
 * The container ID is essential for correlating metrics with specific containers
 * and for querying the Docker API for detailed container information.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * CurrentContainerIdCollector collector = new CurrentContainerIdCollector();
 * Optional&lt;String&gt; containerId = collector.get();
 * containerId.ifPresent(id -> System.out.println("Container ID: " + id));
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class CurrentContainerIdCollector implements IMetricsCollector<Optional<String>> {

    private static final String CGROUP_PATH = "/proc/self/cgroup";
    private static final String CPUSET_PATH = "/proc/1/cpuset";
    private static final String MOUNTINFO_PATH = "/proc/self/mountinfo";
    
    // Pattern to match container IDs (64 hex characters for full ID, 12 for short)
    private static final Pattern CONTAINER_ID_PATTERN = Pattern.compile("([a-f0-9]{64}|[a-f0-9]{12})");
    private static final Pattern DOCKER_CGROUP_PATTERN = Pattern.compile("/docker/([a-f0-9]{64})");
    private static final Pattern KUBEPODS_PATTERN = Pattern.compile("/kubepods[^/]*/[^/]*/[^/]*([a-f0-9]{64})");

    /**
     * Retrieves the container ID of the current Docker container.
     *
     * @return An Optional containing the container ID, or empty if not in a container.
     */
    @Override
    public Optional<String> get() {
        return getContainerId();
    }

    /**
     * Attempts to retrieve the container ID using multiple detection strategies.
     *
     * @return An Optional containing the container ID if found.
     */
    public Optional<String> getContainerId() {
        // Try cgroup first (most reliable)
        Optional<String> fromCgroup = getContainerIdFromCgroup();
        if (fromCgroup.isPresent()) {
            return fromCgroup;
        }

        // Try cpuset
        Optional<String> fromCpuset = getContainerIdFromCpuset();
        if (fromCpuset.isPresent()) {
            return fromCpuset;
        }

        // Try hostname (Docker sets HOSTNAME to short container ID)
        Optional<String> fromHostname = getContainerIdFromHostname();
        if (fromHostname.isPresent()) {
            return fromHostname;
        }

        // Try mountinfo
        return getContainerIdFromMountinfo();
    }

    /**
     * Extracts container ID from /proc/self/cgroup.
     *
     * @return Optional containing the container ID if found.
     */
    public Optional<String> getContainerIdFromCgroup() {
        try {
            Path cgroupPath = Paths.get(CGROUP_PATH);
            if (Files.exists(cgroupPath)) {
                String content = new String(Files.readAllBytes(cgroupPath));
                
                // Try Docker pattern
                Matcher dockerMatcher = DOCKER_CGROUP_PATTERN.matcher(content);
                if (dockerMatcher.find()) {
                    return Optional.of(dockerMatcher.group(1));
                }

                // Try Kubernetes pattern
                Matcher kubeMatcher = KUBEPODS_PATTERN.matcher(content);
                if (kubeMatcher.find()) {
                    return Optional.of(kubeMatcher.group(1));
                }

                // Generic pattern matching
                for (String line : content.split("\n")) {
                    if (line.contains("docker") || line.contains("containerd") || line.contains("cri-o")) {
                        Matcher matcher = CONTAINER_ID_PATTERN.matcher(line);
                        if (matcher.find()) {
                            String id = matcher.group(1);
                            if (id.length() == 64) {
                                return Optional.of(id);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Unable to read cgroup file
        }
        return Optional.empty();
    }

    /**
     * Extracts container ID from /proc/1/cpuset.
     *
     * @return Optional containing the container ID if found.
     */
    public Optional<String> getContainerIdFromCpuset() {
        try {
            Path cpusetPath = Paths.get(CPUSET_PATH);
            if (Files.exists(cpusetPath)) {
                String content = new String(Files.readAllBytes(cpusetPath)).trim();
                Matcher matcher = CONTAINER_ID_PATTERN.matcher(content);
                if (matcher.find()) {
                    String id = matcher.group(1);
                    if (id.length() == 64) {
                        return Optional.of(id);
                    }
                }
            }
        } catch (IOException e) {
            // Unable to read cpuset file
        }
        return Optional.empty();
    }

    /**
     * Extracts container ID from HOSTNAME environment variable.
     * Docker typically sets HOSTNAME to the short (12-character) container ID.
     *
     * @return Optional containing the short container ID if found.
     */
    public Optional<String> getContainerIdFromHostname() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && hostname.matches("^[a-f0-9]{12}$")) {
            return Optional.of(hostname);
        }
        return Optional.empty();
    }

    /**
     * Extracts container ID from /proc/self/mountinfo.
     *
     * @return Optional containing the container ID if found.
     */
    public Optional<String> getContainerIdFromMountinfo() {
        try {
            Path mountinfoPath = Paths.get(MOUNTINFO_PATH);
            if (Files.exists(mountinfoPath)) {
                String content = new String(Files.readAllBytes(mountinfoPath));
                for (String line : content.split("\n")) {
                    if (line.contains("/docker/containers/")) {
                        Matcher matcher = CONTAINER_ID_PATTERN.matcher(line);
                        if (matcher.find()) {
                            String id = matcher.group(1);
                            if (id.length() == 64) {
                                return Optional.of(id);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Unable to read mountinfo file
        }
        return Optional.empty();
    }

    /**
     * Returns the short (12-character) version of the container ID.
     *
     * @return Optional containing the short container ID.
     */
    public Optional<String> getShortContainerId() {
        return getContainerId().map(id -> id.length() > 12 ? id.substring(0, 12) : id);
    }
}
