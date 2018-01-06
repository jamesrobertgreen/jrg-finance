package uk.co.greenjam.jrgfinance.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.services.StockPriceService;
import uk.co.greenjam.jrgfinance.core.services.impl.StockPriceServiceImpl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Model(adaptables=Resource.class)
public class StockPriceModel {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private SlingSettingsService settings;

    @Inject
    private StockPriceService stockPriceService;

    @Inject @Named("sling:resourceType") @Default(values="No resourceType")
    protected String resourceType;

    private String stockPrice;

    @PostConstruct
    protected void init() {
        logger.info("StockPriceModel init");
        stockPrice = stockPriceService.getPrice();
        logger.info(stockPrice);
    }

    public String getStockPrice()
    {
        return stockPrice;
    }
}
