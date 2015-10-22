package fi.aalto.itia.saga.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import fi.aalto.itia.saga.aggregator.Aggregator;
import fi.aalto.itia.saga.aggregator.util.OptParamEstimator;
import fi.aalto.itia.saga.prosumer.Prosumer;
import fi.aalto.itia.saga.simulation.SimulationElement;
import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DBExport {

	private MongoClient mongoClient;
	private MongoDatabase db;
	private Document simulationDoc;
	private Document prosumersDoc;
	private Document dASimDoc;
	private ArrayList<Document> dASimDocArray;
	private long idSimulation;
	final private DBConstants constant = new DBConstants();

	public DBExport() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(constant.DB_NAME);
		idSimulation = System.currentTimeMillis();
		prosumersDoc = new Document();
		simulationDoc = new Document();
		dASimDoc = new Document();
		dASimDocArray = new ArrayList<Document>();
	}

	public void buildSimulationDoc(Integer numberOfDaysSimulation,
			int numberOfProsumers, Date startCalendar, long executionTime) {
		simulationDoc
				.append(constant.ID, idSimulation)
				.append(constant.NUM_OF_SIM_DAY, numberOfDaysSimulation)
				.append(constant.NUM_PROS, numberOfProsumers)
				.append(constant.START_CAL, startCalendar)
				.append(constant.W,
						OptParamEstimator.getInstance().getW(startCalendar)
								.doubleValue())
				.append(constant.T_SIZE,
						OptParamEstimator.getInstance().getTSize(startCalendar)
								.doubleValue())
				.append(constant.EXE_TIME_MS, executionTime);
	}

	public void buildProsumersDoc(ArrayList<SimulationElement> prosumers) {
		Document pros[] = new Document[prosumers.size()];
		Document storageDoc;
		prosumersDoc.append(constant.SIMULATION_ID, idSimulation);
		prosumersDoc.append(constant.NUM_PROS, prosumers.size());
		for (int i = 0; i < prosumers.size(); i++) {
			Prosumer p = (Prosumer) prosumers.get(i);
			storageDoc = new Document()
					.append(constant.ID_STORAGE_PROS,
							p.getStorageController().getStorageId())
					.append(constant.MAX_CAPACITY_STORAGE,
							p.getStorageController().getStorageCapacityW()
									.doubleValue())
					.append(constant.MAX_CH_RATE_STORAGE,
							p.getStorageController()
									.getStorageMaxChargingRateWh()
									.doubleValue())
					.append(constant.MAX_DISCH_RATE_STORAGE,
							p.getStorageController()
									.getStorageMaxDischargingRateWh()
									.doubleValue());
			pros[i] = new Document().append(constant.ID_PROS, p.getId());
			pros[i].append(constant.STORAGE_PROS, storageDoc);
		}
		prosumersDoc.append(constant.PROS_COLL, asList(pros));

	}

	public void buildDADoc(Aggregator aggregator,
			ArrayList<SimulationElement> prosumers, int dayNumber, Date dayDate) {
		Document todayDa = new Document();
		todayDa.append(constant.DAY_NUM, dayNumber);
		todayDa.append(constant.DAY_DATE, dayDate);
		todayDa.append(constant.DA_AGG, buildDAAggDoc(aggregator));
		todayDa.append(constant.DA_PROS, buildDAProsDoc(prosumers));
		dASimDocArray.add(todayDa);
	}

	private Document buildDAAggDoc(Aggregator aggregator) {
		Document agg = new Document();
		// TODO complete the function!
		// Total Flex offered by Pros
		agg.append(constant.AGG_TOT_P_DW, transBigToDouble(aggregator.gettDw()));
		agg.append(constant.AGG_TOT_P_UP, transBigToDouble(aggregator.gettUp()));
		// Total Estimated Consumption
		agg.append(constant.AGG_TOT_Q,
				transBigToDouble(aggregator.getTotalDayAheadConsumption()));
		// SpotPrice
		agg.append(constant.SPOT_PRICE,
				transBigToDouble(aggregator.getDayAheadSpotPrice()));
		// Total initial flex
		agg.append(constant.TOT_FLEX_P_UP,
				transBigToDouble(aggregator.gettDwTotal()));
		agg.append(constant.TOT_FLEX_P_DW,
				transBigToDouble(aggregator.gettUpTotal()));
		return agg;
	}

	private Document buildDAProsDoc(ArrayList<SimulationElement> prosumers) {
		Document pros[] = new Document[prosumers.size()];
		Document finalPDoc = new Document();
		// TODO
		for (int i = 0; i < prosumers.size(); i++) {
			pros[i] = new Document();
			Prosumer p = (Prosumer) prosumers.get(i);
			pros[i].append(constant.ID_PROS, p.getId());
			pros[i].append(constant.DA_Q_E, transBigToDouble(p
					.getDayAheadConsumptionEstimated().getUnitToArray()));
			pros[i].append(constant.DA_Q_P, transBigToDouble(p
					.getDayAheadConsumptionDeviated().getUnitToArray()));
			pros[i].append(constant.DA_SCHEDULE, transBigToDouble(p
					.getDayAheadSchedule().getUnitToArray()));
			pros[i].append(constant.P_UP, transBigToDouble(p.getDayAheadDpUp()));
			pros[i].append(constant.P_DW, transBigToDouble(p.getDayAheadDpDw()));
		}
		finalPDoc.append(constant.DA_PROS, asList(pros));
		return finalPDoc;
	}

	public void writeToDb() {
		dASimDoc.append(constant.SIMULATION_ID, idSimulation);
		dASimDoc.append(constant.DA_SIM_COLL, asList(dASimDocArray));
		// inserting to the db
		db.getCollection(constant.SIM_COLL).insertOne(simulationDoc);
		db.getCollection(constant.PROS_COLL).insertOne(prosumersDoc);
		db.getCollection(constant.DA_SIM_COLL).insertOne(dASimDoc);
	}

	public void queryAll() {
		FindIterable<Document> iterable = db.getCollection(constant.SIM_COLL)
				.find();
		iterable.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println(document);
			}
		});
		iterable = db.getCollection(constant.PROS_COLL).find();
		iterable.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println(document);
			}
		});
		iterable = db.getCollection(constant.DA_SIM_COLL).find();
		iterable.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println(document);
			}
		});
	}

	public void dropAll() {
		String s = "N";
		System.out.println("Are you sure you want to drop the db? Y or N");
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			s = bufferRead.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (s.compareToIgnoreCase("Y") == 0) {
			db.getCollection(constant.SIM_COLL).drop();
			db.getCollection(constant.PROS_COLL).drop();
			db.getCollection(constant.DA_SIM_COLL).drop();
		} else {
			System.out.println("Db not Dropped");
		}
	}

	// converting list BigD to List Double
	private List<Double> transBigToDouble(BigDecimal[] bd) {
		List<BigDecimal> lbd = asList(bd);
		List<Double> doubleList = Lists.transform(lbd,
				new Function<BigDecimal, Double>() {
					public Double apply(BigDecimal value) {
						return value.doubleValue();
					}
				});
		return doubleList;
	}
}
