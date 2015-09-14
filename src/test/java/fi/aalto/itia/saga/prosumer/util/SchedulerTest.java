package fi.aalto.itia.saga.prosumer.util;
import java.util.Date;

import org.junit.Test;

public class SchedulerTest {

	@Test
	public void prepareStringOutTest(){
		//int h, int r, double[] k, double s0,
		//double sh, double pmax, double[] q, double w, double[] t,
		//double tsize, String outDir, int id)
		double[] x = {1d,2d,3d,4d};
		System.out.println(Scheduler.prepareStringOut(1, 2, x, 3d, 4d, 5d, x, 6d, x, x, 7d,"C:/Users/giovanc1/MATLAB/source/result.txt"));
	}
	
	@Test
	public void optimizeMatlabTest(){
		double[] x = {1d,2d,3d,4d};
		Scheduler.optimizeMatlab(4, 2, x, 3d, 4d, 5d, x, 6d, x, x, 7d, new Date());
		//TODO check the file and matlabscript
	}

}
