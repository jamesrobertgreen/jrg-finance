
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.co.greenjam.jrgfinance.core.services.PDFGenerator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
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

    private final static String UNBOUNDDATA_XPATH = "\"/afData/afUnboundData/*\"";

    private final static String TEMPLATE_BASE = "/content/dam/formsanddocuments/jrg-finance/templates/";
    private final static String TEMPLATE_DATA = "/jcr:content/renditions/original/jcr:content";

    private final static String CONTACT_TEMPLATE = TEMPLATE_BASE + "contact.xdp" + TEMPLATE_DATA;

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


        Document doc = convertToXMLDoc(xmlString);

        Node afUnboundData = getXML( doc, UNBOUNDDATA_XPATH);

        pdfGenerator.generatePDF(CONTACT_TEMPLATE,convertNodeToString(afUnboundData),"");

    }

    private Document convertToXMLDoc(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc = null;

        try {
            builder = factory.newDocumentBuilder();
            factory = DocumentBuilderFactory.newInstance();
            doc = builder.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes("utf-8"))));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private static Node getXML(Document doc, String xpathStr) {
        // Create XPathFactory object
        XPathFactory xpathFactory = XPathFactory.newInstance();

        // Create XPath object
        XPath xpath = xpathFactory.newXPath();

        Node node = null;
        try {
            XPathExpression expr =
                    xpath.compile(xpathStr);
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return node;
    }


    public static String convertNodeToString(Node node){
        Transformer t = null;
        StringWriter sw = null;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            sw = new StringWriter();
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }
}


