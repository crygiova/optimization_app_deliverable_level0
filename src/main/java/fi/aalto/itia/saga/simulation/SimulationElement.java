package fi.aalto.itia.saga.simulation;

import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import fi.aalto.itia.saga.data.TaskSchedule;

public abstract class SimulationElement implements Runnable {

	// TODO make it client server, means that the AG can have a list of clients
	// which
	// TODO add end of simulation parameter to quit the simulation.
	// at the moment are peers and the Prosumers they can have only a server at
	// the moment
	// Queue to take the token from and release it from the Simulator
	protected SynchronousQueue<Integer> simulationToken;
	// Messages queue between R0Abstract
	// TODO The type of the message needs to be changed once it has been
	// developed a format for the messages
	protected LinkedBlockingQueue<SimulationMessage> messageQueue;
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
	

	public SimulationElement() {
		super();
		this.simulationToken = new SynchronousQueue<Integer>();
		this.messageQueue = new LinkedBlockingQueue<SimulationMessage>();
		this.tasks = new PriorityQueue<TaskSchedule>();
		calendar = SimulationCalendar.getInstance();
	}

	/**
	 * 
	 */
	public abstract void scheduleTasks();

	public abstract void executeTasks();

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

	// Send a msg to a specified SimulationElement
	/**
	 * @param peer
	 * @param msg
	 */
	public void sendMessage( SimulationMessage msg) {
		msg.getReceiver().addMessage(msg);
	}

	/**
	 * @return
	 */
	public SimulationMessage takeMessage() {
		try {
			return this.messageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean nextTaskAtThisHour() {
		return calendar.get(Calendar.HOUR_OF_DAY) == this.tasks
				.peek().getHour();
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
	public void setEndOfSimulation(boolean endOfSimulation) {
		this.endOfSimulation = endOfSimulation;
	}

}
