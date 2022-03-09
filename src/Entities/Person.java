package Entities;

import java.util.ArrayList;

import Building.Building;
import Building.Path;
import Simulator.Information;
import Simulator.Simulator_Base_Model;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;

/**
 * Class that represents a person
 */
public class Person {// implements Comparable<Person>{
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Constants -------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Space occupied by a person 
	 */
	public final static double SPACE_OCCUPIED = 0.8;

	/**
	 * Type of current movement Elevator
	 */
	public final static String TYPE_ELEVATOR = "Elevator";

	/**
	 * Type of current movement Stairs
	 */
	public final static String TYPE_STAIRS = "Stairs";
	
	//-------------------------------------------------------------------------------------------
	//----------------------------------- Static attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Random Stream for the number of floors to go to
	 */
	public static RandomStream  streamNumFloors = new MRG32k3a();

	/**
	 * Random Stream for the floor to go
	 */
	public static RandomStream  streamFloor = new MRG32k3a();

	/**
	 * Random Stream for the mode of transport to take
	 */
	public static RandomStream streamMode = new MRG32k3a();

	/**
	 * Number of the person. This will update every time a person is created
	 */
	public static int num = 0;

	/**
	 * Enumaration for the passenger status
	 */
	public enum PassengerStatus {
		WAITING, 
		TRAVELING,
		FINISHED
	}
	
	//-------------------------------------------------------------------------------------------
	//----------------------------------------- Attributes --------------------------------------
	//-------------------------------------------------------------------------------------------
	/**
	 * Id of the person
	 */
	public int id;

	/**
	 * Origin floor of the person
	 */
	public int originFloor;

	/**
	 * Destination floor of the person
	 */
	public int destinationFloor;

	/**
	 * Current floor of the entity
	 */
	public int currentFloor;

	/**
	 * Arrival time of the person
	 */
	public double arrivalTime;

	/**
	 * Duration of the person in the floor
	 */
	public double duration;

	/**
	 * Type of movement
	 */
	public String typeOfMovement;

	/**
	 * Time at which the entity started to move towards her destination
	 */
	public double timeStartMovement;

	/**
	 * Time at which the entity arrived to her destination
	 */
	public double timeEndMovement;

	/**
	 * Elevator used
	 */
	public int elevator;

	/**
	 * Boarding time of the person to an elevator
	 */
	public double boardingTime;

	/**
	 * Alighting time of the elevator
	 */
	public double alightingTime;

	/**
	 * Loading time of the person to an elevator
	 */
	public double loadingTime;

	/**
	 * Unloading time of the person from an elevator
	 */
	public double unloadingTime;

	/**
	 * Loading threshold of the person to an elevator
	 */
	public double loadingThreshold;

	/**
	 * Status of the person
	 */
	public PassengerStatus status;

	/**
	 * Velocity of the person in m/m
	 */
	public double velocity;
	
	/**
	 * Time of the last velocity change
	 */
	public double timeLastVelocityChange;
	
	/**
	 * Position of the last velocity change
	 */
	public double positionLastVelocityChange;
	
	/**
	 * Event representing the departure from a path
	 */
	public Event connectionDeparture;
	
	/**
	 * List of the destinations (floors)
	 */
	public ArrayList<Integer> route;
	
	/**
	 * List of the types of transport (stairs or elevator)
	 */
	public ArrayList<String> types;
	
	/**
	 * Average time traveling between floors
	 */
	public double averageTimeTravelingBetweenFloors;
	
	/**
	 * Number of floors traveled
	 */
	public int numFloorsTraveled;
	
	/**
	 * Last used path
	 */
	public Path lastUsedPath;
	
	/**
	 * Time of departure from the origin of a trip
	 */
	public double originTimeOfDeparture;
	
	/**
	 * Period in which the person begins to move to another floor
	 */
	public int periodStartMovement;
	
	/**
	 * Type of simulation. Default set to "M"
	 */
	public String typeOfSimulation;
	
	/**
	 * Turnstile used to exit
	 */
	public int turnstile;
	
	//-------------------------------------------------------------------------------------------
	//----------------------------------- Constructor -------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constructor of the class
	 * @param origin Origin floor
	 * @param loadingTime Loading time to an elevator
	 * @param unloadingTime Unloading time of an elevator
	 * @param loadingThreshold Loading threshold to an elevator
	 * @param typeOfSimulation Type of simulation
	 */
	public Person(int origin, double loadingTime, double unloadingTime, double loadingThreshold, String typeOfSimulation) {
		num++; // Update the number of entities created

		//Set the person's basic information
		this.id = num;
		this.loadingTime = loadingTime;
		this.unloadingTime = unloadingTime;
		this.loadingThreshold = loadingThreshold;

		// Set the initial information
		this.originFloor = origin;
		this.currentFloor = this.originFloor;
		this.arrivalTime = Sim.time();

		//Set the destination information
		setDestination();

		averageTimeTravelingBetweenFloors = 0;
		numFloorsTraveled = 0;

		status = PassengerStatus.FINISHED;
		this.typeOfSimulation = typeOfSimulation;
	}
	
	//-------------------------------------------------------------------------------------------
	//-------------------------------------- Methods --------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Sets destination
	 */
	public void setDestination() {
		this.originFloor = this.currentFloor;
		this.currentFloor = this.originFloor;
		this.timeStartMovement = Sim.time();
		resetMovementVariables();
	}

	/**
	 * Resets some attributes for the next rep
	 */
	public void resetMovementVariables() {
		this.timeEndMovement = -1.0;
		this.elevator = -1;
		this.boardingTime = -1.0;
		this.alightingTime = -1.0;
	}

	/**
	 * Calculates the velocity inside a given path
	 * @param path Path to travel
	 * @return Velocity
	 */
	public double calculateVelocity(Path path) {
		double velocity = -1.0;
		int peopleAhead = path.traveling.indexOf(this);
		double density = peopleAhead / (path.length * path.width);
		if (path.type == Path.FLAT) {
			if(density <= 0.8) {
				velocity = 1.4;
			}
			else if(density <= 6.1){
				velocity = -0.22 + 3.2/(1+Math.pow(density/0.8, 0.9));
			}
			else {
				velocity = 0.001;
			}
		}
		else if (path.type == Path.DOWN) {
			if(density <= 6.1){
				velocity = 0.8521 - density * 0.1205;
			}
			else {
				velocity = 0.001;
			}
		}
		else if (path.type == Path.UP) {
			if(density <= 6.1){
				velocity = 0.7371 - density * 0.1205;
			}
			else {
				velocity = 0.001;
			}
		}
		return velocity * 60;
	}

	/**
	 * Updates last velocity change time
	 */
	public void updatePositionLastVelocityChange() {
		positionLastVelocityChange += velocity * (Sim.time() - timeLastVelocityChange);
	}

	/**
	 * Updates velocity
	 */
	public void updateVelocity(Path path) {
		velocity = calculateVelocity(path);
	}

	/**
	 * Sets the route to take and the types of transport
	 */
	public void setRoute() {
		route = new ArrayList<Integer>();
		types = new ArrayList<String>();
		Double[][] probabilities;
		Double[][] probabilitiesElevator;
		Double[][] probabilitiesStairs;
		Double[] mode;
		probabilitiesElevator = Information.getInstance().elevatorsRoutingProbabilities;
		probabilitiesStairs = Information.getInstance().stairsRoutingProbabilities;
		mode = Information.getInstance().stairsChoiceProbability;

		//Set the number of floors
		double var = duration * streamNumFloors.nextDouble();
		int numFloors = (int) Math.ceil(var/Simulator_Base_Model.CLASS_DURATION);
		int currentFloor = 1;
		route.add(currentFloor);

		//TODO define type of movement.
		while(route.size() < numFloors + 1) {
			if (typeOfSimulation == "S") {
				types.add("S");
				typeOfMovement = TYPE_STAIRS;
				probabilities = probabilitiesStairs;
			}
			else if (typeOfSimulation == "E") {
				types.add("E");
				typeOfMovement = TYPE_ELEVATOR;
				probabilities = probabilitiesElevator;
			}
			else if (typeOfSimulation == "M") {
				if (streamMode.nextDouble() > mode[currentFloor]) {
					probabilities = probabilitiesElevator;
					types.add("E");
				}
				else {
					probabilities = probabilitiesStairs;
					types.add("S");
				}
			}
			else {
				new Error("Type of simulation does not match").printStackTrace();
				System.exit(1);
				probabilities = null;
			}

			double sum = 0;
			//Do not include floor #1
			for(int i = 2; i < probabilities[0].length;i++) {
				sum += probabilities[currentFloor][i] == null? 0 : probabilities[currentFloor][i]; 
			}
			double rescaled = streamFloor.nextDouble()*sum;
			double cumulative = 0;
			for(int i = 2; i < probabilities[0].length;i ++) {
				cumulative += probabilities[currentFloor][i] == null? 0:probabilities[currentFloor][i];
				if (rescaled < cumulative) {
					route.add(i);
					currentFloor = i;
					break;
				}
			}
			//			if (cumulative == 0 && currFlor != 0) {
			//				//Error
			//				System.out.println("ERROR: Routing probabilities for " + typeOfMovement + " are 0 in floor " + currFlor);
			//			}
			if (currentFloor == 0) {
				new Error("ERROR: Routing to Floor #0").printStackTrace();
				System.exit(1);
			}
			if (cumulative == 0 && currentFloor != 0) {
				route.add(currentFloor);
				break;
			}
		}
		if (streamMode.nextDouble() > mode[currentFloor]) {
			probabilities = probabilitiesElevator;
			types.add("E");
		}
		else {
			probabilities = probabilitiesStairs;
			types.add("S");
		}
		route.add(1);
		types.add("");
	}

	/**
	 * Gives the time this person should stay at a floor
	 * @param currentFloor Floor to stay at
	 * @return Time
	 */
	//TODO CHECK method for calculating time at floors
	public double giveTimeAtFloor(int currentFloor) {
		double timeRemaining = duration - (Sim.time() - arrivalTime);
		double ans = -1;
		if (timeRemaining <= (currentFloor - 1) * averageTimeTravelingBetweenFloors) {
			ans = 0;
			route = new ArrayList<Integer>();
			route.add(1);
			destinationFloor = 1;
		}
		else if (timeRemaining - (currentFloor - 1)  * averageTimeTravelingBetweenFloors <= Simulator_Base_Model.CLASS_DURATION){
			ans = timeRemaining - (currentFloor - 1)  * averageTimeTravelingBetweenFloors;
		}
		else {
			if (route.size() >= 3) {
				ans = Simulator_Base_Model.CLASS_DURATION;
			}
			else {
				ans = timeRemaining - (currentFloor - 1)  * averageTimeTravelingBetweenFloors;
			}
		}
		return ans;
	}

	/**
	 * Updates average traveling time. This method is only called when an entity travels between floors.
	 * @param Time
	 */
	//TODO CHECK revisar porque no estoy teniendo en cuenta si la persona se le hace rescheduling al tiempo que sale de un arco entre pisos
	public void updateAverageTravelingTime(double time) {
		double sum = averageTimeTravelingBetweenFloors * numFloorsTraveled;
		numFloorsTraveled ++;
		averageTimeTravelingBetweenFloors = (sum + time)/(numFloorsTraveled);
	}
	
	//-------------------------------------------------------------------------------------------
	//---------------------------------------- MAIN ---------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int i = 0;
		while (i < 10) {
			i++;
			Person p = new Person(1, 10, 10, 0.8, "M");
			p.duration = 670;
			//System.out.println(p.duration);
			p.setRoute();
			System.out.print(p.route);
			System.out.println("\t" + p.types);
		}
	}
	//
	//	@Override
	//	public int compareTo(Person o) {
	//		if (this.id < o.id) {
	//			return -1;
	//		}
	//		else if(this.id == o.id) {
	//			return 0;
	//		}
	//		else {
	//			return 1;
	//		}
	//	}
}
