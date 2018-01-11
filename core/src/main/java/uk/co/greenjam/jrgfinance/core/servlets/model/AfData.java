package uk.co.greenjam.jrgfinance.core.servlets.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "afData")
public class AfData {
    private AfUnboundData afUnboundData;

    @XmlElement(name="afUnboundData")
    public AfUnboundData getAfUnboundData() {
        return afUnboundData;
    }

}