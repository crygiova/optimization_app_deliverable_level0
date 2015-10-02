package fi.aalto.itia.saga.simulation.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author giovanc1
 *
 *         Class which represents a message between the actors in the scenario
 *
 */
public class SimulationMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 15L;
	/**
	 * SimulationElement sender of the message
	 */
	private String sender;
	/**
	 * SimulationElement receiver of the message
	 */
	private String receiver;
	/**
	 * Header of the message. The Header can be used to identify the content of
	 * the message
	 */
	private String header;
	/**
	 * Serializable object with the content of the message.
	 */
	private Serializable content;

	public SimulationMessage(String sender,
			String receiver, String header, Serializable content) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.header = header;
		this.content = content;
	}

	public String getSender() {
		return sender;
	}

	public String getReceiver() {
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
	
	public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

}
