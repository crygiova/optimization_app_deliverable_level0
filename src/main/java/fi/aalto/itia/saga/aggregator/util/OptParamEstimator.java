package fi.aalto.itia.saga.aggregator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import fi.aalto.itia.saga.util.MathUtility;
import fi.aalto.itia.saga.util.Utility;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class OptParamEstimator {

	private static final String FILE_NAME_PROPERTIES_T = "targetFlex.xls";
	private static final String FILE_NAME_PROPERTIES_W = "wParameter.properties";
	private static final String W_KEY = "W";
	private static final int INITIAL_ROW_T = 0;
	private static final int TUP_COL = 0;
	private static final int TDW_COL = 1;
	// TODO apply the right scale factor
	private static final double SCALE_FACTOR = 1;

	/**
	 * Singleton instance for the object
	 */
	private static OptParamEstimator ope = new OptParamEstimator();
	/**
	 * Target flexibility where tUp is t[0] and tDown is t[1]
	 */
	private static double[][] t;
	/**
	 * Optimization W parameter
	 */
	private static double w = 0;
	private static double tSize;

	static {
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
		t = new double[2][sheet.getRows()];// supposed to be 24
		for (int row = INITIAL_ROW_T; !finished && row < sheet.getRows(); row++) {
			tUpCell = (NumberCell) sheet.getCell(TUP_COL, row);// Target UP
			tDwCell = (NumberCell) sheet.getCell(TDW_COL, row);// Target Down
			// Target Flex Scaled down and rounded
			t[0][row] = scaleDownTargetFlex(MathUtility.roundDoubleTo(
					tUpCell.getValue(), 6));
			t[1][row] = scaleDownTargetFlex(MathUtility.roundDoubleTo(
					tDwCell.getValue(), 6));
		}
		// Calculating tSize
		tSize = 0;
		for (int i = 0; i < t[0].length; i++) {
			tSize += Math.pow(t[0][i], 2);
			tSize += Math.pow(t[1][i], 2);
		}
		// Loading W values
		Properties properties;
		properties = Utility.getProperties(FILE_NAME_PROPERTIES_W);
		w = Double.parseDouble(properties.getProperty(W_KEY));
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
	public double[][] getT(Date midnight) {
		return t;
	}

	/**
	 * @param midnight
	 * @return
	 */
	public double[] getTUp(Date midnight) {
		return t[0];
	}

	/**
	 * @param midnight
	 * @return
	 */
	public double[] getTDw(Date midnight) {
		return t[1];
	}

	/**
	 * @param midnight
	 * @return
	 */
	public double getW(Date midnight) {
		return w;
	}

	// TODO implement logic
	public double getTSize(Date midnight) {
		return tSize;
	}

	/**
	 * Scales down the target flexibility
	 * 
	 * @param flexToScale
	 * @return Flexibility scaled by SCALE_FACTOR
	 */
	//TODO scale down by 5000 and multiply by number of prosumers !!!!
	private static double scaleDownTargetFlex(double flexToScale) {
		return flexToScale / SCALE_FACTOR;
	}

}
