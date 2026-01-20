/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.spirals.cerberus237.metricscollectorbase.metrics.requests;

import tools.spirals.cerberus237.metricscollectorbase.filters.RequestMetricsController;
import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;
import tools.spirals.cerberus237.metricscollectorbase.models.ServiceMetrics;

import java.util.Map;

/**
 * Class for collecting local request metrics for services.
 * <p>
 * The {@code LocalRequestMetricsCollector} implements the {@link IMetricsCollector}
 * interface to retrieve request metrics from the {@link RequestMetricsController}.
 * This collector is designed to return metrics for the first service entry available
 * in the metrics map.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * LocalRequestMetricsCollector metricsCollector = new LocalRequestMetricsCollector();
 * ServiceMetrics serviceMetrics = metricsCollector.get();
 * System.out.println("Service Metrics: " + serviceMetrics);
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class LocalRequestMetricsCollector implements IMetricsCollector<ServiceMetrics> {

    /**
     * Retrieves the metrics for the first service in the metrics map.
     *
     * @return The metrics for the first service, or null if no metrics are available.
     */
    @Override
    public ServiceMetrics get() {
        Map<String, ServiceMetrics> serviceMetricsMap = RequestMetricsController.getAllMetrics();
        if (serviceMetricsMap.isEmpty()) {
            return null;
        }
        return serviceMetricsMap.values().iterator().next();
    }
}
