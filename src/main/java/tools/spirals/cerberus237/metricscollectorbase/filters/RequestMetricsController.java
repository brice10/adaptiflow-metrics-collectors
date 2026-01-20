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
package tools.spirals.cerberus237.metricscollectorbase.filters;

import tools.spirals.cerberus237.metricscollectorbase.models.RequestDetails;
import tools.spirals.cerberus237.metricscollectorbase.models.ServiceMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code RequestMetricsController} class provides static methods for managing
 * and retrieving metrics related to service requests in a distributed application.
 * <p>
 * It maintains a concurrent map of service metrics, allowing thread-safe access
 * to metrics such as error rates, request rates, and detailed request information
 * for various services. This class is particularly useful for monitoring
 * the performance and reliability of services in a microservices architecture.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Retrieve error rates for specific services.</li>
 *     <li>Calculate request rates per second over a given time window.</li>
 *     <li>Access detailed request information for specific services.</li>
 *     <li>Get all metrics for all registered services.</li>
 *     <li>Reset all metrics when needed.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * double errorRate = RequestMetricsController.getErrorRate("ServiceName");
 * double requestRate = RequestMetricsController.getRequestRatePerSecond("ServiceName", 60000);
 * List<RequestDetails> requestDetails = RequestMetricsController.getRequestDetails("ServiceName");
 * </pre>
 *
 * <p>
 * All metrics are stored in a thread-safe manner using a {@link ConcurrentHashMap},
 * making this class suitable for use in multi-threaded environments.
 * </p>
 *
 * @see ServiceMetrics
 * @see RequestDetails
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class RequestMetricsController {
    public static final Map<String, ServiceMetrics> SERVICE_METRICS = new ConcurrentHashMap<>();

    /**
     * Returns the error rate for a specific service.
     *
     * @param serviceName The name of the service.
     * @return The error rate as a double (between 0 and 1).
     */
    public static double getErrorRate(String serviceName) {
        ServiceMetrics metrics = RequestMetricsController.SERVICE_METRICS.get(serviceName);
        return (metrics == null) ? 0 : metrics.getErrorRate();
    }

    /**
     * Returns the request rate per second for a specific service.
     *
     * @param serviceName     The name of the service.
     * @param timeWindowMillis The time window in milliseconds.
     * @return The request rate per second.
     */
    public static double getRequestRatePerSecond(String serviceName, long timeWindowMillis) {
        ServiceMetrics metrics = RequestMetricsController.SERVICE_METRICS.get(serviceName);
        return (metrics == null) ? 0 : metrics.getRequestRatePerSecond(timeWindowMillis);
    }

    /**
     * Returns the list of request details for a specific service.
     *
     * @param serviceName The name of the service.
     * @return A list of request details.
     */
    public static List<RequestDetails> getRequestDetails(String serviceName) {
        ServiceMetrics metrics = RequestMetricsController.SERVICE_METRICS.get(serviceName);
        return (metrics == null) ? new ArrayList<>() : metrics.getRequestDetails();
    }

    /**
     * Returns all metrics for all services.
     *
     * @return A map of service names to their metrics.
     */
    public static Map<String, ServiceMetrics> getAllMetrics() {
        return new HashMap<>(RequestMetricsController.SERVICE_METRICS);
    }

    /**
     * Returns metrics for a specific service.
     *
     * @param serviceName The name of the service.
     * @return The metrics for the specified service, or null if the service does not exist.
     */
    public static ServiceMetrics getMetricsForService(String serviceName) {
        return RequestMetricsController.SERVICE_METRICS.get(serviceName);
    }

    public static void resetMetrics() {
        RequestMetricsController.SERVICE_METRICS.clear();
    }
}
