/**
 * 
 */
package de.sevendroids.java.shutdown.message;

import java.io.Serializable;
import java.util.Date;

/**
 * A simple class containing shutdown information. This information can be send
 * to any connected JMS client.
 * 
 * @author 7droids.de (FA)
 * 
 */
public class ShutdownInfo implements Serializable {

	private static final long serialVersionUID = 1754171977646245336L;

	private final String message;
	private final Date shutdownTime;

	/**
	 * @param message
	 * @param shutdownTime
	 */
	public ShutdownInfo(String message, Date shutdownTime) {
		this.message = message;
		this.shutdownTime = shutdownTime;
	}

	/**
	 * @return the message
	 */
	public final String getMessage() {
		return message;
	}

	/**
	 * @return the shutdownTime
	 */
	public final Date getShutdownTime() {
		return shutdownTime;
	}
}
