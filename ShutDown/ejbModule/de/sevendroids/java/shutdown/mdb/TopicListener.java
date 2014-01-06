package de.sevendroids.java.shutdown.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Message-Driven Bean implementation class for: TopicListener
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "SevenDroidsTopic"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
		@ActivationConfigProperty(propertyName = "clientId", propertyValue = "SevenDroidsId"),
		@ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "sevenDroidsDurableTopic"), }, mappedName = "jms/SevenDroidsTopic")
public class TopicListener implements MessageListener {

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			try {
				System.out.println(getClass().getName() + ": " + tm.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
