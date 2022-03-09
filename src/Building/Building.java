package Building;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;


import Entities.Controller;
import Entities.Elevator;
import Entities.Person;
import umontreal.iro.lecuyer.simevents.LinkedListStat;

/**
 * This class represents the building
 * @author Jorge Huertas (huertas.ja@uniandes.edu.co)
 */
public class Building {
	
	//-------------------------------------------------------------------------------------------
	//--------------------------------------- Constants -----------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Time exiting through a turnstile
	 */
	public static double TURNSTILE_TIME = 2.0/60;

	//-------------------------------------------------------------------------------------------
	//----------------------------------- Static attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Singleton of the building
	 */
	private static Building singleInstance = null;
	
	//-------------------------------------------------------------------------------------------
	//----------------------------------- Public attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Floors of the building
	 */
	public Hashtable<Integer, Floor> floors;

	/**
	 * Spaces of the floor
	 */
	public Hashtable<Integer, Space> spaces;

	/**
	 * Connections of the floor
	 */
	public Hashtable<Integer, Connection> connections;

	/**
	 * Elevators of the building
	 */
	public Hashtable<Integer, Elevator> elevators;

	/**
	 * Entering node (space) of the building
	 */
	public Space enteringSpace;

	/**
	 * Turnstiles for exiting the building
	 */
	public ArrayList<LinkedListStat<Person>> turnstiles;
	
	/**
	 * Code of the simulation (a-b-c-d indicates the amount of elevators per type)
	 */
	public String code;
	

	//-------------------------------------------------------------------------------------------
	//------------------------------- Constructor of the class ----------------------------------
	//-------------------------------------------------------------------------------------------

	public Building(){
		floors = new Hashtable<>();
		spaces = new Hashtable<>();
		connections = new Hashtable<>();
		elevators = new Hashtable<>();

		try {
			loadFloors();
			loadSpaces();
			loadConnections();
			//loadElevators();
			createTurnstiles(3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//printBuildingStructure();
	}


	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Methods ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Method that loads the floors from the floors.csv file in data folder.
	 * @throws Exception if the floors.csv file is not found or if there is an error reading the file.
	 */
	private void loadFloors() throws Exception {
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/building/floors.csv")), "UTF-8"));
		String line;
		line = bf.readLine();
		line = bf.readLine();
		while (line != null) {
			String[] parts = line.split(",");
			int floorNumber = Integer.parseInt(parts[0]);
			double height = Double.parseDouble(parts[1]);
			Floor floor = new Floor(floorNumber, height);

			floors.put(floorNumber, floor);
			line = bf.readLine();
		}
		bf.close();
	}

	/**
	 * Method that loads the spaces from the spaces.csv file in data folder.
	 * @throws Exception if the spaces.csv file is not found or if there is an error reading the file.
	 */
	private void loadSpaces() throws Exception{
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/building/spaces.csv")), "UTF-8"));
		String line;
		line = bf.readLine();
		line = bf.readLine();
		while (line != null) {
			String[] parts = line.split(",");
			int id = Integer.parseInt(parts[0]);
			String name = parts[1];
			int floorNumber = Integer.parseInt(parts[2]);
			int type = Integer.parseInt(parts[3]);
			Floor floor = floors.get(floorNumber);

			Space space = new Space(id, name, floor, type, 1000.0);
			spaces.put(id, space);
			if (space.type == Space.ENTRANCE)
				enteringSpace = space;
			line = bf.readLine();
		}
		bf.close();
	}

	/**
	 * Method that loads the spaces from the connections.csv file in data folder.
	 * @throws Exception if the connections.csv file is not found or if there is an error reading the file.
	 */
	private void loadConnections() throws Exception{
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/building/connections.csv")), "UTF-8"));
		String line;
		line = bf.readLine();
		line = bf.readLine();
		while (line != null) {
			String[] parts = line.split(",");
			int id = Integer.parseInt(parts[0]);
			int idSpace1 = Integer.parseInt(parts[3]);
			int idSpace2 = Integer.parseInt(parts[4]);
			double length = Double.parseDouble(parts[5]);
			double width = Double.parseDouble(parts[6]);
			Space space1 = spaces.get(idSpace1);
			Space space2 = spaces.get(idSpace2);

			Connection connection = new Connection(id, space1, space2, length, width);
			connections.put(id, connection);

			line = bf.readLine();
		}
		bf.close();
	}

	/**
	 * Method to check if the structure of the building was correctly loaded.
	 * It prints the structure of the building. 
	 */
	private void printBuildingStructure() {
		for (Iterator<Floor> iterator = floors.values().iterator(); iterator.hasNext();) {
			Floor floor = (Floor) iterator.next();
			String name = floor.elevatorsHall == null? "no hay": floor.elevatorsHall.name;
			System.out.println("Floor: " + floor.floorNumber + " - # of spaces: " + floor.spaces.size() + " - Elevators hall: " + name);
			for (Iterator<Space> it = floor.spaces.iterator(); it.hasNext();) {
				Space s = (Space) it.next();
				System.out.println("\tSpace: " + s.name + " - # of Connections: " + s.connections.size());
				for (Iterator<Connection> k = s.connections.iterator(); k.hasNext();) {
					Connection c = (Connection) k.next();
					System.out.println("\t\t" + c.type + ": " + c.space1.name + "-" + c.space2.name);
				}
			}
		}
	}

	/**
	 * Creates a specified number of elevators and saves the code of the simulation
	 * @param num Number of elevators to be created
	 */
	public void loadElevators() {
		elevators = new Hashtable<>();
		BufferedReader bf;
		String line;
		String pCode = "";
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./data/simulation/Elevators.txt"))));
			line = bf.readLine();
			line = bf.readLine();
			int elevId = 1;
			while (line!=null) {
				String[] vals = line.split("\t");
				int elevNum = Integer.parseInt(vals[1]);
				for (int i = 0; i < elevNum; i++) {
					Elevator e = new Elevator(vals[0]);
					elevators.put(elevId, e);
					elevId ++;
				}
				pCode += vals[0] + "-" + vals[1] + "_";
				line = bf.readLine();
			}
			bf.close();
			code = pCode;
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Creates a specified number of turnstiles
	 * @param num Number of turnstiles to be created
	 */
	private void createTurnstiles(int num) {
		turnstiles = new ArrayList<LinkedListStat<Person>>();
		for (int i = 0; i < num; i++) {
			turnstiles.add(new LinkedListStat<Person>());
		}
	}

	/**
	 * Gives the specified floor's elevator waiting line
	 * @param floor Floor to look for the waiting line
	 * @return waiting line (LinkedListStat)
	 */
	public LinkedListStat<Person> getElevatorWaitingLine(int floor) {
		Floor f = floors.get(floor);
		for (Space s: f.spaces) {
			if (s.type == Space.ELEVATORS_HALL)
				return s.inFloor;
		}
		return null;
	}

	/**
	 * Resets the spaces, connections, floors, elevators and turnstiles.
	 */
	public void reset() {
		Enumeration<Space> s = spaces.elements();
		while (s.hasMoreElements()) {
			Space sp = s.nextElement();
			sp.reset();
		}

		Enumeration<Connection> c = connections.elements();
		while (c.hasMoreElements()) {
			Connection co = c.nextElement();
			co.reset();
		}

		Enumeration<Floor> f = floors.elements();
		while (f.hasMoreElements()) {
			Floor fl = f.nextElement();
			fl.reset();
		}

		Enumeration<Elevator> e = elevators.elements();
		while (e.hasMoreElements()) {
			Elevator el = e.nextElement();
			el.reset();
		}
		
		createTurnstiles(turnstiles.size());
	}

	/**
	 * Gives the floor level (in meters) of a specific floor
	 * @param theFloor Floor to calculate the level
	 * @return Floor level
	 * @throws Exception if specified floor is not in the building
	 */
	public double getFloorLevel(int theFloor) throws Exception {
		if(theFloor < 0 || theFloor >= floors.size())
			throw new Exception("getFloorLevel(" + theFloor + "): argument out of range.");

		double d_temp = 0;
		for(int key: floors.keySet()) {
			if (key < theFloor) {
				d_temp += floors.get(key).height;
			}
		}

		return d_temp;
	}

	/**
	 * Sets the up button in a specified floor
	 * @param floor Set button for that floor
	 * @throws Exception if specified floor is not in the building
	 */
	public void setFloorButtonUp(int floor) throws Exception{
		if(floor < 0 || floor >= floors.size())
			throw new Exception("setFloorButtonUp(int): argument out of range.");

		floors.get(floor).upButton = true;
	}

	/**
	 * Unsets the up button in a specified floor
	 * @param floor Unset button for that floor
	 * @throws Exception if specified floor is not in the building
	 */
	public void unsetFloorButtonUp(int floor) throws Exception {
		if(floor < 0 || floor >= floors.size())
			throw new Exception("unsetFloorButtonUp(int): argument out of range.");

		floors.get(floor).upButton = false;
	}

	/**
	 * Sets the down button in a specified floor
	 * @param floor Set button for that floor
	 * @throws Exception if specified floor is not in the building
	 */
	public void setFloorButtonDown(int floor) throws Exception{
		if(floor < 0 || floor >= floors.size())
			throw new Exception("setFloorButtonDown(int): argument out of range.");

		floors.get(floor).downButton = true;
	}

	/**
	 * Unsets the down button in a specified floor
	 * @param floor Unset button for that floor
	 * @throws Exception if specified floor is not in the building
	 */
	public void unsetFloorButtonDown(int floor) throws Exception {
		if(floor < 0 || floor >= floors.size())
			throw new Exception("unsetFloorButtonDown(int): argument out of range.");

		floors.get(floor).downButton = false;
	}

	/**
	 * Gives the space Elevators_Hall for a specified floor
	 * @param floor Floor
	 * @return Elevators hall (space)
	 */
	public Space getElevatorHall(int floor){
		return floors.get(floor).elevatorsHall;
	}

	/**
	 * Connects the elevators to the controller
	 * @param cont Controller
	 */
	public void connectElevatorsToController(Controller cont){
		Enumeration<Integer> elev =  elevators.keys();
		while (elev.hasMoreElements()) {
			elevators.get(elev.nextElement()).control = cont;
		}
	}
	
	/**
	 * Chooses the turnstile with the least amount of people and gives the time to exit for that person
	 * @param person Person to add to the turnstile
	 * @return Time to exit
	 */
	public double chooseTurnstileAndGiveTime(Person person) {
		int min = 100;
		int index = -1;
		int size;
		for(int i = 0; i < turnstiles.size(); i++) {
			size = turnstiles.get(i).size();
			if (size < min) {
				min = size;
				index = i;
			}
		}
		turnstiles.get(index).add(person);
		person.turnstile = index;
		return turnstiles.get(index).size() * TURNSTILE_TIME;
	}
	
	/**
	 * Removes a specific person from the turnstile input queue.
	 * @param person Person to be removed
	 */
	public void removeFromTurnstile(Person person) {
		int index = person.turnstile;
		turnstiles.get(index).remove(person);
	}
	
	/**
	 * Gives the size of elevator's waiting line of the first floor
	 * @return Size of waiting line
	 */
	public int sizeElevFF() {
		return getElevatorWaitingLine(1).size();
	}
	
	/**
	 * Gives the amount of people at the exit turnstiles
	 * @return Amount of people
	 */
	public int sizeTurnstilesFF() {
		int ans = 0;
		for(int i = 0; i < turnstiles.size(); i++) {
			ans += turnstiles.get(i).size();
		}
		return ans;
	}
	
	/**
	 * Gives the amount of people traveling (walking) in the first floor
	 * @return Amount of people
	 */
	public int sizeTravelingFF() {
		int ans = 0;
		Floor f1 = floors.get(1);
		Space sp;
		Path ex;
		for(int i = 0; i < f1.spaces.size(); i++) {
			sp = f1.spaces.get(i);
			for(int j = 0; j < sp.exitingPaths.size(); j++) {
				ex = sp.exitingPaths.get(j);
				if (ex.type == Path.FLAT) {
					ans += ex.traveling.size();
				}
				ans += ex.inputQueue.size();
			}
			ans += sp.inputQueue.size();
		}
		return ans;
	}
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Static methods --------------------------------------
	//-------------------------------------------------------------------------------------------



	/**
	 * Static method that returns a singleton of the building.
	 * @return singleton of the building.
	 */
	public static Building getInstance() {
		if (singleInstance == null) 
			singleInstance = new Building();

		return singleInstance;
	}

	public static void main(String[] args) {
		Building.getInstance().printBuildingStructure();
	}
}
