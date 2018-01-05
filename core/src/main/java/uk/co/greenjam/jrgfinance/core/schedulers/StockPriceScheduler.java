package uk.co.greenjam.jrgfinance.core.schedulers;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.config.Configuration;
import uk.co.greenjam.jrgfinance.core.services.StockPriceService;
import uk.co.greenjam.jrgfinance.core.services.impl.StockPriceServiceImpl;


@Designate(ocd= Configuration.class)
@Component(service=Runnable.class)
public class StockPriceScheduler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private StockPriceServiceImpl stockPriceService = null;

    @Override
    public void run() {
        logger.info("StockPriceScheduler - Running");
        stockPriceService.updatePrice();
    }

    @Activate
    protected void activate(Configuration config) {
        stockPriceService = new StockPriceServiceImpl();
    }

}
