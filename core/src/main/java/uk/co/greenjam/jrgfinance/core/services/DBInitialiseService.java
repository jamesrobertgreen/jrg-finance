package uk.co.greenjam.jrgfinance.core.services;

import com.day.commons.datasource.poolservice.DataSourcePool;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component(immediate = true,
        service = DBInitialiseService.class)
public class DBInitialiseService {
    @Reference
    private DataSourcePool source;

    private static final String DB_COMMENT_TABLE = "commenttable";
    private static final String DB_ADDITIONAL_META_TABLE = "additionalmetadatatable";
    private static final String DB_DATA_TABLE = "data";
    private static final String DB_METADATA_TABLE = "metadata";
    private static final String DB_SURVEY_TABLE = "survey";


    private static final String[] DB_TABLE = new String[]{"TABLE"};

    private static final String DB_INIT_FILE = "initMysqlDB.sql";
    private static final String DB_DATASOURCE = "jrgfinance.survey";

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    private ComponentContext context = null;

    @Activate
    protected void activate(ComponentContext context) {
        logger.info("Activating " + this.getClass());
        this.context = context;

        Connection connection = getConnection();
        if (connection != null) {
            logger.info("Connection created...");
            if (!noTablesExist(connection)) {
                logger.info("Creating tables");
                createTables(connection);
            } else {
                logger.info("NOT creating tables.");
            }
        }
    }

    private void createTables(Connection connection) {
        InputStream is = getResource(DB_INIT_FILE);

        if (is != null) {
            executeSQL(is, connection);
        } else {
            logger.error("Failed to read SQL Commands");
        }

    }

    private InputStream getResource(String dbInitFile) {
        return getClass().getClassLoader().getResourceAsStream(dbInitFile);
    }

    private boolean noTablesExist(Connection connection) {
        // Ensure there are NONE of the forms portal tables before we create them

        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet rs = metadata.getTables(null, null, DB_COMMENT_TABLE, DB_TABLE);
            if (rs.next()) {
                logger.info("Comment table found");
                return true;
            }
            rs = metadata.getTables(null, null, DB_ADDITIONAL_META_TABLE, DB_TABLE);
            if (rs.next()) {
                logger.info("Additional meta table found");
                return true;
            }
            rs = metadata.getTables(null, null, DB_DATA_TABLE, DB_TABLE);
            if (rs.next()) {
                logger.info("Data table found");
                return true;
            }
            rs = metadata.getTables(null, null, DB_METADATA_TABLE, DB_TABLE);
            if (rs.next()) {
                logger.info("Metadata table found");
                return true;
            }
            rs = metadata.getTables(null, null, DB_SURVEY_TABLE, DB_TABLE);
            if (rs.next()) {
                logger.info("Survey table found");
                return true;
            }
            // If NONE of the tables exist
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Default to tables existing, so we don't overwrite tables if this method fails
        return true;
    }

    private Map getDBConfig() {
        Map config = null;
        return config;
    }

    private Connection getConnection() {
        DataSource dataSource = null;
        Connection con = null;

        try {
            dataSource = (DataSource) source.getDataSource(DB_DATASOURCE);
            return dataSource.getConnection();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to connect to datasource " + DB_DATASOURCE);
        }
        return null;
    }

    private boolean executeSQL(InputStream is, Connection connection) {
        String delimiter = ";";
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder currentStatement = new StringBuilder();
        List<String> statements = new ArrayList<String>();
        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("--"))
                    continue;
                if (line.endsWith(delimiter)) {
                    line = line.substring(0, line.length() - delimiter.length()).trim();
                    if (line.length() > 0) {
                        if (currentStatement.length() > 0)
                            currentStatement.append("\n");
                        currentStatement.append(line);
                        statements.add(currentStatement.toString());
                        currentStatement = new StringBuilder();
                    }
                } else if (line.toUpperCase().startsWith("DELIMITER")) {
                    delimiter = line.substring("DELIMITER".length()).trim();
                } else {
                    if (currentStatement.length() > 0)
                        currentStatement.append("\n");
                    currentStatement.append(line);
                }
            }
            br.close();
            for (String statement : statements) {
                logger.info("***************************************************************");
                logger.info(statement);
                logger.info("***************************************************************");
                if (execute(connection, statement) < 0)
                    return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to execute script - " + e.getMessage());
            return false;
        }
    }


    public int execute(Connection connection, String statement) {
        if (StringUtils.isEmpty(statement)) {
            return -1;
        }

        Statement pst = null;
        try {

            logger.info("***************************************************************");
            logger.info(statement);
            logger.info("***************************************************************");

            pst = connection.createStatement();
            int result = pst.executeUpdate(statement);
            return result >= 0 ? result : 0;
        } catch (SQLException e) {
            logger.error("Couldn't execute statement - " + statement);
            logger.error(e.getMessage());
        }
        return -1;
    }


}
