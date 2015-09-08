package fi.aalto.itia.saga.prosumer.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

import fi.aalto.itia.saga.aggregator.util.SpotPriceEstimator;
import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.storage.StorageController;

public class OptimizedScheduler implements EnergyScheduler {

	public OptimizedScheduler() {
		// TODO Auto-generated constructor stub
	}

	public static void jOptimizeTest(TimeSequencePlan dayAheadConsumption,
			StorageController storageController) {
		double[] spotPrices = SpotPriceEstimator.getInstance()
				.getSpotPrice(dayAheadConsumption.getStart()).getUnitToArray();
		double[] k = ArrayUtils.addAll(spotPrices, spotPrices);// 2xLength
		double[] dayAheadQ = dayAheadConsumption.getUnitToArray();
		double[] q = ArrayUtils.addAll(dayAheadQ, dayAheadQ);// 2xLength
		Mean mean = new Mean();
		double kMean = 1 / mean.evaluate(spotPrices);
		double qnmax = storageController.getStorageCapacityW();
		// TODO TODO MUST BE CHANGES WITH THE PREDICTED BATTERY STATUS AT DAY
		// AHEAD MIDNIGHT
		double qn0 = storageController.getStorageStatusW();
		double pnmax = storageController.getStorageMaxChargingRateWh();
		// Objective function (plane)
		// TODO is needed 1/MEAN(K)
		

		LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(
				dayAheadConsumption.getUnitToArray(), 0);

		// inequalities (polyhedral feasible set G.X<H )
		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
		double[][] G = new double[][] { initVectorTo(24, -1d),
				initVectorTo(24, 1d), { -2., -1. }, { 1. / 3., 1. } };
		double[] h = new double[] { 0.,
				storageController.getStorageMaxChargingRateWh(), 2., 1. / 2. };
		inequalities[0] = new LinearMultivariateRealFunction(G[0], -h[0]);
		inequalities[1] = new LinearMultivariateRealFunction(G[1], -h[1]);
		inequalities[2] = new LinearMultivariateRealFunction(G[2], -h[2]);
		inequalities[3] = new LinearMultivariateRealFunction(G[3], -h[3]);

		// optimization problem
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(objectiveFunction);
		or.setFi(inequalities);
		// or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point,
		// not mandatory
		or.setToleranceFeas(1.E-9);
		or.setTolerance(1.E-9);

		// optimization
		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(or);
		try {
			int returnCode = opt.optimize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[] sol = opt.getOptimizationResponse().getSolution();
	}

	public static double[] initVectorTo(int size, double value) {
		double d[] = new double[size];
		for (int i = 0; i < size; i++) {
			d[i] = value;
		}
		return d;
	}

	public static double[][] getLowerTriangularMatrix(int size, double value) {
		double d[][] = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (i<=j)
					d[i][j] = value;
				else
					d[i][j] = 0;
			}
		}
		return d;
	}

}
