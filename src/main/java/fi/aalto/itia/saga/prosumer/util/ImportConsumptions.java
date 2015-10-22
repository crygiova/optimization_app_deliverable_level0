package fi.aalto.itia.saga.prosumer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import jxl.Cell;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ImportConsumptions {

	private static final String FILE_NAME_PROPERTIES = "cons_house.xls";

	private static final int NAME_COL = 0;
	private static final int DESC_COL = 1;
	private static final int FROM_COL = 2;
	private static final int TO_COL = 3;
	private static final int HOUR_COL = 5;
	private static final int TOT_H_COL = 24;
	private static final int INITIAL_ROW = 0;
	private static final String WEEK_DAY = "WD";
	private static final String WEEK_END = "WE";
	private static ArrayList<String> names = new ArrayList<String>();
	private static HashMap<String, Double[]> map = new HashMap<String, Double[]>();
	private static HashMap<String, String> mapDescr = new HashMap<String, String>();
	private static HashMap<String, Date> mapFrom = new HashMap<String, Date>();
	private static HashMap<String, Date> mapTo = new HashMap<String, Date>();

	private static Workbook workbook;

	/**
	 * Singleton instance of the SpotPriceEstimator
	 */
	private static GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
			.getInstance();

	static {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try (InputStream resourceStream = classLoader
				.getResourceAsStream(FILE_NAME_PROPERTIES)) {
			workbook = Workbook.getWorkbook(resourceStream);
		} catch (IOException e) {
			System.out.println("Excel file not Found: " + FILE_NAME_PROPERTIES);
		} catch (BiffException e) {
			e.printStackTrace();
		}
	};

	public ImportConsumptions() {
		init();
	}

	private void init() {

		Sheet sheet = workbook.getSheet(0);
		Cell nameCell;
		Cell hourCell;

		for (int row = INITIAL_ROW; row < 8; row++) {
			nameCell = sheet.getCell(NAME_COL, row);// Name
			String key = ((LabelCell) nameCell).getString();
			names.add(key);
			Double values[] = new Double[TOT_H_COL];
			for (int col = HOUR_COL, i = 0; col < TOT_H_COL + HOUR_COL; col++, i++) {
				hourCell = sheet.getCell(col, row);
				values[i] = ((NumberCell) hourCell).getValue();
			}
			mapFrom.put(key,
					((DateCell) sheet.getCell(FROM_COL, row)).getDate());
			mapTo.put(key, ((DateCell) sheet.getCell(TO_COL, row)).getDate());

			String descr = ((LabelCell) sheet.getCell(DESC_COL, row))
					.getString();

			mapDescr.put(key, descr);
			map.put(key, values);
		}
	}

	public String getPeriodName(Date d) {
		boolean finished = false;
		String period = null;

		int initPMonth, endPMonth;
		int initPDay, endPDay;
		int month;
		int day;
		gc.setTime(d);
		month = gc.get(Calendar.MONTH);
		day = gc.get(Calendar.DAY_OF_MONTH);
		int dow = gc.get(Calendar.DAY_OF_WEEK);
		boolean isWeekday = ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));
		for (int i = 0; i < names.size() && !finished; i++) {
			String key = names.get(i);
			gc.setTime(mapFrom.get(key));
			initPMonth = gc.get(Calendar.MONTH);
			initPDay = gc.get(Calendar.DAY_OF_MONTH);
			gc.setTime(mapTo.get(key));
			endPMonth = gc.get(Calendar.MONTH);
			endPDay = gc.get(Calendar.DAY_OF_MONTH);
			// normal
			if (endPMonth >= initPMonth) {
				if (month >= initPMonth && month <= endPMonth
						&& day >= initPDay && day <= endPDay) {
					// week day or week end
					if (isWeekday) {
						if (key.contains(WEEK_DAY)) {
							finished = true;
							period = key;
						}
					} else {
						if (key.contains(WEEK_END)) {
							finished = true;
							period = key;
						}
					}

				}
			} else {// new year
				// if (month >= initPMonth && day >= initPDay || day <=
				// endPMonth
				// && day <= endPDay) {
				if (isWeekday) {
					if (key.contains(WEEK_DAY)) {
						finished = true;
						period = key;
					}
				} else {
					if (key.contains(WEEK_END)) {
						finished = true;
						period = key;
					}
				}
				// }
			}
		}
		return period;
	}

	public Double[] getConsumption(String key) {
		System.out.println(key);
		return map.get(key);
	}

}
