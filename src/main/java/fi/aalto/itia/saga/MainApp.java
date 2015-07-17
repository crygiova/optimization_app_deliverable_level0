/**
 * 
 */
package fi.aalto.itia.saga;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import fi.aalto.itia.saga.aggregator.Aggregator;
import fi.aalto.itia.saga.prosumer.ProsumerManager;
import fi.aalto.itia.saga.simulation.SimulationCalendar;
import fi.aalto.itia.saga.simulation.SimulationElement;

/**
 * @author giovanc1
 *
 */
public class MainApp {



	public static SimulationCalendar cal;

	public static CountDownLatch simulationCountDownLatch;

	public static ArrayList<SimulationElement> simulationElements = new ArrayList<SimulationElement>();

	public static ArrayList<Thread> threads = new ArrayList<Thread>();

	public static void main(String[] args) throws NoSuchFieldException,
			SecurityException, InterruptedException {

		cal = initSimulationCalendar();

		initSimulationEnvironment();

		updateSimulationCountDownLatch();

		startThreads();

		System.out.println("Run");

		while (true) {
			System.out.println("Updated Time: " + cal.getTime());
			releaseTokens();
			// Threads are running, wait them to finish the simulation step
			simulationCountDownLatch.await();
			takeTokens();
			// Simulator Progress
			System.out.println("Sleepin Time: " + cal.getTime());
			MainApp.sleep(2000);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
	}

	private static void initSimulationEnvironment() {
		// add Server
		simulationElements.add(0, new Aggregator());
		// add One client
		ArrayList<SimulationElement> clients = new ArrayList<SimulationElement>();
		// Add as much as clients you want theoretically
		clients.add(0, new ProsumerManager());
		simulationElements.addAll(clients);
		// Adding clients to Server #in this case only ONE
		((Aggregator) simulationElements.get(0)).setClients(clients);
		// Adding server reference to Clients
		for (int i = 1; i < simulationElements.size(); i++) {
			((ProsumerManager) simulationElements.get(i)).setServer(simulationElements
					.get(0));
		}

	}

	public static SimulationCalendar initSimulationCalendar() {
		SimulationCalendar.getInstance().initSimulationCalendar();
		return SimulationCalendar.getInstance();
	}

	public static synchronized void releaseTokens() {
		for (SimulationElement r : simulationElements) {
			r.releaseSimulationToken();
		}
	}

	public static void takeTokens() {
		for (SimulationElement r : simulationElements) {
			r.setReleaseToken(true);
		}
		for (SimulationElement r : simulationElements) {
			r.takeSimulationToken();
		}
		updateSimulationCountDownLatch();
	}

	public static void updateSimulationCountDownLatch() {
		simulationCountDownLatch = new CountDownLatch(simulationElements.size());
		for (SimulationElement r : simulationElements) {
			r.updateEndOfSimulationTasks(simulationCountDownLatch);
		}
	}

	public static void startThreads() {
		for (SimulationElement r : simulationElements) {
			threads.add(new Thread(r));
		}
		for (Thread thread : threads) {
			thread.start();
		}
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		context = new ClassPathXmlApplicationContext(
//				"Beans.xml");
//
//		HelloWorld obj = (HelloWorld) context.getBean("helloWorld");
//		obj.getMessage();
//
//	}

}
