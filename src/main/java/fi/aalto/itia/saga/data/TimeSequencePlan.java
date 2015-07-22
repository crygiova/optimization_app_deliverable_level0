package fi.aalto.itia.saga.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;

/**
 * @author giovanc1
 *
 */
public class TimeSequencePlan {

	// TODO Make ordered by Date/ Double Value
	private Date start;
	private List<TimeUnitTuple<Date, Double>> timEnergy = new ArrayList<TimeUnitTuple<Date, Double>>();

	public TimeSequencePlan(Date start) {
		this.start = start;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void addTimeEnergyTuple(Date date, Double unit) {
		this.addTimeEnergyTuple(new TimeUnitTuple<Date, Double>(date, unit));
	}

	public void addTimeEnergyTuple(TimeUnitTuple<Date, Double> tet) {
		// log.debug("Adding hourly plan, t:" + tet.getDate() + " e:"
		// + tet.getWatts());
		this.timEnergy.add(tet);
	}

	public boolean updateTimeEnergyTuple(Date date, Double unit) {
		int index;
		index = indexOf(date);
		if (index == -1)
			return false;
		else {
			this.timEnergy.set(index, new TimeUnitTuple<Date, Double>(date,
					unit));
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
					&& local.get(Calendar.YEAR) == gcTime
							.get(Calendar.YEAR))
				return index;
		}
		return -1;
	}

	public TimeUnitTuple<Date, Double> getTimeEnergyTuple(int index) {

		if (index < 0 || index >= timEnergy.size())
			return null;
		return timEnergy.get(index);
	}

	public List<TimeUnitTuple<Date, Double>> getTimEnergy() {
		return timEnergy;
	}

	public void setTimEnergy(List<TimeUnitTuple<Date, Double>> timEnergy) {
		// TODOthis.timEnergy = timEnergy;
	}

	public static TimeSequencePlan initToZero(Date start, int numHours) {
		TimeSequencePlan tsp = new TimeSequencePlan(start);
		for (int i = 0; i < numHours; i++) {
			tsp.addTimeEnergyTuple(start, 0d);
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return tsp;
	}
	
	public static TimeSequencePlan initToValue(Date start, int numHours, double value) {
		TimeSequencePlan tsp = new TimeSequencePlan(start);
		for (int i = 0; i < numHours; i++) {
			tsp.addTimeEnergyTuple(start, value);
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return tsp;
	}

	@Override
	public String toString() {
		String str = "";
		for (TimeUnitTuple<Date, Double> timeUnitTuple : timEnergy) {
			str += timeUnitTuple.toString() + "\n";
		}
		return "TimeSequencePlan [start=" + start + ", timEnergy=\n" + str
				+ "]";
	}

}
