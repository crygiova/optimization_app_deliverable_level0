package fi.aalto.itia.saga.aggregator;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.aggregator.util.SpotPriceEstimator;
import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.simulation.SimulationMessage;

public class Aggregator extends SimulationElement {

	private final String DAY_AHEAD_TASK = "DAYAHEAD";

	ArrayList<SimulationElement> prosumers;

	private final Logger log = Logger.getLogger(Aggregator.class);

	public Aggregator() {
		super();
	}

	public void setProsumers(ArrayList<SimulationElement> prosumers) {
		this.prosumers = prosumers;
	}

	/*
	 * @Override public void run() { while (true) { // take the token and start
	 * working this.takeSimulationToken(); // TODO Do the all the operations
	 * needed at the calendar time // TODO check message queue and keep doing it
	 * till is not empty // Notify the Simulator that the operations are
	 * finished this.notifyEndOfSimulationTasks(); // TODO check message queue
	 * and keep doing it till the main Thread // update release // TODO use
	 * release to releaseSimulationToken this.releaseSimulationToken(); } }
	 */

	@Override
	public void run() {
		while (!this.isEndOfSimulation()) {
			// take the token and start working
			this.takeSimulationToken();
			if (!this.isEndOfSimulation()) {
				// TODO Do the all the operations needed at that time
				executeTasks();
				// TODO check message queue and keep doing it till is not empty
				// Notify the Simulator that the operations are finished
				this.notifyEndOfSimulationTasks();
				log.debug("AggEndOfSimTasks");
				// TODO check message queue and keep doing it till the main
				// Thread
				// TODO DELETE Communication Test
				while (!this.isReleaseToken() || !this.messageQueue.isEmpty() && !this.isEndOfSimulation()) {
					SimulationMessage str = this.pollMessageMs(10);
					if (str != null) {
						log.debug("A<-P: " + str.getHeader());
						// this.sendMsg(str + " Risposta dopo CountDown Run0");
					}
				}
			}
			// TODO use release to putSq and reset release to false
			this.releaseSimulationToken();
			//log.debug("Loop");
		}
		log.debug("EndOfSimulation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fi.aalto.itia.saga.simulation.SimulationElement#scheduleTasks()
	 */
	@Override
	public void scheduleTasks() {
		this.tasks.add(new TaskSchedule(DAY_AHEAD_TASK, 12, 1));
	}

	@Override
	public void executeTasks() {
		// schedule tasks at midnight
		if (SimulationCalendarUtils
				.isMidnight(SimulationCalendar.getInstance())) {
			scheduleTasks();

		}
		// execute tasks for the current hour
		while (!this.tasks.isEmpty() && this.nextTaskAtThisHour()) {
			TaskSchedule currentTask = this.tasks.remove();
			log.debug(SimulationCalendar.getInstance().getTime() + " Task "
					+ currentTask.getTaskName());
			switch (currentTask.getTaskName()) {
			case DAY_AHEAD_TASK:
				dayAheadTask();
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void elaborateIncomingMessages() {
		// TODO Auto-generated method stub

	}

	private void dayAheadTask() {
		Date midnight = SimulationCalendarUtils.getDayAheadMidnight(calendar
				.getTime());
		TimeSequencePlan spotPriceDayAhead = SpotPriceEstimator.getInstance()
				.getSpotPrice(midnight);

		SimulationMessage sm = new SimulationMessage(this, prosumers.get(0),
				"AToP");
		this.sendMessage(sm);

	}

	private void communicationTest() {
		// TODO DELETE Communication Test
		for (int i = 0; i < 3; i++) {
			SimulationMessage sm = new SimulationMessage(this,
					prosumers.get(0), "AToP " + i);
			log.debug("A->P: " + sm.getHeader());
			this.sendMessage(sm);

			log.debug("A<-P:TAKE " + this.takeMessage().getHeader());
			// System.out.println(this.takeMsg());
		}
	}
}
