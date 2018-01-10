
package uk.co.greenjam.jrgfinance.core.servlets;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;


@Component(service=Servlet.class,
        property={
                Constants.SERVICE_DESCRIPTION + "=Form Submit Servlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.paths="+ "/bin/formSubmit"
        })
public class FormSubmission extends SlingAllMethodsServlet{
    private static final long serialVersionUid = 1L;
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(FormSubmission.class);

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws ServletException, IOException {
        logger.info("Form submit");
        System.out.println("Form submit");
    }


}
