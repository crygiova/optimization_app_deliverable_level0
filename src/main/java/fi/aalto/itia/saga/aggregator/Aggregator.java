package fi.aalto.itia.saga.aggregator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.aggregator.util.OptParamEstimator;
import fi.aalto.itia.saga.aggregator.util.SpotPriceEstimator;
import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentRequest;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentResponse;
import fi.aalto.itia.saga.simulation.messages.IntraChangeConsumptionRequest;
import fi.aalto.itia.saga.simulation.messages.IntraContentResponse;
import fi.aalto.itia.saga.simulation.messages.SimulationMessage;

/**
 * Class which represents an Aggregator
 * 
 * @author giovanc1
 *
 */
public class Aggregator extends SimulationElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4563607656399809462L;
	private final String DAY_AHEAD_TASK = "DAYAHEAD";
	private final static String INPUT_QUEUE_NAME = "Aggregator";
	private final String INTRA_TASK = "INTRA";

	ArrayList<SimulationElement> prosumers;

	private TimeSequencePlan totalDayAheadConsumption;
	private TimeSequencePlan totalTodayConsumption;
	// use for not scheduling the day ahead in the first day!
	private boolean firstDay = true;
	private boolean finishedIntraTask = true;

	private final Logger log = Logger.getLogger(Aggregator.class);

	public Aggregator() {
		super(INPUT_QUEUE_NAME);
		totalDayAheadConsumption = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
		this.startConsumingMq();
	}

	public void setProsumers(ArrayList<SimulationElement> prosumers) {
		this.prosumers = prosumers;
	}

	@Override
	public void run() {
		while (!this.isEndOfSimulation()) {
			// take the token and start working
			this.takeSimulationToken();
			if (!this.isEndOfSimulation()) {
				// Do the all the operations needed at that time
				executeTasks();
				// Notify the Simulator that the operations are finished
				this.notifyEndOfSimulationTasks();
				// log.debug("AggEndOfSimTasks");
				while (!this.isReleaseToken() || !this.messageQueue.isEmpty()
						&& !this.isEndOfSimulation()) {
					SimulationMessage str = this.pollMessageMs(10);
					if (str != null) {
						log.debug("A<-P: " + str.getHeader());
						// this.sendMsg(str + " Risposta dopo CountDown Run0");
					}
				}
			}
			// use release to putSq and reset release to false
			this.releaseSimulationToken();
		}
		log.debug("Agg_EndOfSimulation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fi.aalto.itia.saga.simulation.SimulationElement#scheduleTasks()
	 */
	@Override
	public void scheduleTasks() {
		this.tasks.add(new TaskSchedule(DAY_AHEAD_TASK, 12, 1));
		if (firstDay) {
			firstDay = false;
		} else {
			for (int i = 0; i < 24; i++) {
				// the priority and the hour defines the order in the tasks
				// queue
				// intra day task
				tasks.add(new TaskSchedule(INTRA_TASK, i, 3));
			}
		}
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
			// log.debug(SimulationCalendar.getInstance().getTime() + " Task "+
			// currentTask.getTaskName());
			switch (currentTask.getTaskName()) {
			case DAY_AHEAD_TASK:
				dayAheadTask();
				break;
			case INTRA_TASK:
				intraDayTask();
				break;
			default:
				break;
			}
		}
	}

	private void intraDayTask() {
		finishedIntraTask = false;
		startIntraTask();
		secondPhaseIntraTrak();
	}

	private void secondPhaseIntraTrak() {
		int count = 0;
		SimulationMessage inputMsg;
		BigDecimal sumConsumptions = BigDecimal.ZERO;
		BigDecimal sumDPu = BigDecimal.ZERO;
		BigDecimal sumDPd = BigDecimal.ZERO;
		int index = 0;
		while (!finishedIntraTask) {
			// TODO second part receive the messages and make all the logic!
			// Then send the orders to the others!
			// TODO condition to exit
			// TODO get intra messages
			inputMsg = this.waitForMessage();
			if (inputMsg.getHeader().compareTo(
					SimulationMessage.INTRA_START_HEADER_RESPONSE) == 0) {
				count++;
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				index = hour != 23 ? hour + 1 : 0;
				IntraContentResponse content = (IntraContentResponse) inputMsg
						.getContent();
				// MATLAB logic in here
				// sum of the real consumption for the next hour for the current
				// imbalance
				sumConsumptions = sumConsumptions.add(content.getP()
						.getIndex(index).getUnit());
				sumDPd = sumDPd.add(content.getdPd()[0]);
				sumDPu = sumDPu.add(content.getdPu()[0]);
				if (count >= prosumers.size())
					finishedIntraTask = true;
			} else {
				try {
					throw new Exception(
							"Error in The message sequence in IntraDay");

				} catch (Exception e) {
					e.printStackTrace();
					finishedIntraTask = true;
				}
			}
		}
		BigDecimal dE;// imbalance
		if (index == 0) {
			dE = totalDayAheadConsumption.getIndex(index).getUnit()
					.subtract(sumConsumptions);
		} else {
			dE = totalTodayConsumption.getIndex(index).getUnit()
					.subtract(sumConsumptions);
		}
		log.debug("Imbalance dE: " + dE + " >> sumConsumption " + sumConsumptions
				+ " >> sumDPu " + sumDPu + " >> sumDPu " + sumDPd);
		BigDecimal r = BigDecimal.ZERO;
		boolean isUp = dE.compareTo(BigDecimal.ZERO) < 0;
		if (isUp) {
			// positive imbalance
			if (sumDPu.compareTo(BigDecimal.ZERO) != 0)
				r = BigDecimal.valueOf(dE.doubleValue() / sumDPu.doubleValue());
			// r = dE.divide(sumDPu);
		} else {
			// negative imbalance
			if (sumDPd.compareTo(BigDecimal.ZERO) != 0)
				r = BigDecimal.valueOf(dE.doubleValue() / sumDPd.doubleValue());
			// r = dE.divide(sumDPd);
		}
		//TODO delete 0.9
		r = BigDecimal.valueOf(Math.min(Math.max(r.doubleValue()*0.9, 0), 1));
		
		IntraChangeConsumptionRequest content = new IntraChangeConsumptionRequest(
				isUp, r);// need to send r and up or down! Make an object
		SimulationMessage changeConsumptionRequest = new SimulationMessage(
				this.getInputQueueName(), "",
				SimulationMessage.INTRA_HEADER_CHANGE_CONSUMPTION_REQUEST,
				content);
		this.publishMessage(changeConsumptionRequest);

	}

	private void startIntraTask() {
		SimulationMessage sm = new SimulationMessage(this.getInputQueueName(),
				"", SimulationMessage.INTRA_START_HEADER_REQUEST, null);
		publishMessage(sm);
	}

	@Override
	public void elaborateIncomingMessages() {
		// TODO Auto-generated method stub

	}

	private void dayAheadTask() {
		// Creating the header
		String header = SimulationMessage.DAY_AHEAD_HEADER_REQUEST;
		// creating the content of the message
		Date midnight = SimulationCalendarUtils.getDayAheadMidnight(calendar
				.getTime());
		BigDecimal[] spotPrice = SpotPriceEstimator.getInstance()
				.getSpotPriceDouble(midnight);
		BigDecimal w = OptParamEstimator.getInstance().getW(midnight);
		BigDecimal tSize = OptParamEstimator.getInstance().getTSize(midnight);

		// won't change during the process
		final BigDecimal[] tUpTotal = OptParamEstimator.getInstance().getTUp(
				midnight);
		final BigDecimal[] tDwTotal = OptParamEstimator.getInstance().getTDw(
				midnight);
		BigDecimal[] tUp = tUpTotal.clone();
		BigDecimal[] tDw = tDwTotal.clone();

		for (SimulationElement prosumer : prosumers) {
			// Create content of the message
			Serializable content = new DayAheadContentRequest(spotPrice, tUp,
					tDw, tSize, w, midnight);
			SimulationMessage sm = new SimulationMessage(
					this.getInputQueueName(), prosumer.getInputQueueName(),
					header, content);
			this.sendMessage(sm);
			// Wait for the response of the Prosumer means that at the moment is
			// synchronous communication
			SimulationMessage response = this.waitForMessage();
			// log.debug(" Responded successful!! " + response.getHeader() +
			// "\n");
			// Must to be the same length
			DayAheadContentResponse dacr = ((DayAheadContentResponse) response
					.getContent());
			for (int i = 0; i < tUp.length && i < tDw.length; i++) {
				//Update flexibility
				tUp[i] = tUp[i].subtract(dacr.getDpUp()[i]);
				tDw[i] = tDw[i].subtract(dacr.getDpDown()[i]);
				// TODO Update Total Day Ahead Consumption
				totalDayAheadConsumption.addUnitToIndex(i, dacr.getP()
						.getIndex(i));
			}
			log.debug("Total Scheduled Consumption "
					+ totalDayAheadConsumption.toString());
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

	// TODO Broadcast message send!

	@SuppressWarnings("unused")
	@Deprecated
	private void communicationTest() {
		// for (int i = 0; i < 3; i++) {
		// SimulationMessage sm = new SimulationMessage(this,
		// prosumers.get(0), "AToP " + i, null);
		// log.debug("A->P: " + sm.getHeader());
		// this.sendMessage(sm);
		//
		// log.debug("A<-P:TAKE " + this.waitForMessage().getHeader());
		// // System.out.println(this.takeMsg());
		// }
	}
}
