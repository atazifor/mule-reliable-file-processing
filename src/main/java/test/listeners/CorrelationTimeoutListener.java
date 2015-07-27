package test.listeners;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.notification.RoutingNotificationListener;
import org.mule.context.notification.RoutingNotification;


public class CorrelationTimeoutListener implements
		RoutingNotificationListener<RoutingNotification> {
	
	public String getDlqAddress() {
		return dlqAddress;
	}
	/**
	 * dead queue letter address; vm or jms queue to hold messages there were not processed.
	 * @param dlqAddress
	 */
	public void setDlqAddress(String dlqAddress) {
		this.dlqAddress = dlqAddress;
	}
	public String dlqAddress;
	@Override
	public void onNotification(RoutingNotification notification) {
		if(RoutingNotification.CORRELATION_TIMEOUT == notification.getAction() //if this is a correlation timeout
				|| RoutingNotification.MISSED_AGGREGATION_GROUP_EVENT == notification.getAction()){//missed an item in a correlation group
			MuleMessage message = (MuleMessage)notification.getSource();
			System.out.println("Failed messages: " + message.getPayload());
			MuleContext muleContext = message.getMuleContext();
			try {
				muleContext.getClient().send("vm://default.error", message);
			} catch (MuleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
