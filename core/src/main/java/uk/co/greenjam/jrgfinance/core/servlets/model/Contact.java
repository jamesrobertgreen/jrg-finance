package uk.co.greenjam.jrgfinance.core.servlets.model;

public class Contact
{
    private AfData afData;

    public AfData getAfData ()
    {
        return afData;
    }

    public void setAfData (AfData afData)
    {
        this.afData = afData;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [afData = "+afData+"]";
    }
}

