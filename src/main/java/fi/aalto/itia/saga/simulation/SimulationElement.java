package fi.aalto.itia.saga.simulation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.simulation.messages.SimulationMessage;

/**
 * @author giovanc1
 *
 */
public abstract class SimulationElement implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 55L;
	// TODO make it client server, means that the AG can have a list of clients
	// which
	// Queue to take the token from and release it from the Simulator
	protected SynchronousQueue<Integer> simulationToken;
	// Message queue
	protected LinkedBlockingQueue<SimulationMessage> messageQueue;
	// Input queue
	protected String inputQueueName;
	private Connection connection;
	protected static final String EXCHANGE_NAME = "Ag_Publisher";
	protected Channel dRChannel = null;
	// Tasks ordered by priority and time
	protected PriorityQueue<TaskSchedule> tasks;
	// Count down to communicate that the current R0Abstract has finished it s
	// own tasks,
	protected CountDownLatch endOfSimulationTasks;
	private boolean countDown = false;
	// releaseToken for releasing the simulationToken to the simulator
	private boolean releaseToken = false;
	// to quit the simulation
	private boolean endOfSimulation = false;
	protected SimulationCalendar calendar;

	public SimulationElement(String inputQueueName) {
		super();
		this.simulationToken = new SynchronousQueue<Integer>();
		this.messageQueue = new LinkedBlockingQueue<SimulationMessage>();
		this.inputQueueName = inputQueueName;
		this.tasks = new PriorityQueue<TaskSchedule>();
		calendar = SimulationCalendar.getInstance();
		try {
			dRChannel = createMqChannel();
			dRChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			dRChannel.queueDeclare(inputQueueName, false, false, false, null);
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getInputQueueName() {
		return inputQueueName;
	}

	public void startConsumingMq() {
		Consumer consumer = new DefaultConsumer(dRChannel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				SimulationMessage sm = null;
				try {
					sm = (SimulationMessage) SimulationMessage
							.deserialize(body);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (sm != null)
					addMessage(sm);
			}
		};
		try {
			dRChannel.basicConsume(inputQueueName, true, consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * this method is used to schedule the daily tasks of a simulation element
	 */
	public abstract void scheduleTasks();

	/**
	 * Used to execute the tasks for the current hour
	 */
	public abstract void executeTasks();

	/**
	 * This method is used to elaborate the messages received in input from
	 * other Simulation elements
	 */
	public abstract void elaborateIncomingMessages();

	/**
	 * @param cdl
	 */
	public void updateEndOfSimulationTasks(CountDownLatch cdl) {
		this.endOfSimulationTasks = cdl;
		this.countDown = true;
	}

	/**
	 * 
	 */
	public void notifyEndOfSimulationTasks() {
		if (countDown) {
			this.endOfSimulationTasks.countDown();
			countDown = false;
		}
	}

	/**
	 * @return
	 */
	public Integer takeSimulationToken() {
		Integer buffer = -1;
		try {
			buffer = this.simulationToken.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	/**
	 * 
	 */
	public synchronized void releaseSimulationToken() {
		this.setReleaseToken(false);
		try {
			this.simulationToken.put(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Add a msg to this SimulationElement
	/**
	 * @param msg
	 */
	public void addMessage(SimulationMessage msg) {
		this.messageQueue.add(msg);
	}

	/**
	 * @param timeout
	 * @return
	 */
	public SimulationMessage pollMessageMs(long timeout) {
		try {
			return this.messageQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SimulationMessage waitForMessage() {
		try {
			return this.messageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Send a msg to a specified SimulationElement
	/**
	 * @param peer
	 * @param msg
	 * @throws IOException
	 */
	public void sendMessage(SimulationMessage msg) {
		String queueReceiver = msg.getReceiver();
		String corrId = java.util.UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrId).replyTo(this.inputQueueName).build();
		byte[] body;
		try {
			body = SimulationMessage.serialize(msg);
			dRChannel.basicPublish("", queueReceiver, props, body);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void publishMessage(SimulationMessage msg) {
		String corrId = java.util.UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrId).replyTo(this.inputQueueName).build();
		byte[] body;
		try {
			body = SimulationMessage.serialize(msg);
			dRChannel.basicPublish(EXCHANGE_NAME, "", props, body);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// // Send a msg to a specified SimulationElement
	// /**
	// * @param peer
	// * @param msg
	// */
	// public void sendMessage(SimulationMessage msg) {
	// msg.getReceiver().addMessage(msg);
	// }

	public boolean nextTaskAtThisHour() {
		return calendar.get(Calendar.HOUR_OF_DAY) == this.tasks.peek()
				.getHour();
	}

	/**
	 * @return
	 */
	public boolean isReleaseToken() {
		return releaseToken;
	}

	/**
	 * @param releaseToken
	 */
	public void setReleaseToken(boolean releaseToken) {
		this.releaseToken = releaseToken;
	}

	/**
	 * @return
	 */
	public boolean isEndOfSimulation() {
		return endOfSimulation;
	}

	/**
	 * @param endOfSimulation
	 */
	public synchronized void setEndOfSimulation(boolean endOfSimulation) {
		this.endOfSimulation = endOfSimulation;
	}

	public Channel createMqChannel() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		Channel channel = null;
		try {
			channel = connection.createChannel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return channel;
	}

	public void closeConnection() {
		try {
			dRChannel.close();
			connection.close();
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
