package fi.aalto.itia.saga.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UtilityTest {

	public static Date getMidnight()
	{
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
				.getInstance();
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);
		return gc.getTime();
	}
	
	public static Date calculateNextHour(Date start, int add) {
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
				.getInstance();
		gc.setTime(start);
		gc.add(Calendar.HOUR_OF_DAY, add);
		return (Date) gc.getTime().clone();
	}

}
