package uk.co.greenjam.jrgfinance.core.services.impl;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.fd.forms.api.FormsService;
import com.adobe.fd.output.api.OutputService;
import com.adobe.fd.output.api.OutputServiceException;
import com.adobe.fd.output.api.PDFOutputOptions;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.services.PDFGenerator;

import java.io.*;
import java.net.UnknownHostException;

@Component(
        immediate = true,
        service = PDFGenerator.class,
        configurationPid = "uk.co.greenjam.jrgfinance.core.services.impl.PDFGeneratorImpl"
)
public class PDFGeneratorImpl implements PDFGenerator {

    private static final String PATH_TO_XDP = "/Users/Jim/contact.xdp";
    private static final String OUTPUT_FOLDER = "/Users/Jim";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Reference
    private OutputService outputService;

    @Override
    public void generatePDF(String template, String xmlString, String saveLocation) {
        logger.info("Generate PDF!");


        Document doc=null;
        try {

            PDFOutputOptions option = new PDFOutputOptions();

            option.setAcrobatVersion(com.adobe.fd.output.api.AcrobatVersion.Acrobat_11);

            InputStream templateStream = new FileInputStream(PATH_TO_XDP);

            doc = outputService.generatePDFOutput(new Document(templateStream),new Document(xmlString.getBytes()),option);

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