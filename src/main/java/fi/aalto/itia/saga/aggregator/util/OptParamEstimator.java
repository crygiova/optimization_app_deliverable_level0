package fi.aalto.itia.saga.aggregator.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import fi.aalto.itia.saga.util.MathUtility;
import fi.aalto.itia.saga.util.Utility;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * This class is used as an estimator for Optimization Parameters
 * 
 * @author giovanc1
 *
 */
public class OptParamEstimator {

	private static final String FILE_NAME_PROPERTIES_T = "targetFlex.xls";
	private static final String FILE_NAME_PROPERTIES_W = "wParameter.properties";
	private static final String FILE_NAME_PROPERTIES_NUM_PROS = "mainApp.properties";
	private static final String W_KEY = "W";
	private static final String N_PROS = "numberOfProsumers";
	private static final String INTRA_KEY = "intra";
	private static final int INITIAL_ROW_T = 0;
	private static final int TUP_COL = 0;
	private static final int TDW_COL = 1;
	// TODO apply the right scale factor 10 amount of consumers, later take the
	// amou8nt from the confg file
	private static final BigDecimal SCALE_FACTOR;

	/**
	 * Singleton instance for the object
	 */
	private static OptParamEstimator ope = new OptParamEstimator();
	/**
	 * Target flexibility where tUp is t[0] and tDown is t[1]
	 */
	private static BigDecimal[][] t;
	/**
	 * Optimization W parameter
	 */
	private static BigDecimal w = new BigDecimal(0);
	private static boolean exeIntraDay = false;
	private static BigDecimal tSize;

	// Reading the properties files
	static {
		
		Properties properties;
		properties = Utility.getProperties(FILE_NAME_PROPERTIES_NUM_PROS);
		BigDecimal npros = MathUtility.roundBigDecimalTo(
				new BigDecimal(
						Double.parseDouble(properties.getProperty(N_PROS))), 6);
		SCALE_FACTOR = new BigDecimal(5000).divide(npros);
		
		Workbook workbook = null;
		// Loading Target Flex values
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try (InputStream resourceStream = classLoader
				.getResourceAsStream(FILE_NAME_PROPERTIES_T)) {
			workbook = Workbook.getWorkbook(resourceStream);
		} catch (IOException e) {
			System.out.println("Excel file not Found: "
					+ FILE_NAME_PROPERTIES_T);
		} catch (BiffException e) {
			e.printStackTrace();
		}
		Sheet sheet = workbook.getSheet(0);
		NumberCell tUpCell;
		NumberCell tDwCell;

		boolean finished = false;
		t = new BigDecimal[2][sheet.getRows()];// supposed to be 24
		for (int row = INITIAL_ROW_T; !finished && row < sheet.getRows(); row++) {
			tUpCell = (NumberCell) sheet.getCell(TUP_COL, row);// Target UP
			tDwCell = (NumberCell) sheet.getCell(TDW_COL, row);// Target Down
			// Target Flex Scaled down and rounded
			t[0][row] = scaleDownTargetFlex(MathUtility.roundDoubleTo(
					tUpCell.getValue(), 6));
			t[1][row] = scaleDownTargetFlex(MathUtility.roundDoubleTo(
					tDwCell.getValue(), 6));
		}

		tSize = BigDecimal.ZERO;// Calculating tSize
		for (int i = 0; i < t[0].length; i++) {
			BigDecimal a = t[0][i].pow(2);
			BigDecimal b = t[1][i].pow(2);
			a = a.add(b);
			tSize = tSize.add(a);
		}
		tSize = MathUtility
				.roundBigDecimalTo(tSize.subtract(BigDecimal.ONE), 6);

		// Loading W values
		properties = Utility.getProperties(FILE_NAME_PROPERTIES_W);
		w = MathUtility.roundBigDecimalTo(
				new BigDecimal(
						Double.parseDouble(properties.getProperty(W_KEY))), 6);
		// Decides if executing the intraday or not
		if (properties.getProperty(INTRA_KEY).compareToIgnoreCase("Y") == 0)
			exeIntraDay = true;
	};

	/**
	 * Singleton Pattern implementation
	 * 
	 * @return OptParamEstimator object
	 */
	public static OptParamEstimator getInstance() {
		if (ope == null)
			ope = new OptParamEstimator();
		return ope;
	}

	/**
	 * Constructor
	 */
	private OptParamEstimator() {

	}

	/**
	 * Get the T up[0] and down[1] given
	 * 
	 * @param midnight
	 *            the day the target is required
	 * @return
	 */
	public BigDecimal[][] getT(Date midnight) {
		return t;
	}

	/**
	 * @param midnight
	 * @return
	 */
	public BigDecimal[] getTUp(Date midnight) {
		return t[0];
	}

	/**
	 * @param midnight
	 * @return
	 */
	public BigDecimal[] getTDw(Date midnight) {
		return t[1];
	}

	/**
	 * @param midnight
	 * @return
	 */
	public BigDecimal getW(Date midnight) {
		return w;
	}

	// TODO implement logic
	public BigDecimal getTSize(Date midnight) {
		return tSize;
	}

	/**
	 * Scales down the target flexibility
	 * 
	 * @param flexToScale
	 * @return Flexibility scaled by SCALE_FACTOR
	 */
	// TODO scale down by 5000 and multiply by number of prosumers !!!!
	private static BigDecimal scaleDownTargetFlex(BigDecimal flexToScale) {
		return flexToScale.divide(SCALE_FACTOR);
	}

	public static boolean exeIntra() {
		return exeIntraDay;
	}
}
