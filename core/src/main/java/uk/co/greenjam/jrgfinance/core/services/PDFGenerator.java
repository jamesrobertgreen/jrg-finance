package uk.co.greenjam.jrgfinance.core.services;

import uk.co.greenjam.jrgfinance.core.servlets.model.AfData;

public interface PDFGenerator {
    void generatePDF(String template, String xmlString, String saveLocation);
}
