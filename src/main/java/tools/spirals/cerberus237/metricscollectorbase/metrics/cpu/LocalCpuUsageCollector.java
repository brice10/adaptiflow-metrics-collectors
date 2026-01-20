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
package tools.spirals.cerberus237.metricscollectorbase.metrics.cpu;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Class for collecting CPU usage metrics from the Java Management Extensions (JMX).
 * <p>
 * The {@code LocalCpuUsageCollector} implements the {@link IMetricsCollector} interface
 * to provide the current CPU usage as a percentage. It retrieves the CPU load
 * information using the MBeanServerConnection and the OperatingSystem MBean.
 * </p>
 *
 * <p>
 * This class is particularly useful for monitoring system performance and resource usage
 * in Java applications. The CPU usage is calculated by querying the
 * {@code SystemCpuLoad} attribute of the OperatingSystem MBean, which provides
 * a value between 0.0 and 1.0 representing the CPU load.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * LocalCpuUsageCollector cpuUsageCollector = new LocalCpuUsageCollector();
 * double currentCpuUsage = cpuUsageCollector.get();
 * System.out.println("Current CPU Usage: " + currentCpuUsage + "%");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class LocalCpuUsageCollector implements IMetricsCollector<Double> {

    /**
     * Retrieves the current CPU usage as a percentage.
     *
     * @return The CPU usage percentage, where a value of 100.0 indicates full usage.
     */
    @Override
    public Double get() {
        return getCpuUsage();
    }

    /**
     * Calculates the CPU usage by querying the SystemCpuLoad attribute
     * from the OperatingSystem MBean.
     *
     * @return The CPU load as a percentage (0.0 to 100.0).
     */
    public double getCpuUsage() {
        double cpuLoad = 0.0;
        try {
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
            ObjectName osBeanName = ObjectName.getInstance("java.lang:type=OperatingSystem");
            cpuLoad = (Double) mbsc.getAttribute(osBeanName, "SystemCpuLoad");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cpuLoad * 100;
    }
}
