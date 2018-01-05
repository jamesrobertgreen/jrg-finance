package uk.co.greenjam.jrgfinance.core.services.models;

public class StockPriceResponse {
    private String version;
    private String encoding;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
