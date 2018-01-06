package uk.co.greenjam.jrgfinance.core.services.impl;

import com.google.gson.Gson;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.config.Configuration;
import uk.co.greenjam.jrgfinance.core.services.StockPriceService;
import uk.co.greenjam.jrgfinance.core.services.models.StockPriceResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


@Component(
        immediate = true,
        service = StockPriceService.class,
        configurationPid = "uk.co.greenjam.jrgfinance.core.services.impl.StockPriceServiceImpl"
)
@Designate(
        ocd = Configuration.class
)
public class StockPriceServiceImpl implements StockPriceService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactory resolverFactory;

    private String apiUrl;
    // Price to be share across all instances
    private static String price;
    private static int status;

    @Activate
    @Modified
    protected final void activate(Configuration config) {
        logger.info("Stock Price Service Activate");
        apiUrl = config.stockprice_apiurl_string();
        // Although this will be called from a scheduled task, we want to start with a value
        updatePrice();
    }

    @Deactivate
    protected void deactivate() {
    }

    @Override
    public String getPrice() {
        return price;
    }

    /**
     * Update the price using the api url specified on the osgi configuration
     */
    @Override
    public void updatePrice() {
        // TODO add code to retrieve the price from the apiURL
        logger.info("Updating stock price ");
        URL url = createConnection(apiUrl);

        HttpURLConnection con = openConnection(url);

        if ( getStatus(con) == 200 ){
            String response = getResponse(con);
            StockPriceResponse stockPriceResponse = convertResponse(response);
//            price = stockPriceResponse.getPrice();
        }


    }

    private StockPriceResponse convertResponse(String json) {
        Gson gson = new Gson();
        StockPriceResponse resp = gson.fromJson(json, StockPriceResponse.class);
        logger.info("GSON response version = " + resp.getVersion());
        return resp;
    }

    private String getResponse(HttpURLConnection con) {
        BufferedReader in = null;
        StringBuffer content = null;
        String response = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content!= null) {
            response = content.toString();
            logger.info("Respsonse read: " + response);
        }
        return response;
    }

    /**
     * @param con
     * @return int status e.g. 200
     */
    private int getStatus(HttpURLConnection con) {
        int status = 0;
        try {
            status = con.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }


    /**
     * @param url
     * @return HttpURLConnection
     */
    private HttpURLConnection openConnection(URL url) {
        // Open the connection
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
        } catch (IOException e) {
            logger.error("Unable to connect to: " + apiUrl );
            e.printStackTrace();
        }
        return con;
    }


    /**
     * @param apiUrl
     * @return URL for the connection
     */
    private URL createConnection(String apiUrl) {
        URL url = null;
        try {
            url = new URL(apiUrl);
        } catch (MalformedURLException e) {
            logger.error("Malformed URL configured: " + apiUrl );
            e.printStackTrace();
        }
        return url;
    }


}