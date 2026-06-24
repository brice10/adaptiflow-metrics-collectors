/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.spirals.cerberus237.metricscollectorbase.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

/**
 * A generic REST metrics collector.
 * It queries an endpoint and automatically deserializes the JSON result 
 * into the specified target class.
 *
 * @param <T> The type of the collected metric.
 * @author Arléon Zemtsop (Cerberus)
 */
public class RestMetricsCollector<T> implements IMetricsCollector<T> {

    protected static final Logger logger = LoggerFactory.getLogger(RestMetricsCollector.class);

    private final String targetServiceUrl;
    private final String endpointUri;
    private final String httpMethod;
    private final Class<T> metricType;
    
    private final HttpClient httpClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs a new REST metrics collector.
     *
     * @param targetServiceUrl The base URL of the service (e.g., "http://adaptable-teastore-image:8080")
     * @param endpointUri      The endpoint path (e.g., "/metrics/status")
     * @param httpMethod       The HTTP method ("GET", "POST", etc.)
     * @param metricType       The class representing the metric (e.g., SystemStatus.class)
     */
    public RestMetricsCollector(String targetServiceUrl, String endpointUri, String httpMethod, Class<T> metricType) {
        this.targetServiceUrl = targetServiceUrl;
        this.endpointUri = endpointUri;
        this.httpMethod = (httpMethod != null) ? httpMethod.toUpperCase() : "GET";
        this.metricType = metricType;
        
        // Initialize a lightweight HTTP client with a timeout
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        String fullUrl = targetServiceUrl + (endpointUri.startsWith("/") ? endpointUri : "/" + endpointUri);
        
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofSeconds(5));

            switch (this.httpMethod) {
                case "POST":
                    requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                    break;
                case "GET":
                default:
                    requestBuilder.GET();
                    break;
            }

            HttpRequest request = requestBuilder.build();
            logger.debug("[RestMetricsCollector] Fetching metrics from: {} {}", httpMethod, fullUrl);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String responseBody = response.body();
                
                // Special case: if the target is a simple String, return it directly
                if (metricType.equals(String.class)) {
                    return (T) responseBody;
                }
                
                // General case: Deserialize the JSON to the requested Java class
                return objectMapper.readValue(responseBody, metricType);
            } else {
                logger.error("[RestMetricsCollector] HTTP Request failed with status {} for URL: {}", response.statusCode(), fullUrl);
            }

        } catch (InterruptedException e) {
            logger.error("[RestMetricsCollector] Request to {} was interrupted", fullUrl);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("[RestMetricsCollector] Failed to collect metrics from {}: {}", fullUrl, e.getMessage());
        }
        
        return null; // Return null on failure (to be handled by the MAPE loop)
    }
}