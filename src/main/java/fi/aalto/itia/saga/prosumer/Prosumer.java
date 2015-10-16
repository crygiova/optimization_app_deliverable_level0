/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
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
import fi.aalto.itia.saga.simulation.messages.IntraChangeConsumptionRequest;
import fi.aalto.itia.saga.simulation.messages.IntraContentResponse;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 4881619855765861962L;
	private final String STORAGE_TASK = "STORAGE";

	private final int H = 24;
	private final int R = 2;

	private final String QUEUE_OPT_PREFIX_NAME = "Prosumer_";

	private final static Logger log = Logger.getLogger(Prosumer.class);

	private int id;

	private SimulationElement aggregator;
	private StorageController storageController;
	private TimeSequencePlan todayConsumptionEstimated;
	private TimeSequencePlan todayConsumptionDeviated;
	private TimeSequencePlan dayAheadConsumptionEstimated;
	private TimeSequencePlan dayAheadConsumptionDeviated;
	private TimeSequencePlan todaySchedule;
	private TimeSequencePlan dayAheadSchedule;

	private IntraContentResponse intraEnergyConsumption;
	private int nextHour;

	private Scheduler scheduler;

	private BigDecimal storageStatusAtMidnight;

	private boolean storageCharged = false;

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
				storageCharged = false;
				// log.debug("storageCharged = false;");
				executeTasks();
				// Notify the end of the tasks
				this.notifyEndOfSimulationTasks();
				// log.debug("ProsEndOfSimTasks");
				while (!this.isReleaseToken() || !this.messageQueue.isEmpty()
						&& !this.isEndOfSimulation()) {
					elaborateIncomingMessages();
				}
			}
			// send the release signal
			this.releaseSimulationToken();
		}
		log.debug("P_" + id + "_EndOfSimulation");
	}

	@Override
	public void scheduleTasks() {
		for (int i = 0; i < 24; i++) {
			// the priority and the hour defines the order in the tasks queue
			// Storage charging
			tasks.add(new TaskSchedule(STORAGE_TASK, i, 5));
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
		// tasks rather than simple methods
		while (!this.tasks.isEmpty() && this.nextTaskAtThisHour()) {
			TaskSchedule currentTask = this.tasks.remove();
			switch (currentTask.getTaskName()) {
			case STORAGE_TASK:
				storageTask();
				storageCharged = true;
				// log.debug("storageCharged = true;");
				break;
			default:
				break;
			}
			// elaborate incoming messages
			elaborateIncomingMessages();
		}
		// delete// elaborate incoming messages// this is not necessary
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
		while ((inputMsg = this.pollMessageMs(10)) != null) {
			switch (inputMsg.getHeader()) {

			case SimulationMessage.DAY_AHEAD_HEADER_REQUEST:
				dayAheadResponse(inputMsg);
				break;
			case SimulationMessage.INTRA_START_HEADER_REQUEST:
				intraTask(inputMsg);
				break;
			case SimulationMessage.INTRA_HEADER_CHANGE_CONSUMPTION_REQUEST:
				intraChangeConsumption(inputMsg);
				break;
			default:
				break;
			}
			// log.debug("P<-A: " + inputMsg.getHeader() + "\n");

		}
	}

	private void dayAheadResponse(SimulationMessage inputMsg) {
		DayAheadContentRequest msgContent = (DayAheadContentRequest) inputMsg
				.getContent();
		Date midnight = SimulationCalendarUtils.getDayAheadMidnight(calendar
				.getTime());

		dayAheadConsumptionEstimated = ConsumptionEstimator
				.getConsumption(midnight);
		dayAheadConsumptionDeviated = ConsumptionEstimator
				.getConsumptionDeviated(dayAheadConsumptionEstimated);
		BigDecimal[] dayAheadQ = dayAheadConsumptionEstimated.getUnitToArray();

		log.debug("DayAhead Consumption "
				+ dayAheadConsumptionEstimated.toString());
		log.debug("StorageStatusAtMidnight " + storageStatusAtMidnight);

		DayAheadContentResponse optResult = null;
		DayAheadContentResponse response;

		try {
			optResult = scheduler.optimizeDAMqMlab(H, R,
					msgContent.getSpotPrice(), storageStatusAtMidnight,
					storageController.getStorageCapacityW(),
					storageController.getStorageMaxChargingRateWh(), dayAheadQ,
					msgContent.getW(), msgContent.gettUp(),
					msgContent.gettDw(), msgContent.getTsize(), midnight);
		} catch (Exception e) {
			log.debug("Not possible to instantiate RabbitMQ");
			e.printStackTrace();
		}
		dayAheadSchedule = optResult.getP();
		// this is used only to clarify that the id of the response is
		// changed and assigned the Prosumer ID
		response = optResult;
		response.setId(this.id);
		SimulationMessage sm = new SimulationMessage(this.getInputQueueName(),
				this.aggregator.getInputQueueName(),
				SimulationMessage.DAY_AHEAD_HEADER_RESPONSE, response);
		this.sendMessage(sm);
		// log.debug(optResult.toString());
	}

	// TODO Use Real Consumption and real schedule after intraday
	// TODO u can also try to make a class task
	/**
	 * 
	 */
	private void storageTask() {
		// log.debug("Storage Task ");
		BigDecimal chargeWh;
		BigDecimal dischargeWh;
		chargeWh = this.todaySchedule.getTimeEnergyTuple(
				this.todaySchedule.indexOf(calendar.getTime())).getUnit();
		// Using the consumption deviated after using the IntraDay logic
		dischargeWh = this.todayConsumptionDeviated.getTimeEnergyTuple(
				this.todayConsumptionDeviated.indexOf(calendar.getTime()))
				.getUnit();
		log.debug("Storage Initial charge before C/D, S0: "
				+ this.storageController.getStorageStatusW());
		log.debug("ChargeW: " + chargeWh + " |DischrgeW:" + dischargeWh);
		try {
			this.storageController.chargeAndDischargeStorageWh(chargeWh,
					dischargeWh);
		} catch (Exception e) {
			log.debug("Charge " + (chargeWh.subtract(dischargeWh)));
			e.printStackTrace();
		}
	}

	/**
	 * @param inputMsg
	 */
	private void intraTask(SimulationMessage inputMsg) {
		// log.debug("Publish From Aggregator");
		// S0 is the starting charging
		// Check if it is 11 pm
		nextHour = calendar.get(Calendar.HOUR_OF_DAY);
		BigDecimal dQ;
		BigDecimal[] ps;
		BigDecimal[] q1;
		// used to predict the next hour status of the battery
		BigDecimal chargeDischargeBuffer;
		Date dayAheadMidnight;
		if (nextHour == 23) {// next Day
			chargeDischargeBuffer = this.todayConsumptionDeviated
					.getIndex(nextHour).getUnit()
					.subtract(this.todaySchedule.getIndex(nextHour).getUnit());
			nextHour = 0;
			dQ = dayAheadConsumptionDeviated
					.getIndex(nextHour)
					.getUnit()
					.subtract(
							dayAheadConsumptionEstimated.getIndex(nextHour)
									.getUnit());
			ps = dayAheadSchedule.getUnitToArray();
			q1 = dayAheadConsumptionEstimated.getUnitToArray();
			dayAheadMidnight = dayAheadConsumptionDeviated.getStart();
		} else {// Today hours
			chargeDischargeBuffer = this.todayConsumptionDeviated
					.getIndex(nextHour).getUnit()
					.subtract(this.todaySchedule.getIndex(nextHour).getUnit());
			nextHour++;
			dQ = todayConsumptionDeviated
					.getIndex(nextHour)
					.getUnit()
					.subtract(
							todayConsumptionEstimated.getIndex(nextHour)
									.getUnit());
			ps = todaySchedule.getUnitToArray();
			q1 = todayConsumptionEstimated.getUnitToArray();
			dayAheadMidnight = todayConsumptionDeviated.getStart();
		}

		intraEnergyConsumption = null;
		// estimating charging status at the beginning of the next hour
		BigDecimal s0 = storageCharged ? this.storageController
				.getStorageStatusW() : this.storageController
				.getStorageStatusW().add(chargeDischargeBuffer);
		// log.debug("storageChargedINTRAEXE = " + storageCharged);
		try {

			// TODO if possible ps should consider 48 hours ahead and not only
			// 24! send the dayAheadSchedule
			intraEnergyConsumption = scheduler.optimizeIntraMqMlab(H, R,
					nextHour, s0, storageController.getStorageCapacityW(),
					storageController.getStorageMaxChargingRateWh(), ps, q1,
					MathUtility.roundBigDecimalTo(dQ, 6), dayAheadMidnight,
					dayAheadSchedule.getUnitToArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Response message
		IntraContentResponse response = intraEnergyConsumption;
		response.setId(this.id);
		SimulationMessage sm = new SimulationMessage(this.getInputQueueName(),
				this.aggregator.getInputQueueName(),
				SimulationMessage.INTRA_START_HEADER_RESPONSE, response);
		this.sendMessage(sm);
		// log.debug(intraEnergyConsumption.toString());
		log.debug("Next Hour initial stateOfCharge: " + s0);
	}

	/**
	 * @param inputMsg
	 */
	private void intraChangeConsumption(SimulationMessage inputMsg) {
		IntraChangeConsumptionRequest iccr = (IntraChangeConsumptionRequest) inputMsg
				.getContent();
		int length = intraEnergyConsumption.getdPd().length;
		BigDecimal[] dp = new BigDecimal[length];
		BigDecimal[] dpBuffer;
		if (iccr.isUp()) {
			dpBuffer = intraEnergyConsumption.getdPu().clone();
		} else {
			dpBuffer = intraEnergyConsumption.getdPd().clone();
		}
		for (int i = 0; i < length; i++) {
			// updating the dp
			dpBuffer[i] = MathUtility.roundBigDecimalTo(
					dpBuffer[i].multiply(iccr.getRPercent()), 6);
		}
		// TODO it depends on the next hour value if it is 0 need to take the
		// day ahead values
		if (nextHour == 0) {
			// dayAhead
			updateScheduleIntra(dayAheadSchedule, length, dpBuffer);
		} else {
			// CurrentDay
			updateScheduleIntra(todaySchedule, length, dpBuffer);
		}
		log.debug("Intra Consumption Change R: " + iccr.getRPercent() + " Up: "
				+ iccr.isUp());

	}

	/**
	 * @param tsp
	 * @param length
	 * @param dpBuffer
	 */
	private void updateScheduleIntra(TimeSequencePlan tsp, int length,
			BigDecimal[] dpBuffer) {
		int count = 0;
		for (int i = nextHour; i < H && count < length; i++, count++) {
			tsp.updateTimeEnergyTuple(i, todaySchedule.getIndex(i).getUnit()
					.add(dpBuffer[count]));
		}
	}

	private void updateTodayScheduleConsumption() {
		todayConsumptionEstimated = dayAheadConsumptionEstimated;
		todayConsumptionDeviated = dayAheadConsumptionDeviated;
		todaySchedule = dayAheadSchedule;
		storageStatusAtMidnight = predictStorageNextStatusAtMidnight(
				todayConsumptionEstimated, todaySchedule);
	}

	private void initScheduleConsumption() {
		todayConsumptionEstimated = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
		todaySchedule = TimeSequencePlan.initToValue(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24,
				new BigDecimal(0));
		dayAheadConsumptionEstimated = TimeSequencePlan.initToZero(
				SimulationCalendarUtils.getMidnight(calendar.getTime()), 24);
		dayAheadConsumptionDeviated = TimeSequencePlan.initToZero(
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
