/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import java.util.Date;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.prosumer.storage.StorageController;
import fi.aalto.itia.saga.prosumer.util.ConsumptionEstimator;
import fi.aalto.itia.saga.prosumer.util.Scheduler;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentRequest;
import fi.aalto.itia.saga.simulation.messages.DayAheadContentResponse;
import fi.aalto.itia.saga.simulation.messages.SimulationMessage;
import fi.aalto.itia.saga.util.MathUtility;

/**
 * @author giovanc1
 *
 */
public class Prosumer extends SimulationElement {

	private final String STORAGE_TASK = "STORAGE";
	private final String DA_RESPONSE = "DAResponse";

	private final static Logger log = Logger.getLogger(Prosumer.class);

	private int id;

	private SimulationElement aggregator;
	private StorageController storageController;
	private TimeSequencePlan todayConsumption;
	private TimeSequencePlan dayAheadConsumption;
	private TimeSequencePlan todaySchedule;
	private TimeSequencePlan dayAheadSchedule;

	private double storageStatusAtMidnight;

	/**
	 * 
	 */
	public Prosumer(int id, StorageController sc) {
		this.id = id;
		this.storageController = sc;
		initScheduleConsumption();
	}

	public Prosumer(int id) {
		this(id, new StorageController());
	}

	public void setAggregator(SimulationElement aggregator) {
		this.aggregator = aggregator;
	}

	public SimulationElement getAggregator() {
		return aggregator;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public StorageController getStorageController() {
		return storageController;
	}

	public void setStorageController(StorageController storageController) {
		this.storageController = storageController;
	}

	@Override
	public void run() {
		while (!this.isEndOfSimulation()) {
			// start execution
			this.takeSimulationToken();
			if (!this.isEndOfSimulation()) {
				// exe tasks
				executeTasks();
				// Notify the end of the tasks
				this.notifyEndOfSimulationTasks();
				// log.debug("ProsEndOfSimTasks");
				// TODO if there are messages use those till the simulator does
				// not
				while (!this.isReleaseToken() || !this.messageQueue.isEmpty()
						&& !this.isEndOfSimulation()) {
					elaborateIncomingMessages();
				}
				// TODO wait for signal of the simulator to putSQ
				// TODO use release for putSq
			}
			// send the release signal
			this.releaseSimulationToken();
		}
		log.debug("EndOfSimulation P_" + id);
	}

	@Override
	public void scheduleTasks() {
		for (int i = 0; i < 24; i++) {
			tasks.add(new TaskSchedule(STORAGE_TASK, i, 2));
		}
	}

	@Override
	public void executeTasks() {
		// Schedule Tasks at midnight
		if (SimulationCalendarUtils
				.isMidnight(SimulationCalendar.getInstance())) {
			updateTodayScheduleConsumption();
			scheduleTasks();
		}
		// Execute Tasks
		while (!this.tasks.isEmpty() && this.nextTaskAtThisHour()) {
			TaskSchedule currentTask = this.tasks.remove();
			switch (currentTask.getTaskName()) {
			case STORAGE_TASK:
				storageTask();
				break;
			default:
				break;
			}
			// elaborate incoming messages
			elaborateIncomingMessages();
		}
		// TODO delete// elaborate incoming messages
		elaborateIncomingMessages();
	}

	@Override
	public void elaborateIncomingMessages() {
		SimulationMessage inputMsg;
		final int h = 24;
		final int r = 2;
		while ((inputMsg = this.pollMessageMs(10)) != null) {
			log.debug("P<-A: " + inputMsg.getHeader() + "\n"
					+ inputMsg.getContent().toString());
			DayAheadContentRequest msgContent = (DayAheadContentRequest) inputMsg
					.getContent();
			// TODO based on the type of message it should call one method to
			// handle it.

			Date midnight = SimulationCalendarUtils
					.getDayAheadMidnight(calendar.getTime());

			dayAheadConsumption = ConsumptionEstimator.getConsumption(midnight);
			double[] dayAheadQ = dayAheadConsumption.getUnitToArray();

			log.debug("DayAhead Consumption " + dayAheadConsumption.toString());
			log.debug("StorageStatusAtMidnight " + storageStatusAtMidnight);

			DayAheadContentResponse optResult;
			DayAheadContentResponse response;
			// TODO is could be a REST Web service or so
			optResult = Scheduler.optimizeMatlab(h, r,
					msgContent.getSpotPrice(), storageStatusAtMidnight,
					storageController.getStorageCapacityW(),
					storageController.getStorageMaxChargingRateWh(), dayAheadQ,
					msgContent.getW(), msgContent.gettUp(),
					msgContent.gettDw(), msgContent.getTsize(), midnight);
			dayAheadSchedule = optResult.getP();
			// this is used only to clarify that the id of the response is
			// changed and assigned the Prosumer ID
			response = optResult;
			response.setId(this.id);
			SimulationMessage sm = new SimulationMessage(this, this.aggregator,
					DA_RESPONSE, response);
			this.sendMessage(sm);
			log.debug(optResult.toString());
		}
	}

	// TODO all the necessary controls for the task to be executed without
	// Exceptions
	// TODO u can also try to make a class task
	private void storageTask() {
		// log.debug("Storage Task ");
		double chargeWh;
		double dischargeWh;
		chargeWh = this.todaySchedule.getTimeEnergyTuple(
				this.todaySchedule.indexOf(calendar.getTime())).getUnit();
		dischargeWh = this.todayConsumption.getTimeEnergyTuple(
				this.todayConsumption.indexOf(calendar.getTime())).getUnit();
		try {
			this.storageController.chargeAndDischargeStorageWh(chargeWh,
					dischargeWh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateTodayScheduleConsumption() {
		todayConsumption = dayAheadConsumption;
		todaySchedule = dayAheadSchedule;
		storageStatusAtMidnight = predictStorageNextStatusAtMidnight(
				todayConsumption, todaySchedule);
	}

	private void initScheduleConsumption() {
		todayConsumption = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
		todaySchedule = TimeSequencePlan.initToValue(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24, 0);
		dayAheadConsumption = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
		dayAheadSchedule = TimeSequencePlan.initToValue(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24, 0);
	}

	private double predictStorageNextStatusAtMidnight(
			TimeSequencePlan consumption, TimeSequencePlan schedule) {
		double currentHourStatus = storageController.getStorageStatusW();
		Date now = calendar.getTime();
		int index = consumption.indexOf(now);
		int size = consumption.size();
		double unitConsumption[] = consumption.getUnitToArray();
		double unitScheduled[] = schedule.getUnitToArray();

		for (int i = index; i < size; i++) {
			currentHourStatus += (unitScheduled[i] - unitConsumption[i]);
		}
		if (currentHourStatus < 0)
			currentHourStatus = 0;
		if (currentHourStatus > storageController.getStorageCapacityW())
			currentHourStatus = storageController.getStorageCapacityW();
		return MathUtility.roundDoubleTo(currentHourStatus, 6);

	}

}
