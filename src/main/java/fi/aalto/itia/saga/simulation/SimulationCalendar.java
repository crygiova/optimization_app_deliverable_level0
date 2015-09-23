package fi.aalto.itia.saga.simulation;

import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

/**
 * @author giovanc1 Calendar used in the simulation
 */
public class SimulationCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger
			.getLogger(SimulationCalendar.class);

	private static SimulationCalendar myCalendar = new SimulationCalendar();

	/**
	 * 
	 */
	private SimulationCalendar() {
		super();
	}

	/**
	 * Singleton
	 * 
	 * @return SimulationCalendar object
	 */
	public static synchronized SimulationCalendar getInstance() {
		if (myCalendar == null) {
			log.debug("RiCreation New Singleton SimulationCalendar");
			// TODO possible to erase this since it will never happen
			return new SimulationCalendar();
		} else
			return myCalendar;
	}


	/**
	 * Initiate the Simulator calendar with the required month and day of the year
	 * @param month
	 * @param day
	 */
	public synchronized void initSimulationCalendar(int month, int day) {
		this.set(MONTH, month);
		this.set(DAY_OF_MONTH, day);
		this.set(HOUR_OF_DAY, 0);
		this.set(MINUTE, 0);
		this.set(SECOND, 0);
		log.debug("SimulationCalendar started " + this.getTime());
	}


	/**
	 * Init the calendar with default values
	 */
	public synchronized void initSimulationCalendar() {
		this.set(HOUR_OF_DAY, 0);
		this.set(MINUTE, 0);
		this.set(SECOND, 0);
		this.set(MILLISECOND, 0);
		log.debug("SimulationCalendar started " + this.getTime());
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
