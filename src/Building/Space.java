package Building;

import java.util.ArrayList;

import Entities.Person;
import umontreal.iro.lecuyer.simevents.LinkedListStat;

/**
 * This class represents a space in the building
 *
 */
public class Space {

	//-------------------------------------------------------------------------------------------
	//---------------------------------------- Constants ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Entrance type of space
	 */
	public final static int ENTRANCE = 0;

	/**
	 * Floor type of space
	 */
	public final static int FLOOR = 1;

	/**
	 * Stairs' hall type of space
	 */
	public final static int STAIRS_HALL = 2;

	/**
	 * Elevators' hall type of space
	 */
	public final static int ELEVATORS_HALL = 3;

	/**
	 * Types names
	 */
	public final static String[] TYPE_NAMES = {"Entrance", "Floor", "Stairs' hall", "Elevators' hall"};

	//-------------------------------------------------------------------------------------------
	//----------------------------------- Public attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Space id
	 */
	public int id;

	/**
	 * Space name
	 */
	public String name;

	/**
	 * Floor the space is in
	 */
	public Floor floor;

	/**
	 * The space type
	 */
	public int type;

	/**
	 * Space area (squared meters)
	 */
	public double area;

	/**
	 * Connections of the space
	 */
	public ArrayList<Connection> connections;
	
	/**
	 * Paths that take you out of this space. Created for calculating the amount of people in the space since it needs to include the people in these paths' input queues.
	 */
	public ArrayList <Path> exitingPaths;
	
	/**
	 * LinkedList for the input queue of this space
	 */
	public LinkedListStat<Person> inputQueue = new LinkedListStat<Person>();
	
	/**
	 * LinkedList for the people in the space. 
	 * Used only in spaces of type FLOOR and ELEVATORS_HALL to model people staying in classes or in the elevator's waiting line, respectively.
	 */
	public LinkedListStat<Person> inFloor = new LinkedListStat<Person>();
	
	/**
	 * Capacity of the space
	 */
	public double capacity;
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Constructor  ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constructor of the class
	 * @param id the space id
	 * @param floor the floor of the space
	 * @param name the space name
	 * @param area the space area
	 */
	public Space(int id, String name, Floor floor, int type, double area) {
		this.id = id;
		this.name = name;
		this.floor = floor;
		this.type = type;
		this.area = area;
		connections = new ArrayList<>();
		floor.spaces.add(this);
		if (this.type == ELEVATORS_HALL){
			floor.elevatorsHall = this;	
		}
		capacity = Person.SPACE_OCCUPIED * area;
		exitingPaths = new ArrayList<Path>();
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Methods ----------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Resets some attributes for the next rep
	 */
	public void reset() {
		inputQueue.clear();
		inFloor.clear();
	}
	
	/**
	 * Updates the exiting paths
	 */
	public void updateExitingPaths() {
		for (Connection c: connections) {
			exitingPaths.add(c.pathToUse(this));
		}
//		if (exitingPaths.size() == 0) {
//			System.out.println("ERROR: No arcs added to exiting paths to space " + TYPE_NAMES[type] + " at floor #" + floor.floorNumber);
//		}
	}
	
	/**
	 * Checks whether a person can enter the space.
	 * @return TRUE if the person can enter, or FALSE if not
	 */
	public boolean canEnter() {
		return capacity > peopleInSpace();
	}
	
	/**
	 *  Gives the amount of people in the space
	 * @return Amount of people in space
	 */
	public int peopleInSpace() {
		int num = 0;
		for(Path p: exitingPaths) {
			num += p.inputQueue.size();
		}
		return inFloor.size() + num;
	}

	/**
	 * Gives the path that connects this space to the space FLOOR of the same floor
	 * @return Path to use
	 */
	public Path pathToFloor() {
		Path ans = null;
		for(Connection c: connections) {
			if (c.space1.type == FLOOR || c.space2.type == FLOOR) {
				ans = c.pathToUse(this);
				break;
			}
		}
		return ans;
	}

	/**
	 * Gives the path that connects this space to the space STAIRS_HALL of the same floor
	 * @return Path to use
	 */
	public Path pathToStairsHall() {
		Path ans = null;
		for(Connection c: connections) {
			if (c.space1.type == STAIRS_HALL || c.space2.type == STAIRS_HALL) {
				ans = c.pathToUse(this);
				break;
			}
		}
		return ans;
	}

	/**
	 * Gives the path that connects this space to the space ELEVATORS_HALL of the same floor
	 * @return Path to use
	 */
	public Path pathToElevatorsHall() {
		Path ans = null;
		for(Connection c: connections) {
			if (c.space1.type == ELEVATORS_HALL || c.space2.type == ELEVATORS_HALL) {
				ans = c.pathToUse(this);
				break;
			}
		}
		return ans;
	}

	/**
	 * Gives the path that connects this space to the space STAIRS_HALL of the floor above
	 * @return Path to use
	 */
	public Path pathUpStairs() {
		Path ans = null;
		for(Connection c: connections) {
			if (c.space1.type == STAIRS_HALL || c.space2.type == STAIRS_HALL) {
				if (c.space1.floor.floorNumber > this.floor.floorNumber || c.space2.floor.floorNumber > this.floor.floorNumber) {
					ans = c.pathToUse(this);
					break;
				}
			}
		}
		if (ans == null)
			System.out.println("ERROR: Path is null");
		return ans;
	}

	/**
	 * Gives the path that connects this space to the space STAIRS_HALL of the floor below
	 * @return Path to use
	 */
	public Path pathDownStairs() {
		Path ans = null;
		for(Connection c: connections) {
			if (c.space1.type == STAIRS_HALL || c.space2.type == STAIRS_HALL) {
				if (c.space1.floor.floorNumber < this.floor.floorNumber || c.space2.floor.floorNumber < this.floor.floorNumber) {
					ans = c.pathToUse(this);
					break;
				}
			}
		}
		return ans;
	}

	/**
	 * Gives the path that connects this space to the space ENTRANCE
	 * @return Path to use
	 */
	public Path pathToEntrance() {
		Path ans = null;
		for(Connection c: connections) {
			if (c.space1.type == ENTRANCE || c.space2.type == ENTRANCE) {
				ans = c.pathToUse(this);
				break;
			}
		}
		if (ans == null) {
			new Error("ERROR: Path is null").printStackTrace();
			System.exit(1);
		}
		return ans;
	}
}