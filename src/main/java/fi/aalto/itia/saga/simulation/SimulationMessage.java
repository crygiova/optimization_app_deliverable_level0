package fi.aalto.itia.saga.simulation;


//TODO Make it Abstract if necessary or Not
public class SimulationMessage {

	private SimulationElement sender;
	private SimulationElement receiver;
	private String header;

	public SimulationMessage(SimulationElement sender,
			SimulationElement receiver, String header) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.header = header;
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

	@Override
	public String toString() {
		return "SimulationMessage [sender=" + sender + ", receiver=" + receiver
				+ ", header=" + header + "]";
	}

}
