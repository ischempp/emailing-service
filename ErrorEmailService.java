package org.fhcrc.common.services;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.webconsole.plugins.event.internal.OsgiUtil;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.mail.MailTemplate;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;

@Component(name = "org.fhcrc.common.service.ErrorEmailService", 
	label = "Fred Hutch - Send Error Email Service",
	description = "Fred Hutch - Service used to send emails to warn stakeholders of fatal errors",
	metatype = true)
@Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Service used to send emails to warn stakeholders of fatal errors"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Fred Hutch", propertyPrivate = true),
	@Property(name = "service.toAddress", value = "ischempp@fredhutch.org", 
		description = "Email address to which error emails should be sent if no override is provided", 
		label = "Default TO address"),
	@Property(name = "service.fromAddress", value = "websys@fredhutch.org", 
		description = "Email address from which error emails should be sent if no override is provided", 
		label = "Default FROM address"),
	@Property(name = "service.subjectLine", value = "[AEM] - An error has occurred", 
		description = "Subject line for error emails sent if no override is provided", 
		label = "Default SUBJECT LINE")
})

@Service(value = EmailService.class)
public class ErrorEmailService implements EmailService {
	
	private final String LOGGING_PREFIX = "ERROR EMAILING SERVICE";
	
	private String toAddress,
		fromAddress,
		subjectLine;
	
	protected final Logger log = LoggerFactory.getLogger(ErrorEmailService.class);
	
	@Reference
    private MessageGatewayService messageGatewayService;
	
	@Activate
	private void activate(ComponentContext context) {	
		
        log.info("Activating Fred Hutch - LMS Importer Service. " + this.getClass().getName());
        
        Dictionary<?, ?> properties = context.getProperties();
        toAddress = OsgiUtil.toString(properties, "service.toAddress", new String());
        fromAddress = OsgiUtil.toString(properties, "service.fromAddress", new String());
        subjectLine = OsgiUtil.toString(properties, "service.subjectLine", new String());
        
    }
	
	public void sendEmail() {
		
		HtmlEmail email = new HtmlEmail();
		try {
			email.setHtmlMsg("This is an email message");
			
			List<InternetAddress> emailRecipients = new ArrayList<InternetAddress>();	
			emailRecipients.add(new InternetAddress(toAddress));
			
			if(emailRecipients.size() != 0) {
	    		email.setTo(emailRecipients);
	    		email.setFrom(fromAddress);
	    		email.setSubject(subjectLine);
	    		MessageGateway<HtmlEmail> messageGateway = messageGatewayService.getGateway(HtmlEmail.class);
	    		messageGateway.send(email);
	    	}
			
		} catch (EmailException e) {
			log.error(LOGGING_PREFIX + "Problem setting email content", e);
		} catch (AddressException e) {
			log.error(LOGGING_PREFIX + "Problem setting email address", e);
		}
		
	}

}
