package fi.aalto.itia.saga.aggregator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.aggregator.util.OptParamEstimator;
import fi.aalto.itia.saga.aggregator.util.SpotPriceEstimator;
import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.prosumer.Prosumer;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentRequest;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentResponse;
import fi.aalto.itia.saga.simulation.messages.SimulationMessage;

public class Aggregator extends SimulationElement {

	private final String DAY_AHEAD_TASK = "DAYAHEAD";
	private final String DAY_AHEAD_HEADER_REQUEST = "DA_Request";

	ArrayList<SimulationElement> prosumers;

	// TODO DOUBLE OR TIMESEQUENCEPLAN ??
	private TimeSequencePlan totalDayAheadConsumption;
	private TimeSequencePlan totalTodayConsumption;

	private final Logger log = Logger.getLogger(Aggregator.class);

	public Aggregator() {
		super();
		totalDayAheadConsumption = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
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
				// log.debug("AggEndOfSimTasks");
				// TODO check message queue and keep doing it till the main
				// Thread
				// TODO DELETE Communication Test
				while (!this.isReleaseToken() || !this.messageQueue.isEmpty()
						&& !this.isEndOfSimulation()) {
					SimulationMessage str = this.pollMessageMs(10);
					if (str != null) {
						log.debug("A<-P: " + str.getHeader());
						// this.sendMsg(str + " Risposta dopo CountDown Run0");
					}
				}
			}
			// TODO use release to putSq and reset release to false
			this.releaseSimulationToken();
			// log.debug("Loop");
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
			updateTodayScheduleConsumption();
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
		// Creating the header
		String header = DAY_AHEAD_HEADER_REQUEST;
		// creating the content of the message
		Date midnight = SimulationCalendarUtils.getDayAheadMidnight(calendar
				.getTime());
		double[] spotPrice = SpotPriceEstimator.getInstance()
				.getSpotPriceDouble(midnight);
		double w = OptParamEstimator.getInstance().getW(midnight);
		double tSize = OptParamEstimator.getInstance().getTSize(midnight);

		// won't change during the process
		final double[] tUpTotal = OptParamEstimator.getInstance().getTUp(
				midnight);
		final double[] tDwTotal = OptParamEstimator.getInstance().getTDw(
				midnight);
		double[] tUp = tUpTotal.clone();
		double[] tDw = tDwTotal.clone();

		for (SimulationElement prosumer : prosumers) {
			// Create content of the message
			int idProsumer = ((Prosumer) prosumer).getId();
			Serializable content = new DayAheadContentRequest(spotPrice, tUp,
					tDw, tSize, w, midnight);
			SimulationMessage sm = new SimulationMessage(this, prosumer,
					header, content);
			this.sendMessage(sm);
			// Wait for the response of the Prosumer means that at the moment is
			// synchronous communication
			SimulationMessage response = this.waitForMessage();
			log.debug(idProsumer + " Responded successful!! "
					+ response.getHeader() + "\n"
					+ response.getContent().toString());
			// TODO Ask Olli!!!! Update TArget and total Consumption
			// Must to be the same length
			DayAheadContentResponse dacr = ((DayAheadContentResponse) response
					.getContent());
			for (int i = 0; i < tUp.length && i < tDw.length; i++) {
				tUp[i] -= dacr.getDpUp()[i];
				tDw[i] -= dacr.getDpDown()[i];
				//TODO
			}
		}

	}

	private void updateTodayScheduleConsumption() {
		// TODO Auto-generated method stub
		Date nextMidnight = SimulationCalendarUtils
				.getDayAheadMidnight(calendar.getTime());
		totalTodayConsumption = totalDayAheadConsumption;
		totalDayAheadConsumption = TimeSequencePlan
				.initToZero(nextMidnight, 24);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void communicationTest() {
		for (int i = 0; i < 3; i++) {
			SimulationMessage sm = new SimulationMessage(this,
					prosumers.get(0), "AToP " + i, null);
			log.debug("A->P: " + sm.getHeader());
			this.sendMessage(sm);

			log.debug("A<-P:TAKE " + this.takeMessage().getHeader());
			// System.out.println(this.takeMsg());
		}
	}
}
