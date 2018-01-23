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
import java.sql.Connection;
import java.util.Map;


@Component(immediate = true,
        service = DBInitialiseService.class)
public class DBInitialiseService {
    @Reference
    private DataSourcePool source;

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
            if (!tablesExist(connection)) {
                createTables(connection);
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

    private boolean tablesExist(Connection connection) {
        // Default to tables existing, so we don't overwrite tables if this method fails
        boolean exists = true;
        return exists;
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
