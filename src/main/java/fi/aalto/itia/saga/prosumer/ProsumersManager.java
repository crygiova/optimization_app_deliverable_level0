/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author giovanc1
 *
 */
public class ProsumersManager implements Runnable {

	private List prosumers;
	
	/**
	 * 
	 */
	public ProsumersManager(int numberOfProsumers) {
		prosumers = new ArrayList<Prosumer>();
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
