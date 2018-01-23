package uk.co.greenjam.jrgfinance.core.services;

import com.google.common.base.Charsets;


import com.google.common.io.CharStreams;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Map;


@Component( immediate = true,
            service=DBInitialiseService.class )
public class DBInitialiseService {

        private static final String DB_INIT_FILE = "initMysqlDB.sql";

        private static final String DB_USERNAME = "username";
        private static final String DB_PASSWORD = "password";
        private static final String DB_SERVER = "server";
        private static final String DB_PORT = "port";

        private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

        private ComponentContext context = null;

        @Activate
        protected void activate(ComponentContext context) {
            logger.info("Activating " + this.getClass());
                this.context = context;

                String SQLCommands = getResource(DB_INIT_FILE);

                Map dbConfig = getDBConfig();
                Connection connection = connectToDB(dbConfig);
                if(connection != null){
                        logger.info("Connection created...");
                        if(!tablesExist(connection)){
                                createTables(connection);
                        }
                }
                else {
                        logger.error("Unable to connect to database using connection information: " + dbConfig);
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

        private Connection connectToDB(Map dbConfig) {
                Connection connection = null;
                return connection;
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
