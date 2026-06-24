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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import tools.spirals.cerberus237.metricscollectorbase.IMetricsCollector;
import tools.spirals.cerberus237.metricscollectorbase.models.SQLDatabaseMetrics;

import java.sql.*;

/**
 * Class for collecting metrics from a local database.
 * <p>
 * The {@code LocalDatabaseMetricsCollector} implements the {@link IMetricsCollector}
 * interface to provide various metrics related to the health and status of a local
 * SQL database connection. This includes response time, network status, active connections,
 * and pending queries.
 * </p>
 *
 * <p>
 * This class uses HikariCP for connection pooling, allowing efficient management of
 * database connections and improved performance.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * LocalDatabaseMetricsCollector dbMetricsCollector = new LocalDatabaseMetricsCollector("jdbc:mysql://localhost:3306/mydb", "user", "password");
 * SQLDatabaseMetrics metrics = dbMetricsCollector.get();
 * System.out.println("Response Time: " + metrics.getResponseTime() + " ms");
 * System.out.println("Active Connections: " + metrics.getActiveConnections());
 * System.out.println("Pending Queries: " + metrics.getPendingQueries());
 * </pre>
 *
 * @author Arléon Zemtsop (Cerberus)
 */
public class LocalDatabaseMetricsCollector implements IMetricsCollector<SQLDatabaseMetrics> {
    private final HikariDataSource dataSource;
    private static LocalDatabaseMetricsCollector instance;

    public static LocalDatabaseMetricsCollector getInstance(String dbUrl, String user, String password) {
        if (instance == null)
            instance = new LocalDatabaseMetricsCollector(dbUrl,user, password);
        return instance;
    }

    /**
     * Constructs a {@code LocalDatabaseMetricsCollector} with the specified database URL,
     * username, and password.
     *
     * @param dbUrl the JDBC URL of the database to connect to.
     * @param user the username for database authentication.
     * @param password the password for database authentication.
     */
    private LocalDatabaseMetricsCollector(String dbUrl, String user, String password) {
        this.dataSource = createDataSource(dbUrl, user, password);
    }

    /**
     * Creates a HikariDataSource with the specified database configuration.
     *
     * @param dbUrl the JDBC URL of the database.
     * @param user the username for database authentication.
     * @param password the password for database authentication.
     * @return a configured HikariDataSource instance.
     */
    private HikariDataSource createDataSource(String dbUrl, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(3000);
        config.setMinimumIdle(10);
        return new HikariDataSource(config);
    }

    /**
     * Retrieves the database metrics, including response time, network status, active connections,
     * and pending queries.
     *
     * @return an instance of {@code SQLDatabaseMetrics} containing the collected metrics.
     */
    @Override
    public SQLDatabaseMetrics get() {
        var healthCheckData = new SQLDatabaseMetrics();
        long startTime = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();) {
            healthCheckData.setResponseTime(System.currentTimeMillis() - startTime);
            healthCheckData.setNetworkStatus(conn.isValid(2));
            healthCheckData.setActiveConnections(getActiveConnexions(conn));
            healthCheckData.setPendingQueries(getPendingQueries(conn));
        } catch (SQLInvalidAuthorizationSpecException e) {
            System.err.println("Database connexion failed: invalid credentials");
            e.printStackTrace();
            throw new RuntimeException("Database connexion failed: invalid credentials. " + e.getMessage());
        } catch (SQLSyntaxErrorException e) {
            System.err.println("SQL syntax error: invalid database url or database schema. " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("SQL syntax error: invalid database url or database schema !");
        } catch (SQLTransientConnectionException e) {
            System.err.println("SQLTransientConnectionException !" + "\n" + e.getMessage());
            e.printStackTrace();
            healthCheckData.setResponseTime(Long.MAX_VALUE);
            healthCheckData.setNetworkStatus(false);
        } catch (SQLException e) {
            System.err.println("Connexion timeout : " + e.getMessage());
            e.printStackTrace();
            healthCheckData.setResponseTime(Long.MAX_VALUE);
        }
        return healthCheckData;
    }

    /**
     * Retrieves the number of active connections to the database.
     *
     * @param conn the database connection to use for querying.
     * @return the number of active connections as an {@code int}.
     * @throws SQLException if an error occurs while querying the database.
     */
    public int getActiveConnexions(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS WHERE Variable_name = 'Threads_connected';") ) {
            if (rs.next())
                return rs.getInt("Value");
        }
        return 0;
    }

    /**
     * Retrieves the number of pending queries for the database connection.
     *
     * @param conn the database connection to use for querying.
     * @return the number of pending queries as an {@code int}.
     * @throws SQLException if an error occurs while querying the database.
     */
    public int getPendingQueries(Connection conn) throws SQLException {
        int pendingQueries = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW PROCESSLIST;") ) {
            while (rs.next()) {
                String state = rs.getString("State").toLowerCase();
                if (state.startsWith("locked") || state.startsWith("waiting")) {
                    pendingQueries++;
                }
            }
        }
        return pendingQueries;
    }
}