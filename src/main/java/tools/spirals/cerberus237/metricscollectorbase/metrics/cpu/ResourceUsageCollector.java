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
import tools.spirals.cerberus237.metricscollectorbase.metrics.memory.LocalMemoryUsageCollector;

import java.util.HashMap;

public class ResourceUsageCollector implements IMetricsCollector<HashMap<String, Double>> {
    @Override
    public HashMap<String, Double> get() {
        HashMap<String, Double> data = new HashMap<>();
        data.put("cpu", new LocalCpuUsageCollector().get());
        data.put("memory", new LocalMemoryUsageCollector().get());
        return data;
    }
}
