package uk.co.greenjam.jrgfinance.core.services.impl;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
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

    @Reference
    private ResourceResolverFactory resolverFactory;

    private double price;
    private String apiUrl;

//    @Override
//    public String getSettings() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Stock Price Service:\n");
//        sb.append("API URL: " + apiUrl );
//
//        return sb.toString();
//    }

    @Activate
    @Modified
    protected final void activate(Configuration config) {
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
        price = 123.0;
    }
}