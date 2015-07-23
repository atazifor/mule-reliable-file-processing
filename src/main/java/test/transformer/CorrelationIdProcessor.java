package test.transformer;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.param.InboundHeaders;
import org.mule.api.lifecycle.Callable;

public class CorrelationIdProcessor implements Callable {
	
	
	//synchronized so that only one thread can have access to this at one time
	public synchronized Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		String originalFilename = message.getInboundProperty("originalFilename");
		String correlationId = getCorrelationId(originalFilename);
		message.setCorrelationId(correlationId);
		return message;
	}
	
	private String getCorrelationId(String originalFilename){
		return originalFilename.substring(0,2);
	}

}
