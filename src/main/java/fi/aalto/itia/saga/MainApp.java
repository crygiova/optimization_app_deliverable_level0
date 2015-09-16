/**
 * 
 */
package fi.aalto.itia.saga;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.aggregator.Aggregator;
import fi.aalto.itia.saga.prosumer.Prosumer;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationElement;
import fi.aalto.itia.saga.util.Utility;

/**
 * @author giovanc1
 *
 */

public class MainApp {

	private static final String FILE_NAME_PROPERTIES = "mainApp.properties";
	private static final String MONTH_START = "monthStart";
	private static final String DAY_START = "dayStart";
	private static final String NUMBER_OF_DAYS_SIMULATION = "numberOfDays";

	/**
	 * Simulation Calendar
	 */
	public static SimulationCalendar cal;
	/**
	 * CountDownLatch for the Synchronization of the Simulation Elements
	 */
	public static CountDownLatch simulationCountDownLatch;
	/**
	 * ArrayList which will contain all the Simulation elements
	 */
	public static ArrayList<SimulationElement> simulationElements = new ArrayList<SimulationElement>();
	/**
	 * ArrayList of Threads Objects for each simulation element
	 */
	public static ArrayList<Thread> threads = new ArrayList<Thread>();

	/**
	 * boolean variable which is used to end the simulation
	 */
	private static boolean isEndOfSimulation = false;

	private static Properties properties;

	private static Integer startingDaySimulation;

	private static Integer startingMonthSimulation;

	/**
	 * Represents the number of days of simulation requested
	 */
	private static Integer numberOfDaysSimulation;
	/**
	 * Represents the number of days of simulation requested, but it will be
	 * used as counted to end the simulation
	 */
	private static Integer numberOfDaysSimulationCountDown;

	private static final Logger log = Logger.getLogger(MainApp.class);

	// Loading the properties configuration
	static {
		properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		startingMonthSimulation = Integer.parseInt(properties
				.getProperty(MONTH_START));
		startingDaySimulation = Integer.parseInt(properties
				.getProperty(DAY_START));
		numberOfDaysSimulation = Integer.parseInt(properties
				.getProperty(NUMBER_OF_DAYS_SIMULATION));
		numberOfDaysSimulationCountDown = numberOfDaysSimulation;
	}

	public static void main(String[] args) throws NoSuchFieldException,
			SecurityException, InterruptedException {
		cal = initSimulationCalendar();
		initSimulationEnvironment();
		updateSimulationCountDownLatch();
		startThreads();
		log.debug("Run");
		while (!isEndOfSimulation) {
			releaseTokens();
			// Threads are running, wait them to finish the simulation step
			simulationCountDownLatch.await();
			takeTokens();
			// Simulator Progress
			isEndOfSimulation = nextSimulationStep();
			if (isEndOfSimulation) {
				endOfSimulation();
			}
			log.debug("Updated Time: " + cal.getTime());
		}
		log.debug("EndOfSimulation");
	}

	/**
	 * nextSimulationStep adds one hour to the SimulationCalendar in order to
	 * proceed to the next hour of the Simulation and defines also when the
	 * simulation is finished by returning a boolean value
	 * 
	 * @return true if the simulation is ended due the fact that the number of
	 *         simulation days have been simulated, otherwise it returns false
	 *         if the simulation needs to continue
	 */
	private static boolean nextSimulationStep() {
		int day = cal.get(Calendar.DAY_OF_MONTH);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		int dayAfter = cal.get(Calendar.DAY_OF_MONTH);
		if (day != dayAfter) {
			if (--numberOfDaysSimulationCountDown == 0)
				return true;
		}
		return false;
	}

	/**
	 * Function which initialize the SimulationCalendar
	 * 
	 * @return SimulationCalendar
	 */
	public static SimulationCalendar initSimulationCalendar() {
		SimulationCalendar.getInstance().initSimulationCalendar(
				startingMonthSimulation, startingDaySimulation);
		return SimulationCalendar.getInstance();
	}

	/**
	 * Function which initialize the Simulation Environment in term of Data
	 * structures containing all the SimulationElement object used in the
	 * Simulation
	 */
	// TODO improve this first draft of this function
	private static void initSimulationEnvironment() {
		// add Server
		simulationElements.add(0, new Aggregator());
		// add One client
		ArrayList<SimulationElement> prosumers = new ArrayList<SimulationElement>();
		// Add as much as clients you want theoretically
		prosumers.add(0, new Prosumer(0));
		simulationElements.addAll(prosumers);
		// Adding clients to Server #in this case only ONE
		((Aggregator) simulationElements.get(0)).setProsumers(prosumers);
		// Adding server reference to Clients
		for (int i = 1; i < simulationElements.size(); i++) {
			((Prosumer) simulationElements.get(i))
					.setAggregator(simulationElements.get(0));
		}

	}

	/**
	 * This function releases all the simulation Tokens for each
	 * SimulationElement in the Simulation Environment
	 */
	public static synchronized void releaseTokens() {
		for (SimulationElement r : simulationElements) {
			r.releaseSimulationToken();
		}
	}

	/**
	 * This function takes all the simulation Tokens for each SimulationElement
	 * in the Simulation Environment and then updates the CountDownLatch of each
	 * SimulationElement
	 */
	public static synchronized void takeTokens() {
		for (SimulationElement r : simulationElements) {
			r.setReleaseToken(true);
		}
		for (SimulationElement r : simulationElements) {
			r.takeSimulationToken();
		}
		updateSimulationCountDownLatch();
	}

	/**
	 * This function update the CountDownLatch to all the SimulationElement
	 * object which will take part to the simulation
	 */
	public static synchronized void updateSimulationCountDownLatch() {
		simulationCountDownLatch = new CountDownLatch(simulationElements.size());
		for (SimulationElement r : simulationElements) {
			r.updateEndOfSimulationTasks(simulationCountDownLatch);
		}
	}

	/**
	 * Initialize the threads ArrayList and then start each Thread
	 */
	public static void startThreads() {
		for (SimulationElement r : simulationElements) {
			threads.add(new Thread(r));
		}
		for (Thread thread : threads) {
			thread.start();
		}
	}

	/**
	 * Procedure which will end the simulation of all the SimulationElements
	 */
	public static void endOfSimulation() {
		for (SimulationElement simulationElement : simulationElements) {
			simulationElement.setEndOfSimulation(true);
		}
		releaseTokens();
		takeTokens();
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// public static void main(String[] args) {
	// context = new ClassPathXmlApplicationContext(
	// "Beans.xml");
	//
	// HelloWorld obj = (HelloWorld) context.getBean("helloWorld");
	// obj.getMessage();
	//
	// }

}
