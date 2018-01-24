package uk.co.greenjam.jrgfinance.core.services;

import com.day.commons.datasource.poolservice.DataSourcePool;
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


    private static final String[] DB_TABLE = new String[] {"TABLE"};

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
                createTables(connection);
            }
            else{
                logger.info("NOT creating tables.");
            }
        }
    }

    private void createTables(Connection connection) {
        String SQLCommands = getResource(DB_INIT_FILE);
    }

    private String getResource(String dbInitFile) {
        String resource = null;
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(dbInitFile);
        resource = getStringFromInputStream(resourceAsStream);
        return resource;
    }

    private boolean noTablesExist(Connection connection) {
        // Ensure there are NONE of the forms portal tables before we create them

        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet rs = metadata.getTables(null, null,DB_COMMENT_TABLE, DB_TABLE);
            if (rs.next()){
                logger.info("Comment table found");
                return true;
            }
            rs = metadata.getTables(null, null,DB_ADDITIONAL_META_TABLE, DB_TABLE);
            if (rs.next()){
                logger.info("Additional meta table found");
                return true;
            }
            rs = metadata.getTables(null, null,DB_DATA_TABLE, DB_TABLE);
            if (rs.next()){
                logger.info("Data table found");
                return true;
            }
            rs = metadata.getTables(null, null,DB_METADATA_TABLE, DB_TABLE);
            if (rs.next()){
                logger.info("Metadata table found");
                return true;
            }
            rs = metadata.getTables(null, null,DB_SURVEY_TABLE, DB_TABLE);
            if (rs.next()){
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

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }


}
