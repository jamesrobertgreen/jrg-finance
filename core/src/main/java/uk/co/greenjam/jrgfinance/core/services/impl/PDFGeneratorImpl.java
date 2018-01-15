package uk.co.greenjam.jrgfinance.core.services.impl;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.fd.forms.api.FormsService;
import com.adobe.fd.forms.api.PDFFormRenderOptions;
import com.adobe.icc.services.api.FormService;
import com.google.gson.Gson;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.commons.IOUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.greenjam.jrgfinance.core.config.Configuration;
import uk.co.greenjam.jrgfinance.core.services.PDFGenerator;
import uk.co.greenjam.jrgfinance.core.services.StockPriceService;
import uk.co.greenjam.jrgfinance.core.services.models.StockPriceResponse;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


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

    @Reference private FormsService formsService;
    @Override
    public void generatePDF(String template, String xmlString, String saveLocation) {
        logger.info("Generate PDF!");

        // TODO - Hardcoded for testing
        xmlString =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "    <afData>\n" +
                "        <afUnboundData>\n" +
                "            <data>\n" +
                "                <name>aaa</name>\n" +
                "                <emailAddress>bbb</emailAddress>\n" +
                "                <message>ccc</message>\n" +
                "            </data>\n" +
                "        </afUnboundData>\n" +
                "    </afData>";

        template = "/content/dam/formsanddocuments/jrg-finance/templates/contact.xdp/jcr:content/renditions/original/jcr:content";
        saveLocation = "/content/dam/formsanddocuments/jrg-finance/output/test.pdf";

        Map<String, Object> param = new HashMap<String, Object>();
        param.put(ResourceResolverFactory.SUBSERVICE, "datawrite");

        ResourceResolver resourceResolver = null;
        Session session = null;
        try {

            resourceResolver = resolverFactory.getServiceResourceResolver(param);
            session = resourceResolver.adaptTo(Session.class);

            //Create a node that represents the root node
            Node root = session.getRootNode();

            // Retrieve content
            Node node = root.getNode("/content/dam/formsanddocuments/jrg-finance/templates/contact.xdp");
            System.out.println(node.getPath());


            String xdpString = node.getProperty("jcr:data").toString();
            System.out.println(xdpString);
            logger.info(xdpString);



            byte[] xdpData = xdpString.getBytes();

        Document docData = new Document(xmlString);

        PDFFormRenderOptions options = new PDFFormRenderOptions();

        options.setContentRoot(template); // location of XDP


        Document docPDF = formsService.renderPDFForm("test",docData,options);

        String result = docPDF.getInputStream().toString();





            //tore content
            Node adobe = root.addNode("jrg");
            Node day = adobe.addNode("finance");
            day.setProperty("PDF", result);




//            //Create a node that represents the root node
//            Node newNode = session.getRootNode();

//            // Store content
//            Node adobe = root.addNode("adobe");
//            Node day = adobe.addNode("cq");
//            day.setProperty("message", "Adobe Experience Manager is part of the Adobe Digital Marketing Suite!");

            // Save the session changes and log out
            session.save();
            session.logout();
        }
        catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }


}