package uk.co.greenjam.jrgfinance.core.services.impl;


import com.adobe.aemfd.docmanager.Document;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.services.CRXUtilService;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.io.ByteStreams.toByteArray;


@Component(
        immediate = true,
        service = CRXUtilService.class,
        configurationPid = "uk.co.greenjam.jrgfinance.core.services.impl.CRXUtilServiceImpl"
)
public class CRXUtilServiceImpl implements CRXUtilService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Session session;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Activate
    protected void activate(Map<String, String> config) {
        logger.info("Activating " + this.getClass());

        Map<String, Object> param = new HashMap<String, Object>();

        param.put(ResourceResolverFactory.SUBSERVICE, "jrg-finance");
        ResourceResolver resolver = null;

        try {

            //Invoke the getServiceResourceResolver method to create a Session instance
            resolver = resourceResolverFactory.getServiceResourceResolver(param);
            session = resolver.adaptTo(Session.class);
            logger.info("Session created");

        } catch (LoginException e) {
            logger.error("session could not be created");
            e.printStackTrace();
        }
    }



    @Override
    public Document retrieveDocumentFromCRXRepository(String path) throws RepositoryException, IOException {
        byte[] result = retrieveContentFromCRXRepository(path);
        Document document = convertByteArrayToDocument(result);

        return document;
    }


    private Document convertByteArrayToDocument(byte[] content) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            doc = (Document) builder.parse(new ByteArrayInputStream(content));
        } catch (ParserConfigurationException e){
            logger.error("Unable to read document. ", e);
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private byte[] retrieveContentFromCRXRepository(String path) throws RepositoryException, IOException {
        byte[] result = null;

        InputStream is = null;
        BufferedInputStream bin = null;
        try {
            Node ntFileNode = session.getNode(path);
            is = ntFileNode.getProperty("jcr:data").getBinary().getStream();
            bin = new BufferedInputStream(is);
            result = toByteArray(bin);
        } finally {
            if (bin != null){
                bin.close();
            }
            if (is != null){
                is.close();
            }
        }
        return result;
    }
}
