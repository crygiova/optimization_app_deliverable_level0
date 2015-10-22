package fi.aalto.itia.saga.mongodb;

public class DBConstants {

	
	final String DB_NAME = "drdb";
	final String ID = "_id";
	final String SIMULATION_ID ="simId";
	
	//Simulation collection constante
	final String SIM_COLL = "Simulation";
	final String NUM_OF_SIM_DAY = "numberOfSimDays";
	final String NUM_PROS = "numberOfProsumers";
	final String START_CAL = "startCalendar";
	final String W = "w";
	final String T_SIZE = "tSize";
	final String EXE_TIME_MS = "exeTimeMs";

	//Prosumers Collection
	final String PROS_COLL = "Prosumers";
	final String ID_PROS = "idProsumer";
	final String STORAGE_PROS = "storagePros";
	final String ID_STORAGE_PROS = "idStorage";
	final String MAX_CAPACITY_STORAGE = "maxCapacity";
	final String MAX_CH_RATE_STORAGE = "maxChargingRage";
	final String MAX_DISCH_RATE_STORAGE = "maxDisChargingRage";
	
	//DA_SIM
	final String DA_SIM_COLL = "DASim";
	final String DAY_NUM = "dayNumber";
	final String DAY_DATE = "dayDate";
	
	final String DA_AGG = "DAAggregator";
	final String SPOT_PRICE = "spotPrice";
	final String AGG_TOT_Q = "aggregatorTotalConsumption";
	final String AGG_TOT_P_UP = "aggregatorTotalFlexUp";
	final String AGG_TOT_P_DW = "aggregatorTotalFlexDown";
	final String TOT_FLEX_P_UP = "initialTotalFlexUp";
	final String TOT_FLEX_P_DW = "initialTotalFlexDown";
	
	final String DA_PROS = "DAProsumers";
	final String DA_Q_E = "DAConsumptionEstimated";
	final String DA_Q_P = "DAConsumptionDeviated";
	final String DA_SCHEDULE = "DASchedule";
	final String P_UP = "flexUp";
	final String P_DW = "flexDown";
	

	// public static final String ="";
	// public static final String ="";1445426768444
	// public static final String ="";
	/**
	 * 
	 */
	public DBConstants() {
		super();
	}

}
