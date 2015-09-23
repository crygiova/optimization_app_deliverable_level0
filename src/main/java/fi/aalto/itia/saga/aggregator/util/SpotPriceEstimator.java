package fi.aalto.itia.saga.aggregator.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.util.MathUtility;
import jxl.*;
import jxl.read.biff.BiffException;

/**
 * This class provides an estimation service for the SPOT Prices based on the
 * yearly spot prices defined in the xls file specified in the variable
 * FILE_NAME_PROPERTIES.
 * 
 * @author giovanc1
 * */
public class SpotPriceEstimator {
	private static final String FILE_NAME_PROPERTIES = "spot-2013.xls";
	private static final int DATE_COL = 0;
	private static final int HOUR_COL = 1;
	private static final int FI_COL = 7;
	private static final int INITIAL_ROW = 3;

	private static Workbook workbook;
	private static TimeSequencePlan yearlyPlan;
	private static GregorianCalendar utilityCalendar = new GregorianCalendar();
	/**
	 * Singleton instance of the SpotPriceEstimator
	 */
	private static SpotPriceEstimator instance = new SpotPriceEstimator();

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

	/**
	 * singleton implementation for the SpotPrice class
	 * 
	 * @return Singleton instance of the SpotPriceEstimator
	 */
	public static SpotPriceEstimator getInstance() {
		if (instance == null)
			instance = new SpotPriceEstimator();
		return instance;
	}

	private SpotPriceEstimator() {
	}

	/**
	 * This method initialize the SpotPriceEstimator instance by reading the
	 * Finnish prices from the excel file resource.
	 * 
	 */
	public void init() {
		boolean finished = false;
		boolean first = true;

		Sheet sheet = workbook.getSheet(0);
		Cell dateCell;
		Cell hourCell;
		Cell priceCell;

		for (int row = INITIAL_ROW; !finished && row < sheet.getRows(); row++) {
			dateCell = sheet.getCell(DATE_COL, row);// DATE
			hourCell = sheet.getCell(HOUR_COL, row);// Hours
			priceCell = sheet.getCell(FI_COL, row);// prices in
			if (dateCell.getType() == CellType.DATE
					&& hourCell.getType() == CellType.LABEL
					&& priceCell.getType() == CellType.NUMBER) {
				DateCell dc = (DateCell) dateCell;
				NumberCell n = (NumberCell) priceCell;
				utilityCalendar.setTime(dc.getDate());
				utilityCalendar.set(Calendar.HOUR_OF_DAY, Integer
						.parseInt(hourCell.getContents().substring(0, 2)));
				utilityCalendar.set(Calendar.YEAR,
						Calendar.getInstance().get(Calendar.YEAR));
				if (first) {
					yearlyPlan = new TimeSequencePlan(utilityCalendar.getTime());
					first = false;
				}
				BigDecimal priceKWh = MathUtility.convertMWhtoKWh(n.getValue());
				priceKWh = MathUtility.roundDoubleTo(priceKWh.doubleValue(), 6); // Round;
				yearlyPlan.addTimeEnergyTuple(utilityCalendar.getTime(),
						priceKWh);

			} else {
				finished = true;
			}
		}
	}

	/**
	 * This method can be seen as a service which estimates and return the SPOT
	 * Prices for a required and given day of the year
	 * 
	 * @param dayRequested
	 *            The adte of the current year to generate the SPOTPrices which
	 *            are read from the xls file.
	 * @return TimeSequencePlan with the SPOT Prices for the requested day of
	 *         the year
	 */
	public TimeSequencePlan getSpotPrice(Date dayRequested) {
		TimeSequencePlan ep;
		GregorianCalendar gc0 = new GregorianCalendar();
		gc0.setTime(dayRequested);
		GregorianCalendar gc = new GregorianCalendar(gc0.get(Calendar.YEAR),
				gc0.get(Calendar.MONTH), gc0.get(Calendar.DAY_OF_MONTH));
		dayRequested = gc.getTime();
		if (yearlyPlan == null)
			init();
		utilityCalendar.setTime(dayRequested);
		ep = new TimeSequencePlan(dayRequested);
		int index = yearlyPlan.indexOf(utilityCalendar.getTime());
		if (index != -1) {
			for (int i = index; i < index + 24; i++) {
				ep.addTimeEnergyTuple(yearlyPlan.getTimeEnergyTuple(i));
			}
			return ep;
		}
		return null;
	}

	public BigDecimal[] getSpotPriceDouble(Date dayRequested) {
		TimeSequencePlan ep;
		ep = getSpotPrice(dayRequested);
		return ep.getUnitToArray();
	}
}
