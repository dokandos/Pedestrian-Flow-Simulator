package Entities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import Building.Building;
import Building.Floor;
import Building.Space;
import Entities.Elevator.Direction;
import Simulator.Simulator_Base_Model;
import umontreal.iro.lecuyer.simevents.Accumulate;
/**
 * Class that represents the controller of the elevators
 */
public class Controller {
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Attributes ------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Simulator
	 */
	private Simulator_Base_Model sbm;

	/**
	 * Printwriter
	 */
	public PrintWriter pw;
	
	/**
	 * Hashtable that registers the usage of the elevators
	 */
	public Hashtable<String, Accumulate> usage;
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Constructor -----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constructor of the class
	 * @param sbm
	 */
	public Controller(Simulator_Base_Model sbm){
		this.sbm = sbm;
		usage = new Hashtable<String, Accumulate>();
	}
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Methods ---------------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Creates printriter to export elevator usage information.
	 */
	public void createPrintWriter(String dir) {
		try {
			pw = new PrintWriter(dir + "Elevator Usage.txt");
			pw.println("Elevator ID" + "\t" + "Period" + "\t" + "Rep" + "\t" + "Usage");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Executes group control algorithm when someone calls an elevator
	 ** Choose the closest elevator that is able to stop at this floor.
	 ** If no elevators fulfill the conditions, wait until updateAtDoorsClosed method is called.
	 * @param floor Floor number
	 * @param direction Direction of the elevator
	 */
	public void updateAtFloor(int floor, Direction direction) {
		Hashtable<Integer, Elevator> elevators = Building.getInstance().elevators;
		int min = 12;
		Elevator selected = null;

		//Select closest elevator to param floor that 
		for(int i = 1; i <= elevators.size(); i ++) {
			Elevator e = (Elevator) elevators.get(i);
			int distance = e.getDistanceToCall(floor, direction);
			if (min > distance && e.canStop(floor) && distance != -1) {
				min = distance;
				selected = e;
			}
		}

		if (selected != null) {
			//Check again if selected elevator can stop at floor.
			if (selected.canStop(floor)) {
				selected.committedDirection = direction;
				selected.setTarget(floor);
			}
		}
	}

	/**
	 * Executes group control algorithm when the elevator's doors close
	 * @param elevator Elevator
	 */
	public void updateAtDoorsClosed(int elevatorId) {
		Hashtable<Integer, Floor> floors = Building.getInstance().floors;
		Elevator elevator = Building.getInstance().elevators.get(elevatorId);
		//a. There are people inside the elevator
		if (elevator.inElevator.size() > 0) {
			if (elevator.committedDirection == Direction.UP) {
				for(int i = elevator.getCurrentFloor() + 1; i < floors.size(); i++) {
					if (elevator.buttons[i] || floors.get(i).upButton) {
						elevator.setTarget(i);
						break;
					}
				}
			}
			else if (elevator.committedDirection == Direction.DOWN) {
				for(int i = elevator.getCurrentFloor() - 1; i >= 0; i--) {
					if (elevator.buttons[i] || floors.get(i).downButton) {
						elevator.setTarget(i);
						break;
					}
				}
			}
			else {
				new Error("ERROR: Elevator committed direction should not be NONE. @Controller.updateAtDoorsClosed").printStackTrace();
				System.exit(1);
			}
		}
		//b. There are no people inside the elevator.
		//THESE ALGORITHMS NEED TO BE BETTER
		else {
			elevator.committedDirection = Direction.NONE;
			int floor = -1;
			int min = 12;
			for(int i = 0; i < floors.size(); i++) {
				if (floors.get(i).upButton || floors.get(i).downButton) {
					int distance = elevator.getDistanceToCall(i, Direction.NONE);
					if (distance < min) {
						min = distance;
						floor = i;
					}
				}
			}
			if (floor != -1) {
				//If both up and down buttons are pressed on the floor,  prioritize down button if floor is on lower half of the building,
				//otherwise prioritize up button
//				if (floors.get(floor).upButton && floors.get(floor).downButton) {
//					int lim = Math.round(floors.size()/2);
//					if (floor >= lim)
//						elevator.committedDirection = Direction.UP;
//					else
//						elevator.committedDirection = Direction.DOWN;
//				}
				if (floors.get(floor).upButton) {
					elevator.committedDirection = Direction.UP;
				}
				else if (floors.get(floor).downButton) {
					elevator.committedDirection = Direction.DOWN;
				}
				elevator.setTarget(floor);
			}
		}
		updateWaitingLine(elevator.getCurrentFloor());
	}

	/**
	 * Inserts the person to the ELEVATORS_HALL space
	 * @param space
	 * @param person
	 */
	public void arrivalToDestinationElevHall(Space space, Person person){
		sbm.new EntryToSpace(space, person).schedule(0);
	}

	/**
	 * Updates the amount of people in line at a floor
	 * @param floorNumber Floor to update
	 */
	public void updateWaitingLine(int floorNumber) {
		sbm.addToWaitingLines(floorNumber);
	}
	
	/**
	 * Updates the amount of people in the first floor
	 * @param floorNumber Floor to update
	 */
	public void updateFirstFloorElevators() {
		sbm.addToFirstFloorPeople(2);
	}
	
	/**
	 * Updates the usage of elevatos
	 * @param elevator Elevator to update
	 */
	public void updateUsage(int elevator) {

		if(elevator != -1) {
			String key = elevator + "\t" + sbm.currentPeriod + "\t" + sbm.currentRep;
			Elevator elev = Building.getInstance().elevators.get(elevator);
			double val = (double) elev.inElevator.size() / elev.capacity;

			if (usage.containsKey(key)) {
				usage.get(key).update(val);
			}
			else {
				Accumulate a = new Accumulate(key);
				a.update(val);
				usage.put(key,a);
			}
		}
		else {
			for(int i = 1; i <= Building.getInstance().elevators.size(); i++) {
				usage.get(i + "\t" + sbm.currentPeriod + "\t" + sbm.currentRep).setCollecting(false);
			}
		}
	}

	/**
	 * Prints the elevators' usage
	 */
	public void printElevatorUsage() {
		//"Elevator ID" + "\t" + "Period" + "\t" + "Rep" + "\t" + "Usage"
		Set<String> keys = usage.keySet();
		for(String key: keys){
			Accumulate ac = usage.get(key);
			pw.println(key + "\t" + ac.average());
		}
		pw.close();
	}

}

//	public void update() {
//		// cycle through the elevators in the group
//
//		Enumeration<Integer> keys = Building.getInstance().elevators.keys();
//
//		while (keys.hasMoreElements()) {
//			Elevator elev = Building.getInstance().elevators.get(keys.nextElement());
//			// commited direction is UP
//			if(elev.committedDirection == Elevator.Direction.UP) {
//				elev.elevatorRegistry.println("Commited Direction UP. Elev Direction = " + elev.elevatorDirection);
//				// elevator direction is UP
//				if(elev.elevatorDirection == Elevator.Direction.UP) {
//					int target = elev.target;
//
//					// if someone is going up in my way, then pick them up
//					for(int i = elev.getCurrentFloor() + 1; i < target; ++i)
//					{
//						if(Building.getInstance().floors.get(i).upButton && elev.canStop(i))
//							elev.setTarget(i);
//					}
//				}
//				else if(elev.elevatorDirection == Elevator.Direction.DOWN)
//				{
//					// elevator direction is down
//
//					int target = elev.target;
//
//					// if someone is going up below our current target, then aim for that floor instead
//					for(int i = target - 1; i >= 0; --i)
//					{
//						if(Building.getInstance().floors.get(i).upButton && elev.canStop(i))
//							elev.setTarget(i);
//					}
//				}
//				else
//				{
//					// elevator direction is none, i.e., the elevator is currently stationary at a floor.
//					// Simply scan for elevator buttons and up buttons in the floors beyond the current floor and aim for the nearest.
//					// if none found, switch to uncommitted. BEWARE OF SWITCHING TO UNCOMMITTED PREMATURELY!
//
//					int elev_button = -1;
//					for(int i = elev.getCurrentFloor() + 1; i < Building.getInstance().floors.size(); i++)
//					{
//						if(elev.buttons[i])
//						{
//							elev_button = i;
//							break;
//						}
//					}
//
//					int floor_button = -1;
//					for(int i = elev.getCurrentFloor() + 1; i < Building.getInstance().floors.size(); i++)
//					{
//						if(Building.getInstance().floors.get(i).upButton)
//						{
//							floor_button = i;
//							break;
//						}
//					}
//
//					if(floor_button != -1 && elev_button != -1)
//					{
//						elev.setTarget(Math.min(elev_button, floor_button));
//					}
//					else if(elev_button != -1)
//						elev.setTarget( elev_button);
//					else if(floor_button != -1)
//						elev.setTarget( floor_button);
//					//					doorStatus == 2
//					else if(elev.doorStatus == Elevator.DoorStatus.CLOSED) {
//						elev.committedDirection = Elevator.Direction.NONE;
//						if (peopleWaiting()) {
//							update();
//						}
//					}
//				}
//			}
//			else if(elev.committedDirection == Elevator.Direction.DOWN)
//			{
//				elev.elevatorRegistry.println("Commited Direction DOWN");
//				// Committed Direction is DOWN, same logic as before with few modifications where appropriate.
//
//				if(elev.elevatorDirection == Elevator.Direction.UP)
//				{
//					int target = elev.target;
//
//					for(int i = target + 1; i < Building.getInstance().floors.size(); i++)
//					{
//						if(Building.getInstance().floors.get(i).downButton && elev.canStop(i))
//							elev.setTarget(i);
//					}
//				}
//				else if(elev.elevatorDirection == Elevator.Direction.DOWN)
//				{
//					int target = elev.target;
//
//					for(int i = elev.getCurrentFloor() - 1; i > target; --i)
//					{
//						if(Building.getInstance().floors.get(i).downButton && elev.canStop(i))
//							elev.setTarget(i);
//					}
//				}
//				else
//				{
//					int elev_button = -1;
//					for(int i = elev.getCurrentFloor() - 1; i >=0; --i)
//					{
//						if(elev.buttons[i])
//						{
//							elev_button = i;
//							break;
//						}
//					}
//
//					int floor_button = -1;
//					for(int i = elev.getCurrentFloor()-1; i >= 0; --i)
//					{
//						if(Building.getInstance().floors.get(i).downButton)
//						{
//							floor_button = i;
//							break;
//						}
//					}
//
//					if(floor_button != -1 && elev_button != -1)
//					{
//						elev.setTarget(Math.max(elev_button, floor_button));
//					}
//					else if(elev_button != -1)
//						elev.setTarget(elev_button);
//					else if(floor_button != -1)
//						elev.setTarget(floor_button);
//					else if(elev.doorStatus == Elevator.DoorStatus.CLOSED)
//						elev.committedDirection = Elevator.Direction.NONE;
//				}
//
//			}
//			else
//			{
//				elev.elevatorRegistry.println("Commited Direction NONE");
//				// Ok, so we are standing at a floor free from any committment, WHAT SHOULD WE DO NOW??? THAT IS THE QUESTION ;)
//				// Here we will simply scan each floor from 0 to the top of the building, we will check for up button then down button on that floor,
//				// and respond correspondly.
//				// Note that this behaviour is deficient. Much better behaviour could be achieved, e.g, we can check the position of the elevator,
//				// if it is in the upper half of the building, then scan the following floors for down buttons first since it is more probable that passengers
//				// in the upper half would go down and it is more effecient since we have already crossed more than half the way!
//				// Note also we dont check if other elevators are actually heading for that floor we are aiming at. This could be done easily by looping
//				// through other elevators and checking if any other elevator can stop at that floor and have the same committed direction as the floor button
//				// and we could also check elevator loading and based on that info we could decide to ignore or answer the call. and don't forget that this is a continues allocation,
//				// so if we figured it out wrong this time, the same calculation will be done again in the next cycle.
//
//				for(int fl = 0; fl < Building.getInstance().floors.size(); fl++)
//				{
//					if(Building.getInstance().floors.get(fl).downButton)
//					{
//						elev.committedDirection = Elevator.Direction.DOWN;
//						elev.setTarget(fl);
//						break;
//					}
//					else if(Building.getInstance().floors.get(fl).upButton)
//					{
//						elev.committedDirection = Elevator.Direction.UP;
//						elev.setTarget(fl);
//						break;
//					}
//				}
//			}
//		}
//	}
//
//
//	public boolean peopleWaiting() {
//		Hashtable<Integer, Floor> f = Building.getInstance().floors;
//		for (int i = 0; i < f.size(); i++) {
//			Floor fl = (Floor) f.get(i);
//			if (fl.elevatorsHall.inFloor.size() > 0) {
//				return true;
//			}
//		}
//		return false;
//	}
//}
