package Simulator;

import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.probdist.StudentDist;

/**
 * Class that helps with statistics
 */
public class Statistics {
	
	//-------------------------------------------------------------------------------------------
	//-------------------------------------- Methods --------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Calculates the number of reps to execute the simulation
	 * @param outReal Matrix of the real number of people exiting the building
	 * @param outSimulation Matrix of the simulated number of people exiting the building
	 * @return Number of reps
	 */
	public static int reps (int[][] outReal, int[][] outSimulation) {
		double alpha = 0.05;
		double sigma_S;
		double sigma_R;
		int Rtemp;
		double t;
		int[] R1 = new int[152];
		for (int i = 0; i < R1.length; i++) {
			sigma_S = standardDeviation(outSimulation[i]);
			sigma_R = standardDeviation(outReal[i]);
			Rtemp = (int) Math.ceil(Math.pow(NormalDist.inverseF01(1 - alpha/2) * sigma_S/sigma_R, 2));
			if (Rtemp >= 2) {
				t = Math.pow(StudentDist.inverseF(Rtemp - 1, 1 - alpha/2) * sigma_S / sigma_R, 2);
				while (Rtemp < t) {
					Rtemp ++;
					t = Math.pow(StudentDist.inverseF(Rtemp - 1, 1 - alpha/2) * sigma_S / sigma_R, 2);
				}
			}
			R1[i] = Rtemp;
		}
		return (int) max(R1);
	}

	/**
	 * Gives the standard deviation of an array of data
	 * @param data Data to calculate the standard deviation
	 * @return Standard deviation
	 */
	private static double standardDeviation(int[] data) {
		double sum = 0;
		double mean;
		mean = mean(data);
		for(int i = 0; i < data.length; i++) {
			sum += Math.pow(data[i] - mean, 2);
		}
		return Math.sqrt(sum/data.length);
		
	}

	/**
	 * Gives the mean of an array of data
	 * @param data Data to calculate the mean
	 * @return Mean
	 */
	private static double mean(int[] data) {
		double sum = 0;
		double count = 0;

		for(int i = 0; i < data.length; i++) {
			sum += data[i];
			count ++;
		}

		return sum/count;
	}
	
	/**
	 * Gives the maximum number of an array of data
	 * @param data Data to calculate the maximum
	 * @return Max
	 */
	private static double max(int[] data) {
		double max = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i] > max)
				max = data[i];
		}
		return max;
	}
}

