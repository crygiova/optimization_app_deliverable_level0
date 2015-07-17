package fi.aalto.itia.saga.aggregator;

import java.util.ArrayList;

import fi.aalto.itia.saga.MainApp;
import fi.aalto.itia.saga.simulation.SimulationElement;

public class Aggregator extends SimulationElement {

	ArrayList<SimulationElement> clients;

	public Aggregator() {
		super();
	}

	public void setClients(ArrayList<SimulationElement> clients) {
		this.clients = clients;
	}

/*	@Override
	public void run() {
		while (true) {
			// take the token and start working
			this.takeSimulationToken();
			// TODO Do the all the operations needed at the calendar time
			// TODO check message queue and keep doing it till is not empty
			// Notify the Simulator that the operations are finished
			this.notifyEndOfSimulationTasks();
			// TODO check message queue and keep doing it till the main Thread
			// update release
			// TODO use release to releaseSimulationToken
			this.releaseSimulationToken();
		}
	}*/
	
	
	@Override
	public void run() {
		while (true) {
			// take the token and start working
			this.takeSimulationToken();
			// TODO Do the all the operations needed at that time
			System.out.println("Aggregator....");
			for (int i = 0; i < 3; i++) {
				String str = "AToP " + i;
				System.out.println("A->P: " + str);
				this.sendMessage(clients.get(0), str);
				// System.out.println(this.takeMsg());
				MainApp.sleep(1000);
			}
			// TODO check message queue and keep doing it till is not empty
			// TODO Notify the Simulator that the operations are finished
			this.notifyEndOfSimulationTasks();
			System.out.println("AggEndOfSimTasks");
			// TODO check message queue and keep doing it till the main Thread
			// update release
			while (!this.isReleaseToken() || !this.messageQueue.isEmpty()) {
				String str = this.pollMessageMs(10);
				if (str != null) {
					System.out.println("A<-P: " + str);
					// this.sendMsg(str + " Risposta dopo CountDown Run0");
				}
			}
			// TODO use release to putSq and reset release to false
			this.releaseSimulationToken();
		}
	}
}
