package uk.co.greenjam.jrgfinance.core.services.models;

import java.util.ArrayList;

public class StockPriceResponse {
    private String version;
    private String encoding;
    private Feed feed;

    private class Feed {
        private ArrayList<Entry> entry;
    }
    private class Entry{
        private Content content;

    }
    private class Content{
        private String $t;
    }

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

    public String getPrice() {
        return feed.entry.get(0).content.$t;
    }
}
