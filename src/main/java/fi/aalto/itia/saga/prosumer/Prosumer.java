/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import java.util.Date;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.aggregator.util.OptParamEstimator;
import fi.aalto.itia.saga.aggregator.util.SpotPriceEstimator;
import fi.aalto.itia.saga.data.TaskSchedule;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.prosumer.storage.StorageController;
import fi.aalto.itia.saga.prosumer.util.ConsumptionEstimator;
import fi.aalto.itia.saga.prosumer.util.OptimizationResult;
import fi.aalto.itia.saga.prosumer.util.Scheduler;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.simulation.SimulationMessage;
import fi.aalto.itia.saga.util.MathUtility;

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
				log.debug("ProsEndOfSimTasks");
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
		log.debug("EndOfSimulation");
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
		SimulationMessage str;
		final int h = 24;
		final int r = 2;
		while ((str = this.pollMessageMs(10)) != null) {
			log.debug("P<-A: " + str.getHeader());
			// TODO based on the type of message it should call one method to
			// handle it.
			Date midnight = SimulationCalendarUtils
					.getDayAheadMidnight(calendar.getTime());
			dayAheadConsumption = ConsumptionEstimator.getConsumption(midnight);
			SpotPriceEstimator spe = SpotPriceEstimator.getInstance();
			log.debug("DayAhead Consumption " + dayAheadConsumption.toString());
			log.debug("StorageStatusAtMidnight " + storageStatusAtMidnight);
			// TODO test
			double[] dayAheadQ = dayAheadConsumption.getUnitToArray();
			OptimizationResult opt;
			OptParamEstimator ope = OptParamEstimator.getInstance();
			// TODO many values are casted so make it work better
			// TODO correct get status W since U will need to calculate the
			// status at midnight
			// TODO is could be a REST Web service or so
			opt = Scheduler.optimizeMatlab(h, r,
					spe.getSpotPriceDouble(midnight), storageStatusAtMidnight,
					storageController.getStorageCapacityW(),
					storageController.getStorageMaxChargingRateWh(), dayAheadQ,
					ope.getW(midnight), ope.getT(midnight)[0],
					ope.getT(midnight)[1], ope.getTSize(midnight), midnight);
			dayAheadSchedule = opt.getP();
			log.debug(opt.toString());
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
		// TODO is it the current hour status of the next hour status already?
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
