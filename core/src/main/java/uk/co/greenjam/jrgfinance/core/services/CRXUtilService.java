package uk.co.greenjam.jrgfinance.core.services;


import com.adobe.aemfd.docmanager.Document;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;


public interface CRXUtilService {
    public Document retrieveDocumentFromCRXRepository(String path) throws RepositoryException, IOException;

}
