package uk.co.greenjam.jrgfinance.core.servlets.model;

import javax.xml.bind.annotation.XmlAttribute;


public class Data {

    private String name;
    private String emailAddress;
    private String message;

    public Data(){};
    public Data(String name, String emailAddress, String message) {
        super();
        this.name = name;
        this.emailAddress = emailAddress;
        this.message = message;
    }
    @XmlAttribute
    public String getName() {
        return name;
    }
    @XmlAttribute
    public String getEmailAddress() {
        return emailAddress;
    }
    @XmlAttribute
    public String getMessage() {
        return message;
    }

}
