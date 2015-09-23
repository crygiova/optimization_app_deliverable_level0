package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;

import fi.aalto.itia.saga.simulation.SimulationElement;

/**
 * @author giovanc1
 *
 *         Class which represents a message between the actors in the scenario
 *
 */
public class SimulationMessage {

	/**
	 * SimulationElement sender of the message
	 */
	private SimulationElement sender;
	/**
	 * SimulationElement receiver of the message
	 */
	private SimulationElement receiver;
	/**
	 * Header of the message. The Header can be used to identify the content of
	 * the message
	 */
	private String header;
	/**
	 * Serializable object with the content of the message.
	 */
	private Serializable content;

	public SimulationMessage(SimulationElement sender,
			SimulationElement receiver, String header, Serializable content) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.header = header;
		this.content = content;
	}

	public SimulationElement getSender() {
		return sender;
	}

	public SimulationElement getReceiver() {
		return receiver;
	}

	public String getHeader() {
		return header;
	}

	public Serializable getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "SimulationMessage [sender=" + sender + ", receiver=" + receiver
				+ ", header=" + header + "]";
	}

}
