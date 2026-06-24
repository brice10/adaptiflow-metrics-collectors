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
package tools.spirals.cerberus237.metricscollectorbase.metrics.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

/**
 * Class for collecting memory usage metrics from the Java Virtual Machine (JVM).
 * <p>
 * The {@code LocalMemoryUsageCollector} implements the {@link IMetricsCollector} interface
 * to provide the current memory usage of the heap as a percentage. It retrieves
 * memory usage information using the MemoryMXBean provided by the Java Management
 * Extensions (JMX).
 * </p>
 *
 * <p>
 * This class is useful for monitoring the memory consumption of Java applications,
 * allowing developers to track memory usage and detect potential memory leaks or
 * inefficient memory usage patterns.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * LocalMemoryUsageCollector memoryUsageCollector = new LocalMemoryUsageCollector();
 * double currentMemoryUsage = memoryUsageCollector.get();
 * System.out.println("Current Memory Usage: " + currentMemoryUsage + "%");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class LocalMemoryUsageCollector implements IMetricsCollector<Double> {

    /**
     * Retrieves the current memory usage of the heap as a percentage.
     *
     * @return The memory usage percentage, where a value of 100.0 indicates full usage.
     */
    @Override
    public Double get() {
        return getMemoryUsage();
    }

    /**
     * Calculates the memory usage by retrieving the heap memory usage from the
     * MemoryMXBean.
     *
     * @return The memory usage as a percentage (0.0 to 100.0).
     */
    public double getMemoryUsage() {
        MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed();
        long maxMemory = heapUsage.getMax();

        return (maxMemory > 0) ? (usedMemory * 100.0 / maxMemory) : 0.0;
    }
}
