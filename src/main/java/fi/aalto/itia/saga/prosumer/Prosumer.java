/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.prosumer.util.ConsumptionEstimator;
import fi.aalto.itia.saga.prosumer.util.OptimizedScheduler;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.simulation.SimulationMessage;
import fi.aalto.itia.saga.storage.StorageController;

/**
 * @author giovanc1
 *
 */
public class Prosumer extends SimulationElement {

	private final String STORAGE_TASK = "STORAGE";

	private final static Logger log = Logger.getLogger(Prosumer.class);

	private int id;

	private SimulationElement aggregator;
	private StorageController storageController;
	private TimeSequencePlan todayConsumption;
	private TimeSequencePlan dayAheadConsumption;
	private TimeSequencePlan todaySchedule;
	private TimeSequencePlan dayAheadSchedule;

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
				// TODO run tasks
				executeTasks();
				// Notify the end of the tasks
				this.notifyEndOfSimulationTasks();
				log.debug("ProsEndOfSimTasks");
				// TODO if there are messages use those till the simulator does
				// not
				// TODO DELETE Communication Test
				while (!this.isReleaseToken() || !this.messageQueue.isEmpty()) {
					elaborateIncomingMessages();
				}
				// send the release signal
				// TODO wait for signal of the simulator to putSQ
				// TODO use release for putSq
			}
			this.releaseSimulationToken();
		}
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
			scheduleTasks();
			updateTodayScheduleConsumption();
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
		SimulationMessage str;
		while ((str = this.pollMessageMs(10)) != null) {

			log.debug("P<-A: " + str.getHeader());
			// TODO based on the type of message it should call one method to
			// handle it.
			dayAheadConsumption = ConsumptionEstimator
					.getConsumption(SimulationCalendarUtils
							.getDayAheadMidnight(calendar.getTime()));
			log.debug("DayAhead Consumption " + dayAheadConsumption.toString());
			OptimizedScheduler.jOptimizeTest(dayAheadConsumption, storageController);
		}
	}

	// TODO all the necessary controls for the task to be executed without
	// Exception
	private void storageTask() {
		log.debug("Storage Task ");
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

}
