package uk.co.greenjam.jrgfinance.core.services.impl;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.fd.output.api.OutputService;
import com.adobe.fd.output.api.OutputServiceException;
import com.adobe.fd.output.api.PDFOutputOptions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.greenjam.jrgfinance.core.services.PDFGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component(
        immediate = true,
        service = PDFGenerator.class,
        configurationPid = "uk.co.greenjam.jrgfinance.core.services.impl.PDFGeneratorImpl"
)
public class PDFGeneratorImpl implements PDFGenerator {

    private static final String OUTPUT_FOLDER = "/Users/Jim";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Reference
    private OutputService outputService;

    @Override
    public void generatePDF(String templateLocation, String xmlString, String saveLocation) {
        logger.info("Generate PDF!");


        Document doc=null;
        try {

            PDFOutputOptions option = new PDFOutputOptions();

            option.setAcrobatVersion(com.adobe.fd.output.api.AcrobatVersion.Acrobat_11);

            Document xdpDoc = new Document(templateLocation);


            doc = outputService.generatePDFOutput( xdpDoc ,new Document(xmlString.getBytes()),option);

            File toSave = new File(OUTPUT_FOLDER,"contact.pdf");

            doc.copyToFile(toSave);

        } catch (OutputServiceException e) {
            logger.error("Error", e);
        }catch (FileNotFoundException e) {
            logger.error("Error", e);
        } catch (IOException e) {
            logger.error("Error", e);
        } finally {
            doc.dispose();
        }
    }



}