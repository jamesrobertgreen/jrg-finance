package uk.co.greenjam.jrgfinance.core.servlets.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "uk.co.greenjam.jrgfinance.core.servlets.model.AfData")
public class AfUnboundData {
    private Data data;

    @XmlElement(name="data")
    public Data getData() {
        return data;
    }
}
