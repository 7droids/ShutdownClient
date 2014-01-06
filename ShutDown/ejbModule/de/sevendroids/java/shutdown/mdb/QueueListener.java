package de.sevendroids.java.shutdown.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Message-Driven Bean implementation class for: QueueListener
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "SevenDroidsQueue") }, mappedName = "jms/SevenDroidsQueue")
public class QueueListener implements MessageListener {

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	@Override
	public void onMessage(Message message) {
		TextMessage tm = (TextMessage) message;
		try {
			System.out.println(getClass().getName() + ": " + tm.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
