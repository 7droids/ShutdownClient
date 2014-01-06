/**
 * 
 */
package de.sevendroids.java.shutdown.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.sevendroids.java.shutdown.message.ShutdownInfo;

/**
 * This class is a small client that uses JMS to send and receive messages. With
 * one of the messages a delayed shutdown of all listening clients can be
 * performed.
 * 
 * @author 7droids.de (FA)
 * 
 */
public class JMSClient extends JFrame implements MessageListener {
	private static final long serialVersionUID = -495916699282092634L;
	private static final String CONNECTION_FACTORY = "jms/SevenDroidsConnectionFactory";
	private static final String CONNECTION_TOPIC = "jms/SevenDroidsTopic";

	private JTextField messageLog;
	private JTextField timeTextField;
	private Context jndiContext = null;
	private TopicConnectionFactory connectionFactory = null;
	private TopicConnection connection = null;
	private Topic topic = null;

	public JMSClient() {
		initConnection2AS();
		createGUI();
	}

	/**
	 * Connect to the application server and register as a subscriber to a
	 * topic.
	 */
	private void initConnection2AS() {
		try {
			// Get the JNDI Context
			jndiContext = new InitialContext();
			// Create new topic
			topic = (Topic) jndiContext.lookup(CONNECTION_TOPIC);

			// Create the Connection Factory
			connectionFactory = (TopicConnectionFactory) jndiContext
					.lookup(CONNECTION_FACTORY);
			connection = connectionFactory.createTopicConnection();
			TopicSession topicSession = connection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			TopicSubscriber topicSubscriber = topicSession
					.createSubscriber(topic);
			topicSubscriber.setMessageListener(this);
			// will use the onMessage() method below
			connection.start();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a really simple GUI with some buttons and text fields.
	 */
	private void createGUI() {
		setTitle("JMS-Client");
		setLayout(new BorderLayout());
		messageLog = new JTextField();
		messageLog.setEditable(false);
		add(messageLog, BorderLayout.SOUTH);
		JButton sendButton = new JButton("Send message");
		sendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessageToTopic();
			}

		});
		add(sendButton, BorderLayout.NORTH);

		// Create a new panel with a text field for time input and a button to
		// send the new message
		JPanel shutdownPanel = new JPanel(new BorderLayout());
		add(shutdownPanel, BorderLayout.CENTER);
		timeTextField = new JTextField(10);
		JButton shutdownButton = new JButton("Shutdown");
		shutdownButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createAndSendShutdownMessage();
			}
		});
		shutdownPanel.add(timeTextField, BorderLayout.CENTER);
		shutdownPanel.add(shutdownButton, BorderLayout.EAST);
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		// To close the connections during exit of the application, I register a
		// shutdown hook.
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					if (connection != null) {
						connection.close();
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		setVisible(true);
	}

	/**
	 * Here an ObjectMessage is created and send. The send object contains all
	 * information needed to perform a shutdown at the correct time.
	 */
	protected void createAndSendShutdownMessage() {
		String time = timeTextField.getText();
		String[] timeElements = time.split(":");
		Calendar shutDownCalendar = Calendar.getInstance();
		// if timeElements contains 2 Elements than try to interpret as time
		if (timeElements.length == 2) {
			// This should be created more robust
			shutDownCalendar.add(Calendar.HOUR_OF_DAY,
					Integer.parseInt(timeElements[0]));
			shutDownCalendar.add(Calendar.MINUTE,
					Integer.parseInt(timeElements[1]));
		}
		ShutdownInfo info = new ShutdownInfo(
				"The client will automatically shutdown\nat "
						+ DateFormat.getDateTimeInstance().format(
								shutDownCalendar.getTime()) + ".",
				shutDownCalendar.getTime());
		try {
			// Create the session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Create Message Producer
			MessageProducer producer = session.createProducer(topic);

			// Send ObjectMessage
			ObjectMessage objMessage = session.createObjectMessage();
			objMessage.setObject(info);
			producer.send(objMessage);
		} catch (JMSException e) {
			e.printStackTrace();
			messageLog.setText(e.getMessage());
		}
	}

	/**
	 * Create and send a simple TextMessage with current date to the topic
	 */
	protected void sendMessageToTopic() {
		try {
			// Create the session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Create Message Producer
			MessageProducer producer = session.createProducer(topic);

			// Send TextMessage
			TextMessage textMessage = session.createTextMessage();
			textMessage.setText("Message send at: " + new Date());
			producer.send(textMessage);
		} catch (JMSException e) {
			e.printStackTrace();
			messageLog.setText(e.getMessage());
		}
	}

	/**
	 * MessageListener method that will be called when ever a new message
	 * arrives at the connected topic
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		try {
			// Handling text messages
			if (message instanceof TextMessage) {
				TextMessage tm = (TextMessage) message;
				System.out.println(getClass().getName() + ": " + tm.getText());
				messageLog.setText("Received: " + tm.getText());
			} else
			// Handling object messages
			if (message instanceof ObjectMessage) {
				ObjectMessage om = (ObjectMessage) message;
				// Checking the type of the object in the message
				if (om.getObject() instanceof ShutdownInfo) {
					final ShutdownInfo sdi = (ShutdownInfo) om.getObject();
					System.out.println(sdi.getMessage());
					messageLog.setText(sdi.getMessage());
					// performing the shutdown
					Thread shutdownThread = new Thread() {
						@Override
						public void run() {
							// Create a runnable to display the dialog in the
							// AWTEventQueue
							Runnable dialogRun = new Runnable() {
								@Override
								public void run() {
									JOptionPane.showMessageDialog(null,
											sdi.getMessage(),
											"Shutdown of the client",
											JOptionPane.WARNING_MESSAGE);
								}
							};
							SwingUtilities.invokeLater(dialogRun);
							try {
								// Calculate how long to wait before shutdown
								long delay = sdi.getShutdownTime().getTime()
										- System.currentTimeMillis();
								// we should wait at least 10 seconds
								if (delay < 10000)
									delay = 10000;
								sleep(delay);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							System.exit(0);
						}
					};
					shutdownThread.start();
				}
			} else {
				// Handling unknown messages
				messageLog.setText("Unknown message type");
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new JMSClient();
	}
}
