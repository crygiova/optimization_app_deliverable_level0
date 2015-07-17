package fi.aalto.itia.saga.prosumer;

import java.util.List;

import fi.aalto.itia.saga.simulation.SimulationElement;

public class ProsumerManager extends SimulationElement {

	SimulationElement server;
	List<Prosumer> p;
	
	public ProsumerManager() {
		super();
	}

	public void setServer(SimulationElement server) {
		this.server = server;
	}

	@Override
	public void run() {
		while (true) {
			// start execution
			this.takeSimulationToken();
			// TODO run tasks
			System.out.println("Prosumer....");
			// TODO if there are messages use those till the queue is not empty
			String str;
			// while (!this.msg.isEmpty()) {
			while ((str = this.pollMessageMs(10)) != null) {
				System.out.println("P<-A: " + str);
				str += " Reply1";
				System.out.println("P->A: " + str);
				this.sendMessage(this.server, str);
			}
			// Notify the end of the tasks
			this.notifyEndOfSimulationTasks();
			System.out.println("ProsEndOfSimTasks");
			// TODO if there are messages use those till the simulator does not
			while (!this.isReleaseToken() || !this.messageQueue.isEmpty()) {
				str = this.pollMessageMs(10);
				if (str != null) {
					System.out.println("P<-A: " + str);
					str += " Reply2";
					System.out.println("P->A: " + str);
					this.sendMessage(this.server, str);
				}
			}
			// send the release signal
			// TODO wait for signal of the simulator to putSQ
			// TODO use release for putSq

			this.releaseSimulationToken();
		}
	}
}
