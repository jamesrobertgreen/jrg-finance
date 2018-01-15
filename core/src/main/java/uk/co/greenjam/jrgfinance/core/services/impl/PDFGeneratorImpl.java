package uk.co.greenjam.jrgfinance.core.services.impl;

import com.google.gson.Gson;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.config.Configuration;
import uk.co.greenjam.jrgfinance.core.services.PDFGenerator;
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
        service = PDFGenerator.class,
        configurationPid = "uk.co.greenjam.jrgfinance.core.services.impl.PDFGeneratorImpl"
)
@Designate(
        ocd = Configuration.class
)
public class PDFGeneratorImpl implements PDFGenerator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Activate
    @Modified
    protected final void activate(Configuration config) {
        logger.info("PDF Generator Activate");
    }

    @Deactivate
    protected void deactivate() {
    }


    @Override
    public void generatePDF(String template, String xmlString, String saveLocation) {
        // TODO implement saveLocation functionality - for now use hardcoded path
        logger.info("Generate PDF!");

    }
}