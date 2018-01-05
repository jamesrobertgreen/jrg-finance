package uk.co.greenjam.jrgfinance.core.services.impl;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.config.Configuration;
import uk.co.greenjam.jrgfinance.core.services.StockPriceService;


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
    private static double price;

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
    public double getPrice() {
        return price;
    }

    @Override
    public void updatePrice() {
        // TODO add code to retrieve the price from the apiURL
        logger.info("Updating stock price " + new java.util.Date());
        price = 123.0;
    }
}