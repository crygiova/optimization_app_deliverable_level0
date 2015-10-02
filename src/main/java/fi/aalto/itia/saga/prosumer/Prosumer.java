/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeoutException;

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
 * 
 * Prosumer class
 * 
 * @author giovanc1
 *
 */
public class Prosumer extends SimulationElement {

	private final String STORAGE_TASK = "STORAGE";
	private final String DA_RESPONSE = "DAResponse";
	private final String QUEUE_OPT_PREFIX_NAME = "Prosumer_";

	private final static Logger log = Logger.getLogger(Prosumer.class);

	private int id;

	private SimulationElement aggregator;
	private StorageController storageController;
	private TimeSequencePlan todayConsumption;
	private TimeSequencePlan dayAheadConsumption;
	private TimeSequencePlan todaySchedule;
	private TimeSequencePlan dayAheadSchedule;
	private Scheduler scheduler;

	private BigDecimal storageStatusAtMidnight;

	/**
	 * @throws Exception
	 * 
	 */
	public Prosumer(int id, StorageController sc, String inputQueueName) {
		super(inputQueueName);
		this.id = id;
		this.storageController = sc;
		try {
			// broadcast messages
			dRChannel.queueBind(inputQueueName, EXCHANGE_NAME, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			scheduler = new Scheduler(QUEUE_OPT_PREFIX_NAME
					+ String.valueOf(this.id));
		} catch (Exception e) {
			log.debug("Not possible to create Scheduler due to Some MQ problem!");
		}
		initScheduleConsumption();
		this.startConsumingMq();
	}

	public Prosumer(int id, String inputQueueName) {
		this(id, new StorageController(), inputQueueName);
	}

	/**
	 * Allows to set the aggregator for this prosumer
	 * 
	 * @param aggregator
	 */
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
				// execute tasks of the current hour
				executeTasks();
				// Notify the end of the tasks
				this.notifyEndOfSimulationTasks();
				// log.debug("ProsEndOfSimTasks");
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
		log.debug("P_" + id + "_EndOfSimulation");
	}

	@Override
	public void scheduleTasks() {
		for (int i = 0; i < 24; i++) {
			// charging and discharging of the storage is scheduled once every
			// hour
			tasks.add(new TaskSchedule(STORAGE_TASK, i, 2));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fi.aalto.itia.saga.simulation.SimulationElement#executeTasks()
	 */
	@Override
	public void executeTasks() {
		// Schedule Tasks at midnight
		if (SimulationCalendarUtils
				.isMidnight(SimulationCalendar.getInstance())) {
			updateTodayScheduleConsumption();
			scheduleTasks();
		}
		// Execute Tasks
		// TODO this part can be improved by using classes for the scheduled
		// tasks rather than simple mathods
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
		// TODO delete// elaborate incoming messages// this is not necessary
		elaborateIncomingMessages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fi.aalto.itia.saga.simulation.SimulationElement#elaborateIncomingMessages
	 * ()
	 */
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
			BigDecimal[] dayAheadQ = dayAheadConsumption.getUnitToArray();

			log.debug("DayAhead Consumption " + dayAheadConsumption.toString());
			log.debug("StorageStatusAtMidnight " + storageStatusAtMidnight);

			DayAheadContentResponse optResult;
			DayAheadContentResponse response;
			// TODO is could be a REST Web service or so
			// optResult = Scheduler.optimizeMatlab(h, r,
			// msgContent.getSpotPrice(), storageStatusAtMidnight,
			// storageController.getStorageCapacityW(),
			// storageController.getStorageMaxChargingRateWh(), dayAheadQ,
			// msgContent.getW(), msgContent.gettUp(),
			// msgContent.gettDw(), msgContent.getTsize(), midnight);
			optResult = null;
			try {
				optResult = scheduler.optimizeMqMlab(h, r,
						msgContent.getSpotPrice(), storageStatusAtMidnight,
						storageController.getStorageCapacityW(),
						storageController.getStorageMaxChargingRateWh(),
						dayAheadQ, msgContent.getW(), msgContent.gettUp(),
						msgContent.gettDw(), msgContent.getTsize(), midnight);
			} catch (Exception e) {
				log.debug("Not possible to instantiate RabbitMQ");
				// TODO quit
				e.printStackTrace();
			}
			dayAheadSchedule = optResult.getP();
			// this is used only to clarify that the id of the response is
			// changed and assigned the Prosumer ID
			response = optResult;
			response.setId(this.id);
			SimulationMessage sm = new SimulationMessage(
					this.getInputQueueName(),
					this.aggregator.getInputQueueName(), DA_RESPONSE, response);
			this.sendMessage(sm);
			log.debug(optResult.toString());
		}
	}

	// TODO all the necessary controls for the task to be executed without
	// Exceptions
	// TODO u can also try to make a class task
	private void storageTask() {
		// log.debug("Storage Task ");
		BigDecimal chargeWh;
		BigDecimal dischargeWh;
		chargeWh = this.todaySchedule.getTimeEnergyTuple(
				this.todaySchedule.indexOf(calendar.getTime())).getUnit();
		dischargeWh = this.todayConsumption.getTimeEnergyTuple(
				this.todayConsumption.indexOf(calendar.getTime())).getUnit();
		try {
			this.storageController.chargeAndDischargeStorageWh(chargeWh,
					dischargeWh);
		} catch (Exception e) {
			log.debug("Charge " + (chargeWh.subtract(dischargeWh)));
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
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24,
				new BigDecimal(0));
		dayAheadConsumption = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
		dayAheadSchedule = TimeSequencePlan.initToValue(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24,
				new BigDecimal(0));
	}

	/**
	 * This utility method is used to predict the status of the battery at
	 * midnight
	 * 
	 * @param consumption
	 * @param schedule
	 * @return
	 */
	private BigDecimal predictStorageNextStatusAtMidnight(
			TimeSequencePlan consumption, TimeSequencePlan schedule) {
		BigDecimal currentHourStatus = storageController.getStorageStatusW();
		Date now = calendar.getTime();
		int index = consumption.indexOf(now);
		int size = consumption.size();
		BigDecimal unitConsumption[] = consumption.getUnitToArray();
		BigDecimal unitScheduled[] = schedule.getUnitToArray();

		for (int i = index; i < size; i++) {
			currentHourStatus = currentHourStatus.add(unitScheduled[i]);
			currentHourStatus = currentHourStatus.subtract(unitConsumption[i]);
		}
		if (currentHourStatus.compareTo(BigDecimal.ZERO) < 0)
			currentHourStatus = BigDecimal.ZERO;
		if (currentHourStatus
				.compareTo(storageController.getStorageCapacityW()) > 0)
			currentHourStatus = storageController.getStorageCapacityW();
		return MathUtility.roundBigDecimalTo(currentHourStatus, 6);

	}

}
