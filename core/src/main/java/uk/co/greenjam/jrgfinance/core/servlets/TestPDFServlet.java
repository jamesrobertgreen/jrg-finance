package uk.co.greenjam.jrgfinance.core.servlets;

import com.adobe.fd.output.api.OutputService;
import com.adobe.fd.output.api.OutputServiceException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.UnknownHostException;
import javax.servlet.Servlet;
import com.adobe.fd.output.api.PDFOutputOptions;
import com.adobe.aemfd.docmanager.Document;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/testpdf",
                "sling.auth.requirements=-/bin/testpdf"
        }
)
public class TestPDFServlet extends SlingAllMethodsServlet {

    private static final String PATH_TO_XDP = "/Users/Jim/contact.xdp";
    private static final String PATH_TO_XML = "/Users/Jim/contact.xml";
    private static final String OUTPUT_FOLDER = "/Users/Jim";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private OutputService outputService;

    @Activate
    protected void activate(ComponentContext context) throws UnknownHostException {
        logger.info("Activating " + this.getClass());
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException{

        Document doc=null;

        try {

            PDFOutputOptions option = new PDFOutputOptions();

            option.setAcrobatVersion(com.adobe.fd.output.api.AcrobatVersion.Acrobat_11);

            InputStream inputXMLStream = new FileInputStream(PATH_TO_XML);

            InputStream templateStream = new FileInputStream(PATH_TO_XDP);

            doc = outputService.generatePDFOutput(new Document(templateStream),new Document(inputXMLStream),option);

            File toSave = new File(OUTPUT_FOLDER,"contact.pdf");

            doc.copyToFile(toSave);

        } catch (OutputServiceException e) {
            logger.error("Error", e);
        }catch (FileNotFoundException e) {
            logger.error("Error", e);
        } catch (IOException e) {
            logger.error("Error", e);
        }finally {
            doc.dispose();
        }
    }

}