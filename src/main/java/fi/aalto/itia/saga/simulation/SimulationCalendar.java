package fi.aalto.itia.saga.simulation;

import java.util.GregorianCalendar;

public class SimulationCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static SimulationCalendar myCalendar = new SimulationCalendar();

	/**
	 * 
	 */
	private SimulationCalendar() {
		super();
	}

	public static synchronized SimulationCalendar getInstance() {
		if (myCalendar == null) {
			System.out.println("RiCreation New Singleton SimulationCalendar");
			// TODO possible to erase this since it will never happen
			return new SimulationCalendar();
		} else
			return myCalendar;

	}

	/**
	 * @param simulationHourTimeMs
	 */
	public synchronized void initSimulationCalendar() {
		this.set(HOUR_OF_DAY, 0);
		this.set(MINUTE, 0);
		this.set(SECOND, 0);
		this.set(MILLISECOND, 0);
		System.out.println("SimulationCalendar started " + this.getTime());
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
