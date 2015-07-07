/**
 * 
 */
package fi.aalto.itia.saga.simulation;

import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

/**
 * @author giovanc1
 *
 */
// TODO need to set the calendar at the beginning of of the day and set 0 the
// seconds
public class SimulationCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger
			.getLogger(SimulationCalendar.class);

	private long simulationHourTimeMs;// in Milliseconds
	private long simulationMinTimeMs;

	private static SimulationCalendar myCalendar = new SimulationCalendar();
	private Timer timer = new Timer();

	private TimerTask myTask = new TimerTask() {
		@Override
		public void run() {
			myCalendar.add(MINUTE, 1);
			log.debug("Minute Added: " + myCalendar.getTime());
		}
	};

	/**
	 * 
	 */
	private SimulationCalendar() {
		super();
	}

	public static synchronized SimulationCalendar getInstance() {
		if (myCalendar == null) {
			log.debug("RiCreation New Singleton SimulationCalendar");
			return new SimulationCalendar();
		} else
			return myCalendar;

	}

	/**
	 * @param simulationHourTimeMs
	 */
	public synchronized void startSimulationCalendar(long simulationHourTimeMs) {
		this.simulationHourTimeMs = simulationHourTimeMs;
		this.simulationMinTimeMs = (long) this.simulationHourTimeMs / 60l;
		this.set(HOUR_OF_DAY, 0);
		this.set(MINUTE, 0);
		this.set(SECOND, 0);
		this.set(MILLISECOND, 0);
		log.debug("SimulationCalendar started " + this.getTime());
		// timer.schedule(myTask, 2000, 2000); in Ms
		timer.schedule(myTask, simulationMinTimeMs, simulationMinTimeMs);
	}

	/**
	 * 
	 */
	public void stopSimluationCalendar() {
		log.debug("SimulationCalendar stopped");
		myTask.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#add(int, int)
	 */
	@Override
	public void add(int field, int amount) {
		super.add(field, amount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#computeTime()
	 */
	@Override
	protected void computeTime() {
		super.computeTime();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#computeFields()
	 */
	@Override
	protected void computeFields() {
		super.computeFields();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#roll(int, boolean)
	 */
	@Override
	public void roll(int field, boolean up) {
		super.roll(field, up);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#getMinimum(int)
	 */
	@Override
	public int getMinimum(int field) {
		return super.getMinimum(field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#getMaximum(int)
	 */
	@Override
	public int getMaximum(int field) {
		return super.getMaximum(field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#getGreatestMinimum(int)
	 */
	@Override
	public int getGreatestMinimum(int field) {
		return super.getGreatestMinimum(field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Calendar#getLeastMaximum(int)
	 */
	@Override
	public int getLeastMaximum(int field) {
		return super.getLeastMaximum(field);
	}

}