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
public class ProsumersManagerOld implements Runnable {

	@SuppressWarnings({ "unused", "rawtypes" })
	private List prosumers;
	
	/**
	 * 
	 */
	public ProsumersManagerOld(int numberOfProsumers) {
		prosumers = new ArrayList<Prosumer>();
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
