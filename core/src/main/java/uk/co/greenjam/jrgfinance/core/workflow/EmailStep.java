package uk.co.greenjam.jrgfinance.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        immediate = true,
        service = EmailStep.class,
        configurationPid = "uk.co.greenjam.jrgfinance.core.workflow.impl.EmailStep"
)
public class EmailStep implements WorkflowProcess
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    //Inject a MessageGatewayService
    @Reference
    private MessageGatewayService messageGatewayService;

    public void execute(WorkItem item, WorkflowSession wfsession,MetaDataMap args) throws WorkflowException {

        try
        {
            log.info("Email Step Executing");    //ensure that the execute method is invoked

            //Declare a MessageGateway service
            MessageGateway<Email> messageGateway;

            //Set up the Email message
            Email email = new SimpleEmail();

            //Set the mail values
            String emailToRecipients = "jrg-finance@mailinator.com";

            email.addTo(emailToRecipients);
            email.setSubject("Email Step");
            email.setFrom("jrg-finance@greenjam.co.uk");
            email.setMsg("This message has been sent from a custom workflow");

            //Inject a MessageGateway Service and send the message
            messageGateway = messageGatewayService.getGateway(Email.class);

            // Check the logs to see that messageGateway is not null
            messageGateway.send((Email) email);
        } catch (EmailException e) {
            e.printStackTrace();
        }


    }

}