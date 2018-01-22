package uk.co.greenjam.jrgfinance.core.services;

public interface PDFGenerator {
    void generatePDF(String template, String xmlString, String saveLocation);
}
