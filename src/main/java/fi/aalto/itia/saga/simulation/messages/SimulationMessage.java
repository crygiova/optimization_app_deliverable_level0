package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;

import fi.aalto.itia.saga.simulation.SimulationElement;


//TODO Make it Abstract if necessary or Not
public class SimulationMessage {

	private SimulationElement sender;
	private SimulationElement receiver;
	private String header;
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
