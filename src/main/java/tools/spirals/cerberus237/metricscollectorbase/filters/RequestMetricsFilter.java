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

import java.io.IOException;
import java.util.Date;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.spirals.cerberus237.metricscollectorbase.models.ServiceMetrics;

/**
 * Servlet filter for tracking detailed request metrics for each service.
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class RequestMetricsFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestMetricsFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic (if needed)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract request details
        String serviceName = httpRequest.getRequestURI().split("/")[1]; // Extract service name from URI
        Date receivedAt = new Date();
        String sourceHost = request.getRemoteAddr();
        int sourcePort = request.getRemotePort();
        String targetHost = request.getLocalAddr();
        int targetPort = request.getLocalPort();
        String endpoint = httpRequest.getRequestURI();
        long startTime = System.currentTimeMillis();

        // Log the request
        boolean success = false;
        try {
            chain.doFilter(request, response);
            success = httpResponse.getStatus() < 400; // Consider status codes < 400 as success
        } catch (Exception e) {
            LOG.error("Request failed: " + e.getMessage(), e);
        } finally {
            long requestTime = System.currentTimeMillis() - startTime;

            // Update metrics for the service
            RequestMetricsController.SERVICE_METRICS.computeIfAbsent(serviceName, ServiceMetrics::new)
                    .logRequest(receivedAt, sourceHost, sourcePort, targetHost, targetPort, endpoint, requestTime, success);
        }
    }

    @Override
    public void destroy() {
        // Cleanup logic (if needed)
    }
}
