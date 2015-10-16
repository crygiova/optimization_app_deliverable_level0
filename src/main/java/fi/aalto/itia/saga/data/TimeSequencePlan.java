package fi.aalto.itia.saga.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.util.MathUtility;

/**
 * This class represents a time sequence of a Date and a BigDecimal. BigDecimal
 * are used instead of Double !!
 * 
 * @author giovanc1
 *
 */
public class TimeSequencePlan implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1216068921552905410L;
	// TODO Make ordered by Date/ Double Value
	private Date start;
	private List<TimeUnitTuple<Date, BigDecimal>> timEnergy = new ArrayList<TimeUnitTuple<Date, BigDecimal>>();

	public TimeSequencePlan(Date start) {
		this.start = start;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void addTimeEnergyTuple(Date date, BigDecimal unit) {
		this.addTimeEnergyTuple(new TimeUnitTuple<Date, BigDecimal>(date, unit));
	}

	public void addTimeEnergyTuple(TimeUnitTuple<Date, BigDecimal> tet) {
		// log.debug("Adding hourly plan, t:" + tet.getDate() + " e:"
		// + tet.getWatts());
		this.timEnergy.add(tet);
	}

	public boolean updateTimeEnergyTuple(Date date, BigDecimal unit) {
		int index;
		index = indexOf(date);
		if (index == -1)
			return false;
		else {
			this.timEnergy.set(index, new TimeUnitTuple<Date, BigDecimal>(date,
					unit));
			return true;
		}
	}

	public boolean updateTimeEnergyTuple(int index, BigDecimal unit) {

		if (index == -1)
			return false;
		else {
			this.timEnergy.set(index, new TimeUnitTuple<Date, BigDecimal>(
					timEnergy.get(index).getDate(), unit));
			return true;
		}
	}

	public int indexOf(Date time) {
		int index = -1;
		GregorianCalendar gcTime = (GregorianCalendar) GregorianCalendar
				.getInstance();
		gcTime.setTime(time);
		GregorianCalendar local = (GregorianCalendar) GregorianCalendar
				.getInstance();

		// TODO you can optimize when time and change the compare to consider
		// only day month and hour
		while (++index < timEnergy.size()) {
			local.setTime(timEnergy.get(index).getDate());
			if (local.get(Calendar.MONTH) == gcTime.get(Calendar.MONTH)
					&& local.get(Calendar.HOUR_OF_DAY) == gcTime
							.get(Calendar.HOUR_OF_DAY)
					&& local.get(Calendar.DAY_OF_MONTH) == gcTime
							.get(Calendar.DAY_OF_MONTH)
					&& local.get(Calendar.YEAR) == gcTime.get(Calendar.YEAR))
				return index;
		}
		return -1;
	}

	public TimeUnitTuple<Date, BigDecimal> getIndex(int index) {
		if (index >= 0 && index < this.size())
			return timEnergy.get(index);
		else
			throw new ArrayIndexOutOfBoundsException(index);
	}

	public TimeUnitTuple<Date, BigDecimal> getTimeEnergyTuple(int index) {

		if (index < 0 || index >= timEnergy.size())
			return null;
		return timEnergy.get(index);
	}

	public List<TimeUnitTuple<Date, BigDecimal>> getTimEnergy() {
		return timEnergy;
	}

	public int size() {
		return timEnergy.size();
	}

	public boolean addUnitToIndex(int index,
			TimeUnitTuple<Date, BigDecimal> value) {

		if (index < this.timEnergy.size()) {
			timEnergy.set(
					index,
					new TimeUnitTuple<Date, BigDecimal>(timEnergy.get(index)
							.getDate(), timEnergy.get(index).getUnit()
							.add(value.getUnit())));
			return true;
		}
		return false;
	}

	// number of decimal values for the rounding of the mean
	public BigDecimal getMeanValue(int decimal) {
		Mean mean = new Mean();
		for (int i = 0; i < timEnergy.size(); i++)
			mean.increment(timEnergy.get(i).getUnit().doubleValue());
		return MathUtility.roundBigDecimalTo(
				BigDecimal.valueOf(mean.getResult()), decimal);
	}

	public static TimeSequencePlan initToZero(Date start, int numHours) {
		TimeSequencePlan tsp = new TimeSequencePlan(start);
		for (int i = 0; i < numHours; i++) {
			tsp.addTimeEnergyTuple(start, new BigDecimal(0d));
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return tsp;
	}

	public static TimeSequencePlan initToValue(Date start, int numHours,
			BigDecimal value) {
		TimeSequencePlan tsp = new TimeSequencePlan(start);
		for (int i = 0; i < numHours; i++) {
			tsp.addTimeEnergyTuple(start, value);
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return tsp;
	}

	public BigDecimal[] getUnitToArray() {
		BigDecimal[] d = new BigDecimal[timEnergy.size()];
		for (int i = 0; i < timEnergy.size(); i++) {
			d[i] = timEnergy.get(i).getUnit();
		}
		return d;
	}

	@Override
	public String toString() {
		String str = "";
		for (TimeUnitTuple<Date, BigDecimal> timeUnitTuple : timEnergy) {
			str += timeUnitTuple.toString() + "\n";
		}
		return "TimeSequencePlan [start=" + start + ", timEnergy=\n" + str
				+ "]";
	}

}
