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

    private static String apiUrl;
    // Price to be shared across all instances
    private static String price;

    private static final String PRICES_UNAVAILABLE = "Prices are currently unavailable.";

    @Activate
    @Modified
    protected final void activate(Configuration config) {
        logger.info("Stock Price Service Activate");
        this.apiUrl = config.stockprice_apiurl_string();
        // Although this will be called from a scheduled task, we want to start with a value
        updatePrice();
    }

    @Deactivate
    protected void deactivate() {
    }

    /**
     * @return String containing the latest price and additional information
     */
    @Override
    public String getPrice() {
        return price;
    }

    /**
     * Update the price using the api url specified on the osgi configuration
     */
    @Override
    public void updatePrice() {
        logger.info("Updating stock price ");
        URL url = createConnection(apiUrl);
        if (url == null){
            return;
        }

        HttpURLConnection con = openConnection(url);
        if (con == null){
            return;
        }
        if ( getStatus(con) == 200 ){
            String response = getResponse(con);
            if (response != null && ! response.equals("")) {
                StockPriceResponse stockPriceResponse = convertResponse(response);
                if (stockPriceResponse != null){
                    // Update the price
                    price = stockPriceResponse.getPrice();
                }
                else {
                    updateFailed();
                }
            }
            else
            {
                updateFailed();
            }
        }
        else {
            updateFailed();
        }
    }

    private StockPriceResponse convertResponse(String json) {
        StockPriceResponse resp = null;
        Gson gson = new Gson();
        resp = gson.fromJson(json, StockPriceResponse.class);
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
            logger.info("Response read: " + response);
        }
        return response;
    }

    private int getStatus(HttpURLConnection con) {
        int status = 0;
        try {
            status = con.getResponseCode();
        } catch (IOException e) {
            logger.error("Failed to read status");
            updateFailed();
            e.printStackTrace();
        }
        return status;
    }

    private HttpURLConnection openConnection(URL url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
        } catch (IOException e) {
            logger.error("Unable to connect to: " + apiUrl );
            updateFailed();
            e.printStackTrace();
        }
        return con;
    }

    private URL createConnection(String apiUrl) {
        URL url = null;
        try {
            url = new URL(apiUrl);
        } catch (MalformedURLException e) {
            logger.error("Malformed URL configured: " + apiUrl );
            updateFailed();
            e.printStackTrace();
        }
        return url;
    }

    private void updateFailed(){
        // if the update failed, update the price accordingly
        price = PRICES_UNAVAILABLE;
    }


}