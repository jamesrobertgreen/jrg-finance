package uk.co.greenjam.jrgfinance.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Stock Price Service")
public @interface Configuration {

    String DEFAULT_API_URL = "https://spreadsheets.google.com/feeds/list/0AhySzEddwIC1dEtpWF9hQUhCWURZNEViUmpUeVgwdGc/1/public/basic?alt=json&sq=symbol=";

    @AttributeDefinition(
            name = "Stock Price API URL",
            description = "The API URL to use to retrieve stock prices",
            type = AttributeType.STRING
    )
    String stockprice_apiurl_string() default DEFAULT_API_URL;
}