
package uk.co.greenjam.jrgfinance.core.servlets;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import uk.co.greenjam.jrgfinance.core.services.PDFGenerator;
import uk.co.greenjam.jrgfinance.core.services.impl.PDFGeneratorImpl;
import uk.co.greenjam.jrgfinance.core.servlets.model.AfData;

import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;


@Component(service=Servlet.class,
        property={
                Constants.SERVICE_DESCRIPTION + "=Form Submit Servlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.paths="+ "/bin/formSubmit"
        })
public class FormSubmission extends SlingAllMethodsServlet{
    private static final long serialVersionUid = 1L;
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(FormSubmission.class);

    @Reference
    private PDFGenerator pdfGenerator;

    @Activate
    protected void activate(ComponentContext context) throws UnknownHostException {
        logger.info("Activating " + this.getClass());
    }

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws ServletException, IOException {
        logger.info("Form submit");

        RequestParameterMap requestParameterMap = req.getRequestParameterMap();
        RequestParameter dataXml = requestParameterMap.getValue("dataXml");
        String xmlString = dataXml.toString();


        // Convert string to XML Element
        Element xmlData = createXMLElement(xmlString);
        if (xmlData == null){
            // TODO write error
            return;
        }
        else {
            // Convert xml to afData pojo
            AfData afData = convertXMLtoPojo(xmlData);
            if(afData == null) {
                // TODO write error
                return;
            }
            else {
                logger.info("Name = " + afData.getAfUnboundData().getData().getName());
                logger.info("Email = " + afData.getAfUnboundData().getData().getEmailAddress());
                logger.info("Message = " + afData.getAfUnboundData().getData().getMessage());

                pdfGenerator.generatePDF("","","");
            }

        }


    }

    private AfData convertXMLtoPojo(Element xmlData) {
        JAXBContext jaxbContext = null;
        AfData afData = null;

        try {
            jaxbContext = JAXBContext.newInstance(AfData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            afData = (AfData) jaxbUnmarshaller.unmarshal(xmlData);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return afData;

    }

    private Element createXMLElement(String xmlString) {
        Element xmlData = null;
        try {
            xmlData =  DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlString.getBytes()))
                    .getDocumentElement();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xmlData;
    }


}
