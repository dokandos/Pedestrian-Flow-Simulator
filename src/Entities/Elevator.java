package Entities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;

import Building.Building;
import Building.Floor;
import Building.Space;
import Simulator.Kinematics;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.LinkedListStat;
import umontreal.iro.lecuyer.simevents.Sim;

/**
 * Class that represents an elevator
 */
public class Elevator {
	//-------------------------------------------------------------------------------------------
	//------------------------------------- Constants -------------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Number for the elevator id. Updates when a new elevator is created
	 */
	public static int num = 0;

	/**
	 * Enumeration for the direction of the elevator
	 */
	public enum Direction {
		NONE,
		UP,
		DOWN
	}
	
	/**
	 * Enumeration for the status of the elevator
	 */
	public enum DoorStatus {
		OPEN,
		OPENING,
		CLOSED,
		CLOSING
	}
	
	/**
	 * Elevator id
	 */
	public int id;
	
	/**
	 * Capacity of the elevator
	 */
	public int capacity;
	
	/**
	 * Rated velocity of the elevator
	 */
	public double ratedV;
	
	/**
	 * Rated acceleration of the elevator
	 */
	public double ratedA;
	
	/**
	 * Rated jerk of the elevator
	 */
	public double ratedJ;
	
	/**
	 * Door opening time of the elevator
	 */
	public double doorOpeningTime;
	
	/**
	 * Door closing time of the elevator
	 */
	public double doorClosingTime;
	
	/**
	 * Motor start delay of the elevator
	 */
	public double motorStartDelay;
	
	/**
	 * Advanced opening time of the elevator
	 */
	public double advanceOpening;
	
	/**
	 * Home floor of the elevator. Used to know at which floor the elevator begins at the beginning of the day
	 */
	public int homeFloor = 1;
	
	/**
	 * Initial direction of the elevator
	 */
	public Direction elevatorDirection = Direction.NONE;
	
	/**
	 * Initial door status of the elevator
	 */
	public DoorStatus doorStatus = DoorStatus.CLOSED;

	/**
	 * Initial door status of the elevator
	 */
	public Direction committedDirection = Direction.NONE;
	
	/**
	 * Origin floor of a trip
	 */
	public int originFloor;
	
	/**
	 * Target of a trip
	 */
	public int target;
	
	/**
	 * T1
	 */
	public double t1;
	
	/**
	 * T2
	 */
	public double t2;
	
	/**
	 * T3
	 */
	public double t3;
	
	/**
	 * T4
	 */
	public double t4;
	
	/**
	 * T5
	 */
	public double t5;
	
	/**
	 * T6
	 */
	public double t6;
	
	/**
	 * T7
	 */
	public double tEnd;
	
	/**
	 * Number of passengers inside the elevator
	 */
	public int passengerCount = 0;
	
	/**
	 * Represents if the elevator can stop to a certain floor
	 */
	public boolean canGetTarget = true;
	
	/**
	 * Represents the light between inside the doors (red light for the entrance of passengers)
	 */
	public boolean beamBroken = false;
	
	/**
	 * Current allocation time of the elevator
	 */
	public double currentAllocTime;
	
	/**
	 * Buttons inside the elevator
	 */
	public boolean buttons[] = new boolean[11];
	
	/**
	 * Journey time
	 */
	public double journeyTime = 0;
	
	/**
	 * Represents if an elevator is traveling or not
	 */
	public boolean inJourney = false;
	
	/**
	 * Absolute journey time
	 */
	public double absoluteJourneyTime = 0;
	
	/**
	 * Journey distance
	 */
	public double journeyDistance = 0;
	
	/**
	 * Last position of the elevator
	 */
	public double lastPos = 0;
	
	/**
	 * Condition (A,B or C)
	 */
	public char condition;
	
	/**
	 * Event T1
	 */
	public ElevT1 ET1;
	
	/**
	 * Event T2
	 */
	public ElevT2 ET2;
	
	/**
	 * Event T3
	 */
	public ElevT3 ET3;
	
	/**
	 * Event T4
	 */
	public ElevT4 ET4;
	
	/**
	 * Event T5
	 */
	public ElevT5 ET5;
	
	/**
	 * Event T6
	 */
	public ElevT6 ET6;
	
	/**
	 * Event T7
	 */
	public ElevArrived EArrived;
	
	/**
	 * LinkedList for the people inside the elevator
	 */
	public LinkedListStat<Person> inElevator;
	
	/**
	 * Controller of the elevators
	 */
	public Controller control;
	
	
	/**
	 * Used to know if an elevator can stop at a floor. Used to present alternatives
	 */
	public boolean[] canStopAtFloor;

	//-------------------------------------------------------------------------------------------
	//------------------------------------- Constructor -----------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Constructor of the class
	 * @param type Elevator type. Default is "N"
	 */
	public Elevator(String type) {
		num ++;
		id = num;
		capacity = 20;
		//Velocity, acceleration and jerk units: m/s. 2.5, 1, 2
		ratedV = 2;
		ratedA = 0.8;
		ratedJ = 1.8;
		doorClosingTime = 3/60;
		doorOpeningTime = 3/60;
		motorStartDelay = 1/60;
		advanceOpening = 3/60;
		reset();

		canStopAtFloor = new boolean[11];
		//Type of elevator
		canStopAtFloor[1] = true;
		switch(type) {
		//Lechero
		case "N":
			for (int i = 0; i < canStopAtFloor.length; i ++)
				canStopAtFloor[i] = true;
			break;
		//Express
		case "X":
			for (int i = 7; i < canStopAtFloor.length; i ++)
				canStopAtFloor[i] = true;
			break;
		//Even
		case "E":
			for (int i = 0; i < canStopAtFloor.length; i ++) {
				if (i % 2 == 0)
					canStopAtFloor[i] = true;
			}
			break;
		//Odd
		case "O":
			for (int i = 0; i < canStopAtFloor.length; i ++) {
				if (i % 2 == 1)
					canStopAtFloor[i] = true;
			}	
			break;
		default:
			new Error("Type does not match possible types. Type = " + type).printStackTrace();
			System.exit(1);
		}
	}


	//-------------------------------------------------------------------------------------------
	//------------------------------ Events for elevator ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Class that represents the arrival of an elevator
	 */
	class ElevArrived extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			try {
				inJourney = false;
				
				originFloor = target;

				if (committedDirection == Direction.UP) {
					Building.getInstance().unsetFloorButtonUp(originFloor);
				}

				if (committedDirection == Direction.DOWN) {
					Building.getInstance().unsetFloorButtonDown(originFloor);
				}


				elevatorDirection = Direction.NONE;
				canGetTarget = false;
				journeyTime = 0;
				lastPos = Building.getInstance().getFloorLevel(originFloor);
				buttons[originFloor] = false;

				doorStatus = DoorStatus.OPENING;
				new ElevDoorsOpen().schedule(doorOpeningTime - advanceOpening);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Class that represents that the doors are open
	 */
	class ElevDoorsOpen extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			doorStatus = DoorStatus.OPEN;
			canGetTarget = false;
			new PassengersStartAlighting().schedule(0);
		}
	}

	/**
	 * Class that represents the alighting of passengers
	 */
	class PassengersStartAlighting extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			boolean found = false;
			for(Person p: inElevator) {
				if(p.status == Person.PassengerStatus.TRAVELING && p.destinationFloor == getCurrentFloor() && !beamBroken) {
					p.status = Person.PassengerStatus.FINISHED;
					beamBroken = true;
					inElevator.remove(p);
					new PassengerAlighted(p).schedule(p.unloadingTime);
					found = true;
					break;
				}
			}

			if(!found)
			{
				new PassengersStartEntering().schedule(0);
			}
		}
	}

	/**
	 * Class that represents when a passenger alighted
	 */
	class PassengerAlighted extends Event {

		/**
		 * Person that alighted
		 */
		Person person;

		/**
		 * Constructor of the class
		 * @param person Person alighted
		 */
		public PassengerAlighted(Person person) {
			this.person = person;
		}
		/**
		 * Actions
		 */
		public void actions() {
			beamBroken = false;
			person.alightingTime = Sim.time();
			person.status = Person.PassengerStatus.FINISHED;
			int floor = getCurrentFloor();
			if (person.destinationFloor == floor){
				Space elevHall =  Building.getInstance().getElevatorHall(floor);
				if (elevHall.canEnter()){
					inElevator.remove(person);
					control.arrivalToDestinationElevHall(elevHall, person);
				}
				else {
					elevHall.inputQueue.add(person);
				}
			}
			else{
				new Error("ERROR: current floor and destination floor do not match.").printStackTrace();
				System.exit(1);
			}
			control.updateUsage(id);
			new PassengersStartAlighting().schedule(0);
		}
	}

	/**
	 * Class that represents the beginning of the entrance of passengers
	 */
	class PassengersStartEntering extends Event {
		/**
		 * Actions
		 */
		public void actions() {
			boolean someoneWantsToEnter = false;
			int currentFloor = getCurrentFloor();
			LinkedListStat<Person> queue = Building.getInstance().getElevatorWaitingLine(currentFloor);
			for(Person p: queue) {
				if(p.status == Person.PassengerStatus.WAITING
						&& !beamBroken
						&& (((p.destinationFloor > currentFloor) && committedDirection == Direction.UP)
								|| ((p.destinationFloor < currentFloor) && committedDirection == Direction.DOWN))
						&& ((double) inElevator.size()/capacity) < p.loadingThreshold
						&& canStopAtFloor[p.destinationFloor])
				{
					beamBroken = true;
					queue.remove(p);
					inElevator.add(p);
					p.boardingTime = Sim.time();
					p.elevator = id;
					//					sim_->scheduleEvent(Event(PASSENGER_ENTERED, e.time() + (iter)->loadingTime, (iter)->id_));
					new PassengerEntered(p).schedule(p.loadingTime);
					someoneWantsToEnter = true;
					break;
				}
			}
			if(!someoneWantsToEnter)
			{
				double dwell = 2/60;
				new ElevCloseDoors().schedule(dwell);
			}
		}
	}
	/**
	 * Class that represents when a passenger enters
	 */
	class PassengerEntered extends Event{
		/**
		 * Person entering
		 */
		Person person;

		/**
		 * Constructor of the class
		 * @param person Person entered
		 */
		public PassengerEntered(Person person) {
			this.person = person;
		}
		
		/**
		 * Actions
		 */
		public void actions() {
			beamBroken = false;
			person.status = Person.PassengerStatus.TRAVELING;
			buttons[person.destinationFloor] = true;
			control.updateUsage(id);
			control.updateFirstFloorElevators();
			new PassengersStartEntering().schedule(0);
		}
	}

	/**
	 * Class that represents the beginning of the doors closing
	 */
	class ElevCloseDoors extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			canGetTarget = false;
			doorStatus = DoorStatus.CLOSING;
			new ElevDoorsClosed().schedule(doorClosingTime + motorStartDelay);
		}
	}

	/**
	 * Class that represents when the doors are closed
	 */
	class ElevDoorsClosed extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			doorStatus = DoorStatus.CLOSED;
			canGetTarget = true;
			control.updateAtDoorsClosed(id);
			new UpdatePassengerReg().schedule(0.01);
		}
	}

	/**
	 * Class that represents the update of the buttons inside the elevator
	 */
	public class UpdatePassengerReg extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			int currentFloor = getCurrentFloor();
			LinkedListStat<Person> queue = Building.getInstance().getElevatorWaitingLine(currentFloor);
			if (queue.size() == 0) {
				Building.getInstance().floors.get(currentFloor).upButton = false;
				Building.getInstance().floors.get(currentFloor).downButton = false;
			}
			for(Person p: queue) {
				if(p.destinationFloor > currentFloor)	
					Building.getInstance().floors.get(currentFloor).upButton = true;
				else
					Building.getInstance().floors.get(currentFloor).downButton = true;
			}
		}
	}

	//-------------------------------------------------------------------------------------------
	//--------------------- Events for times of elevator ----------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Class that represents time T1
	 */
	class ElevT1 extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			journeyTime = t1;
			canGetTarget = true;
			ET2.cancel();
			ET2.schedule(t2 - t1);
		}
	}

	/**
	 * Class that represents time T2
	 */
	class ElevT2 extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			journeyTime = t2;
			if (condition == 'C')
				canGetTarget = false;
			else
				canGetTarget = true;
			ET3.cancel();
			ET3.schedule(t3 - t2);
		}
	}
	
	/**
	 * Class that represents time T3
	 */
	class ElevT3 extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			journeyTime = t3;
			if(condition == 'A')
				canGetTarget = true;
			else
				canGetTarget = false;

			if (condition == 'C') {
				EArrived.cancel();
				EArrived.schedule(tEnd - t3);
			}
			else {
				ET4.cancel();
				ET4.schedule(t4 - t3);
			}
		}
	}

	/**
	 * Class that represents time T4
	 */
	class ElevT4 extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			canGetTarget = false;

			if (condition != 'C') {
				journeyTime = t4;
				ET5.cancel();
				ET5.schedule(t5 - t4);
			}
		}
	}

	/**
	 * Class that represents time T5
	 */
	class ElevT5 extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			canGetTarget = false;

			if(condition == 'A')
			{
				journeyTime = t5;
				ET6.cancel();
				ET6.schedule(t6 - t5);
			}
			else if(condition == 'B')
			{
				journeyTime = t5;
				EArrived.cancel();
				EArrived.schedule(tEnd - t5);
			}
		}
	}

	/**
	 * Class that represents time T6
	 */
	class ElevT6 extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			canGetTarget = false;

			if(condition == 'A')
			{
				journeyTime = t6;
				EArrived.cancel();
				EArrived.schedule(tEnd - t6);
			}
		}

	}

	/**
	 * Class that represents the controller update
	 */
	public class ControllerUpdate extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			currentAllocTime = Sim.time();

			if(elevatorDirection != Direction.NONE)
			{
				journeyTime = Sim.time() - absoluteJourneyTime;
			}
		}
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------- Methods ---------------------------------------------
	//-------------------------------------------------------------------------------------------
	
	/**
	 * Resets some attributes for the next rep
	 */
	public void reset() {
		t1 = 0;
		t2 = 0;
		t3 = 0;
		t4 = 0;
		t5 = 0;
		t6 = 0;
		tEnd = 0;
		currentAllocTime = 0;
		originFloor = homeFloor;
		target = homeFloor;
		ET1 = new ElevT1();
		ET2 = new ElevT2();
		ET3 = new ElevT3();
		ET4 = new ElevT4();
		ET5 = new ElevT5();
		ET6 = new ElevT6();
		EArrived = new ElevArrived();
		inElevator = new LinkedListStat<Person>();
		canGetTarget = true;
		beamBroken = false;
		inJourney = false;
		journeyTime = 0;
		committedDirection = Direction.NONE;
		elevatorDirection = Direction.NONE;
	}

	/**
	 * Sets the target for the elevator
	 * @param floor Floor to stop
	 */
	public void setTarget(int floor) {
		if(canGetTarget && floor == originFloor && elevatorDirection == Direction.NONE)
		{
			target = floor;
			//scheduleEvent(Event(ELEVARRIVED, currentAllocTime, id));
			EArrived.schedule(0);
		}
		else if(canGetTarget && canStop(floor))
		{
			target = floor;
			initJourney(target);

			if(journeyTime == 0)
				absoluteJourneyTime = currentAllocTime;

			if(journeyTime >= 0 && journeyTime < t1)
			{
				ET1.cancel();
				ET1.schedule(t1 - journeyTime);
			}
			else if(journeyTime >= t1 && journeyTime < t2)
			{
				ET2.cancel();
				ET2.schedule(t2 - journeyTime);
			}
			else if(journeyTime >= t2 && journeyTime < t3)
			{
				ET3.cancel();
				ET3.schedule(t3 - journeyTime);
			}
			else if(journeyTime >= t3 && journeyTime < t4)
			{
				ET4.cancel();
				ET4.schedule(t4 - journeyTime);
			}
		}
	}

	/**
	 * Initialize journey
	 * @param destFloor Target floor
	 */
	public void initJourney(int destFloor) {

		journeyDistance = 0;

		if(destFloor > originFloor)
		{
			elevatorDirection = Direction.UP;
			for(int key: Building.getInstance().floors.keySet()) {
				if (key < destFloor && key >= originFloor) {
					journeyDistance += Building.getInstance().floors.get(key).height;
				}
			}
		}	
		else if(destFloor < originFloor)
		{
			elevatorDirection = Direction.DOWN;
			for(int key: Building.getInstance().floors.keySet()) {
				if (key > destFloor && key <= originFloor) {
					journeyDistance += Building.getInstance().floors.get(key).height;
				}
			}
		}
		else
		{
			elevatorDirection = Direction.NONE;
			journeyDistance = 0;
		}

		if(journeyDistance >= ((ratedA * ratedA * ratedV + ratedV * ratedV * ratedJ)/(ratedJ * ratedA)))
		{
			condition = 'A';
			t1 = (ratedA / ratedJ);
			t2 = (ratedV / ratedA);
			t3 = ((ratedA/ratedJ) + (ratedV/ratedA));
			t4 = (journeyDistance/ratedV);
			t5 = ((journeyDistance/ratedV)+(ratedA/ratedJ));
			t6 = ((journeyDistance/ratedV) + (ratedV/ratedA));
			tEnd = ((journeyDistance/ratedV) + (ratedA/ratedJ) + (ratedV/ratedA));
		}
		else if((journeyDistance >= ((2 * ratedA * ratedA * ratedA)/(ratedJ * ratedJ))) && (journeyDistance < ((ratedA * ratedA * ratedV + ratedV * ratedV * ratedJ)/(ratedJ * ratedA))))
		{
			condition = 'B';

			t1 = (ratedA/ratedJ);
			t2 = ((-1 * ratedA)/(2 * ratedJ)) + ((Math.sqrt((ratedA * ratedA * ratedA) + (4 * journeyDistance * ratedJ * ratedJ)))/(2 * ratedJ * Math.sqrt(ratedA)));
			t3 = ((ratedA)/(2 * ratedJ)) + ((Math.sqrt((ratedA * ratedA * ratedA) + (4 * journeyDistance * ratedJ * ratedJ)))/(2 * ratedJ * Math.sqrt(ratedA)));
			t4 = ((3 * ratedA)/(2 * ratedJ)) + ((Math.sqrt((ratedA * ratedA * ratedA) + (4 * journeyDistance * ratedJ * ratedJ)))/(2 * ratedJ * Math.sqrt(ratedA)));
			t5 = ((Math.sqrt((ratedA  *  ratedA  *  ratedA) + (4 * journeyDistance * ratedJ * ratedJ)))/(Math.sqrt(ratedA)  *  ratedJ));
			tEnd = ((ratedA/ratedJ) + ((Math.sqrt((ratedA * ratedA * ratedA) + (4 * journeyDistance * ratedJ * ratedJ)))/(Math.sqrt(ratedA)  *  ratedJ)));
		}
		else if(journeyDistance < ((2 * ratedA * ratedA * ratedA)/(ratedJ * ratedJ)))
		{
			condition = 'C';

			t1 = (Math.pow(journeyDistance/(2 * ratedJ),0.3333));
			t2 = (Math.pow((4 * journeyDistance)/ratedJ,0.3333));
			t3 = (Math.pow((27 * journeyDistance)/(2 * ratedJ),0.3333));
			tEnd = (Math.pow((32 * journeyDistance)/ratedJ,0.3333));
		}
		t1 = t1 /60;
		t2 = t2 /60;
		t3 = t3 /60;
		t4 = t4 /60;
		t5 = t5 /60;
		t6 = t6 /60;
		tEnd = tEnd /60;
		inJourney = true;
	}

	/**
	 * Returns if the elevator can stop at a floor
	 * @param floor Floor to stop
	 * @return TRUE if it can stop, FALSE if not
	 */
	public boolean canStop(int floor) {
		if (canStopAtFloor[floor]) {
			if(floor == originFloor && elevatorDirection == Direction.NONE)
				return true;

			double dtemp = 0;
			double tstop = 0;

			Enumeration<Floor> f = Building.getInstance().floors.elements();
			Floor fl = null;
			if(floor > originFloor)
			{
				while(f.hasMoreElements()) {
					fl = f.nextElement();
					if (fl.floorNumber >= originFloor)
						dtemp += fl.height;
				}
			}	
			else
			{
				while(f.hasMoreElements()) {
					fl = f.nextElement();
					if (fl.floorNumber <= originFloor)
						dtemp += fl.height;
				}
			}

			if(dtemp >= ((ratedA * ratedA * ratedV  + ratedV * ratedV * ratedJ )/(ratedJ * ratedA)))
			{
				tstop = (dtemp/ratedV);
			}
			else if((dtemp >= ((2 * ratedA * ratedA * ratedA )/(ratedJ *ratedJ ))) && (dtemp < ((ratedA * ratedA * ratedV  + ratedV * ratedV *ratedJ )/(ratedJ * ratedA ))))
			{		
				tstop = ((-1 * ratedA )/(2 * ratedJ )) + ((Math.sqrt((ratedA * ratedA * ratedA ) + (4 * dtemp * ratedJ * ratedJ )))/(2 * ratedJ * Math.sqrt(ratedA )));	
			}
			else if(dtemp < ((2 * ratedA * ratedA * ratedA )/(ratedJ * ratedJ )))
			{
				tstop = (Math.pow(dtemp/(2 * ratedJ ), 0.3333));
			}

			if(journeyTime < tstop)
				return true;
			else
				return false;
		}
		else {
			return false;
		}
	}

	/**
	 * Gets current positions of elevator
	 * @return Position of elevator
	 */
	public double getCurrentPosition() {
		if(elevatorDirection == Direction.NONE)
			return lastPos;
		else
		{
			if(condition == 'A')
			{
				if(journeyTime < t1)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad0(ratedJ,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad0(ratedJ,journeyTime);
				}
				else if(journeyTime >= t1 && journeyTime < t2)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad1(ratedA,ratedJ,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad1(ratedA,ratedJ,journeyTime);
				}
				else if(journeyTime >= t2 && journeyTime < t3)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad2(ratedA,ratedJ,ratedV,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad2(ratedA,ratedJ,ratedV,journeyTime);
				}
				else if(journeyTime >= t3 && journeyTime < t4)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad3(ratedA,ratedJ,ratedV,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad3(ratedA,ratedJ,ratedV,journeyTime);
				}
				else if(journeyTime >= t4 && journeyTime < t5)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad4(ratedA,ratedV,ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad4(ratedA,ratedV,ratedJ,journeyDistance,journeyTime);
				}
				else if(journeyTime >= t5 && journeyTime < t6)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad5(ratedA,ratedJ,ratedV,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad5(ratedA,ratedJ,ratedV,journeyDistance,journeyTime);
				}
				else if(journeyTime >= t6 && journeyTime < tEnd)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.ad6(ratedA,ratedJ,ratedV,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.ad6(ratedA,ratedJ,ratedV,journeyDistance,journeyTime);
				}
			}
			else if(condition == 'B')
			{
				if(journeyTime < t1)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.bd0(ratedJ,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.bd0(ratedJ,journeyTime);
				}
				else if(journeyTime >= t1 && journeyTime < t2)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.bd1(ratedA,ratedJ,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.bd1(ratedA,ratedJ,journeyTime);
				}
				else if(journeyTime >= t2 && journeyTime < t3)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.bd2(ratedA,ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.bd2(ratedA,ratedJ,journeyDistance,journeyTime);

				}
				else if(journeyTime >= t3 && journeyTime < t4)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.bd3(ratedA,ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.bd3(ratedA,ratedJ,journeyDistance,journeyTime);
				}
				else if(journeyTime >= t4 && journeyTime < t5)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.bd4(ratedA,ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.bd4(ratedA,ratedJ,journeyDistance,journeyTime);
				}
				else if(journeyTime >= t5 && journeyTime < tEnd)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.bd5(ratedA,ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.bd5(ratedA,ratedJ,journeyDistance,journeyTime);
				}
			}
			else if(condition == 'C')
			{
				if(journeyTime < t1)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.cd0(ratedJ,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.cd0(ratedJ,journeyTime);
				}
				else if(journeyTime >= t1 && journeyTime < t2)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.cd1(ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.cd1(ratedJ,journeyDistance,journeyTime);
				}
				else if(journeyTime >= t2 && journeyTime < t3)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.cd2(ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.cd2(ratedJ,journeyDistance,journeyTime);
				}
				else if(journeyTime >= t3 && journeyTime < tEnd)
				{
					if(elevatorDirection == Direction.UP)
						return Kinematics.cd3(ratedJ,journeyDistance,journeyTime) + lastPos;
					else
						return lastPos - Kinematics.cd3(ratedJ,journeyDistance,journeyTime);
				}
			}
		}
		return -1;
	}
	
	/**
	 * Gets current floor of elevator
	 * @return Current floor
	 */
	public int getCurrentFloor() {
		int i = originFloor;
		if(elevatorDirection == Direction.NONE)
			i = originFloor;
		else if(elevatorDirection == Direction.UP)
		{
			for(int k = originFloor; k < Building.getInstance().floors.size(); k++) {
				try {
					if((Building.getInstance().getFloorLevel(k) > getCurrentPosition()) && !canStop(k))
					{
						i = k;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else if(elevatorDirection == Direction.DOWN)
		{
			for(int k = originFloor; k > -1; k--) {
				try {
					if((Building.getInstance().getFloorLevel(k) < getCurrentPosition()) && !canStop(k))
					{
						i = k;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return i;
	}
	/**
	 * Distance from current floor to param floor. This method takes into account the direction of the elevator to give a logical answer.
	 * @param floorNumber
	 * @param direction
	 * @return -1 if elevator cannot stop at floor, or if person is going the other way of the elevator's direction.
	 * @return number of floors between current floor and param floor.
	 */
	public int getDistanceToCall(int floorNumber, Direction direction) {
		int current = getCurrentFloor();
		if (elevatorDirection == Direction.UP && direction == Direction.UP) {
			if (current <  floorNumber && floorNumber < target) 
				return (floorNumber - current);
			else
				return -1;
		}
		else if (elevatorDirection == Direction.DOWN && direction == Direction.DOWN) {
			if (target < floorNumber && floorNumber < current) 
				return (current - floorNumber);
			else
				return -1;
		}
		else if (elevatorDirection == Direction.NONE){
			return Math.abs(floorNumber - current);
		}
		else {
			return -1;
		}
	}
}
