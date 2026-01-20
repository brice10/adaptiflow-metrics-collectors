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
package tools.spirals.cerberus237.metricscollectorbase.metrics.database;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;

import java.sql.*;

/**
 * Class for collecting response time metrics for local database connections.
 * <p>
 * The {@code LocalDatabaseResponseTimeCollector} implements the {@link IMetricsCollector}
 * interface to measure the time taken to establish a connection to a local database.
 * It provides a mechanism to monitor the responsiveness of the database, which can be
 * critical for performance tuning and troubleshooting.
 * </p>
 *
 * <p>
 * This class is particularly useful in environments where database responsiveness
 * affects application performance. By measuring connection times, developers and
 * operators can gain insights into the efficiency of database configurations and
 * identify potential bottlenecks.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * LocalDatabaseResponseTimeCollector dbResponseTimeCollector = new LocalDatabaseResponseTimeCollector("jdbc:mysql://localhost:3306/mydb", "user", "password");
 * Long responseTime = dbResponseTimeCollector.get();
 * System.out.println("Database Response Time: " + responseTime + " ms");
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class LocalDatabaseResponseTimeCollector implements IMetricsCollector<Long> {
    private final String DB_URL;
    private final String USER;
    private final String PASSWORD;

    /**
     * Constructs a {@code LocalDatabaseResponseTimeCollector} with the specified database URL,
     * user, and password.
     *
     * @param dbUrl the database URL to connect to.
     * @param user the username for database authentication.
     * @param password the password for database authentication.
     */
    public LocalDatabaseResponseTimeCollector(String dbUrl, String user, String password) {
        DB_URL = dbUrl;
        USER = user;
        PASSWORD = password;
    }

    /**
     * Measures the time taken to establish a connection to the database.
     *
     * @return the response time in milliseconds as a {@code Long}. If a connection
     *         error occurs, it returns {@code Long.MAX_VALUE} to indicate a timeout.
     */
    @Override
    public Long get() {
        long startTime = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            return System.currentTimeMillis() - startTime;
        } catch (SQLInvalidAuthorizationSpecException e) {
            System.err.println("Database connexion failed: invalid credentials");
            throw new RuntimeException("Database connexion failed: invalid credentials");
        } catch (SQLSyntaxErrorException e) {
            System.err.println("SQL syntax error: invalid database url or database schema !");
            throw new RuntimeException("SQL syntax error: invalid database url or database schema !");
        } catch (SQLTransientConnectionException e) {
            System.err.println("SQLTransientConnectionException !" + "\n" + e.getMessage());
            throw new RuntimeException("SQLTransientConnectionException !" + "\n" + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connexion timeout : " + e.getMessage());
            return Long.MAX_VALUE;
        }
    }
}