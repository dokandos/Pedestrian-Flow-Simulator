package Simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.DebugGraphics;

/**
 * Class with the required information for the simulation
 * 1. Arrival rates per period of 5 minutes
 * 2. People's durations in the building determined by their entry period
 * 3. Routing modal probabilities from source floors to destination floors 
 * @author Jorge Huertas (huertas.ja@uniandes.edu.co)
 */
public class Information {

	//-------------------------------------------------------------------------------------------
	//---------------------------------------- Singleton ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Singleton of the class
	 */
	public static Information single_instance = null;

	//-------------------------------------------------------------------------------------------
	//----------------------------------- Public attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Arrival rate per period (position 0 reference the period 70)
	 */
	public ArrayList<Double> arrivalRates = new ArrayList<>();

	/**
	 * Durations inside the building
	 */
	public Hashtable<Integer, ArrayList<Double>> durations = new Hashtable<>();

	/**
	 * Probability of choosing stairs in a given floor
	 */
	public Double[] stairsChoiceProbability = new Double[11];

	/**
	 * Probability of choosing elevators in a given floor
	 */
	public Double[] elevatorsChoiceProbability = new Double[11];

	/**
	 * Probability of going from one floor to another using stairs
	 */
	public Double[][] stairsRoutingProbabilities = new Double[11][11];

	/**
	 * Probability of going from one floor to another using elevators
	 */
	public Double[][] elevatorsRoutingProbabilities = new Double[11][11];

	/**
	 * Number of people exiting the building at certain periods (Real data). Used to validate the model
	 */
	public int[][] out;

	//-------------------------------------------------------------------------------------------
	//------------------------------------- Constructor(s) --------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constructor of the class
	 */
	public Information() {
		readArrivalRates();
		readDurations();
		readModeProbabilities();
		readElevatorRoutingProbabilites();
		readStairsRoutingProbabilites();
		readOut();

	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------ Private methods --------------------------------------
	//-------------------------------------------------------------------------------------------

	private void readModeProbabilities() {
		stairsChoiceProbability = new Double[11];
		elevatorsChoiceProbability = new Double[11];
		BufferedReader bf;
		String line;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/od_matrix/mode_choice_probability.csv")), "UTF-8"));
			line = bf.readLine();
			line = bf.readLine();
			while (line!=null) {
				String[] parts = line.split(",");
				int floor = Integer.parseInt(parts[0]);
				double stairsProbability = Double.parseDouble(parts[1]);
				stairsChoiceProbability[floor] = stairsProbability;
				double elevatorProbability = Double.parseDouble(parts[2]);
				elevatorsChoiceProbability[floor] = elevatorProbability;
				line = bf.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void readElevatorRoutingProbabilites() {
		elevatorsRoutingProbabilities = new Double[11][11];
		BufferedReader bf;
		String line;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/od_matrix/elevators_routing_probability.csv")), "UTF-8"));
			line = bf.readLine();
			line = bf.readLine();
			while (line!=null) {
				String[] parts = line.split(",");
				for (int i = 1; i < parts.length; i ++) {
					elevatorsRoutingProbabilities[Integer.parseInt(parts[0])][i] = Double.parseDouble(parts[i]);
				}
				line = bf.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void readStairsRoutingProbabilites() {
		stairsRoutingProbabilities = new Double[11][11];
		BufferedReader bf;
		String line;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/od_matrix/stairs_routing_probability.csv")), "UTF-8"));
			line = bf.readLine();
			line = bf.readLine();
			while (line!=null) {
				String[] parts = line.split(",");
				for (int i = 1; i < parts.length; i ++) {
					stairsRoutingProbabilities[Integer.parseInt(parts[0])][i] = Double.parseDouble(parts[i]);
				}
				line = bf.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Method that reads the arrival rates data from the file in the ./data/simulations folder
	 */
	private void readArrivalRates() {
		arrivalRates = new ArrayList<>();
		BufferedReader bf;
		String line;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/arrival_rate.csv")), "UTF-8"));
			line = bf.readLine();
			line = bf.readLine();
			while (line!=null) {
				String[] parts = line.split(",");
				double rate = Double.parseDouble(parts[1]);
				arrivalRates.add(rate);
				line = bf.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Method that reads the durations data from the file in the ./data/simulation folder
	 * Once this file is read, it creates the Arraylist of ascending sorted durations per period
	 */
	private void readDurations() {
		BufferedReader bf;
		String line;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/durations.csv")), "UTF-8"));
			line = bf.readLine();
			line = bf.readLine();
			while (line!=null) {
				String[] parts = line.split(",");
				int periodo = Integer.parseInt(parts[2]) - 70;
				double duracion = Double.parseDouble(parts[4]);
				if(!durations.keySet().contains(periodo)) {
					ArrayList<Double> duraciones = new ArrayList<>();
					duraciones.add(2.0);	//The minimum duration inside the building is going to be 2 minutes
					durations.put(periodo, duraciones);
				}
				durations.get(periodo).add(duracion);
				line = bf.readLine();
			}
			bf.close();
			// Sort durations of every period
			for (Iterator<Integer> iterator = durations.keySet().iterator(); iterator.hasNext();) {
				int periodo = (int) iterator.next();
				Collections.sort(durations.get(periodo));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Reads the amount of people exiting the building per period (Real data)
	 */
	private void readOut() {
		BufferedReader bf;
		String line;
		int numCols;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/out2.csv")), "UTF-8"));
			line = bf.readLine();
			numCols = line.split(";").length - 1;
			out = new int[152][numCols];
			int pos;
			while (line!=null) {
				String[] parts = line.split(";");
				pos = Integer.parseInt(parts[0]) - 70;
				for(int i = 0; i < parts.length - 1; i++) {
					out[pos][i] = Integer.parseInt(parts[i+1]);
				}
				line = bf.readLine();
			}
			bf.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------- Static methods --------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Method that returns the arrival rate of a given period
	 * @param period the period. Period 0 refers to period 70
	 * @return the arrival rate of the given period
	 */
	public static double getArrivalRate(int period) {
		return Information.getInstance().arrivalRates.get(period);
	}

	/**
	 * Method that returns a random duration of a person given an entry period and a uniformly distributed random number.
	 * This method uses the inverse transformation of an empirical accumulated probability distribution of the durations given the entry period.
	 * @param period the entry period to determine the duration
	 * @param r uniformly distributed random number
	 * @return a random generated duration. 
	 */
	public static double getRandomDuration(int period, double r) {
		double d = 2.0;
		int l = 0;
		double F_l = 0.0;
		ArrayList<Double> D = Information.getInstance().durations.get(period);
		int N = D.size() - 1;
		double delta_p = 1.0/N;
		for (int i = 0; i < D.size()-1; i++) {
			if (i*delta_p <= r && r <= (i+1)*delta_p) {
				l = i;
				F_l = i*delta_p;
				break;
			}
		}
		double m = delta_p/(D.get(l+1)-D.get(l));
		d = D.get(l) + ((r-F_l)/m);
		return d;
	}

	/**
	 * Static method that returns a singleton of the class
	 * @return singleton of the class
	 */
	public static Information getInstance() {
		if (single_instance == null)
			single_instance = new Information();
		return single_instance;
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------- Main method of this class ---------------------------------
	//-------------------------------------------------------------------------------------------


	public static void main(String[] args) {
		Information.getInstance();
	}
//		ArrayList<Double> D = Information.getInstance().durations.get(43);
//		System.out.println(D);
//		int N = D.size()-1;
//		double p = 1.0/N;
//		PrintWriter pw;
//		try {
//			pw = new PrintWriter("./data_processing/durations/cumulatedDistribution.csv");
//			pw.println("i,d,F(d)");
//			for(int i=0; i <N; i++) {
//				pw.println(i+","+D.get(i)+","+i*p);
//			}
//			pw.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
