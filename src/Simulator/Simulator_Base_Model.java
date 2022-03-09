package Simulator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import Building.*;
import Entities.*;
import Entities.Elevator.Direction;
import umontreal.iro.lecuyer.probdist.ExponentialDist;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import umontreal.iro.lecuyer.simevents.Accumulate;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;
import umontreal.iro.lecuyer.stat.Tally;

/**
 * This class represents the simulator
 * @author Daniel Otero
 */
public class Simulator_Base_Model {

	//-------------------------------------------------------------------------------------------
	//-------------------------------------- Constants ------------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constant that represents the time of a single class
	 */
	public final static int CLASS_DURATION = 80;

	/**
	 * Constant that represents the time a person will stay in a node. In this case, it is assumed that every person is immediately transfered to the arc
	 */
	public final static double TIME_AT_SPACE = 0;

	//-------------------------------------------------------------------------------------------
	//------------------------------------ Static Attributes ------------------------------------
	//-------------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * A random stream for the arrivals
	 */
	RandomStream  streamArr  = new MRG32k3a();

	/**
	 * A random stream for the durations
	 */
	RandomStream  streamDuration  = new MRG32k3a();

	/**
	 * Arrival rate of the current period
	 */
	public double currentArrivalRate = 0.0;

	/**
	 * Current period
	 */
	public int currentPeriod = -1;

	/**
	 * Attribute of class Arrival that helps modify the next arrival if this is on another period
	 */
	private Arrival nextArrival;

	/**
	 * Matrix where the values for the amount of people exiting the building at given periods per rep are stored
	 */
	public int[][] numberOut;

	/**
	 * Current rep
	 */
	public int currentRep = -1;

	/**
	 * Attribute to verify the balance at the end of the day in the building (number of people who entered - number of people who exited)
	 */

	public int balance = 0;

	/**
	 * Printwriter to let know the Shiny app the progress of the simulation
	 */
	public PrintWriter progress;

	/**
	 * Entrance to the building (turnstiles)
	 */
	public Space enteringSpace;

	/**
	 * Hashtable containing tally statistics per rep, o-d, period, mode (key = "rep origin destination period mode")
	 */
	Hashtable<String, Tally> timeOfMovement;

	/**
	 * Hashtable containing accumulate statistics (time persistant) of the size of the waiting lines
	 */
	Hashtable<String, Accumulate> waitingLines;

	/**
	 * Hashtable containing accumulate statistics (time persistant) of the amount of people doing different kinds of things in a floor (only in the first)
	 */
	Hashtable<String, Accumulate> firstFloorPeople;

	/**
	 * Hashtable containing the amount of people inside the building.
	 */
	Hashtable<String, Integer> numberInBuilding;

	/**
	 * Controller of the elevators
	 */
	Controller elevControl = new Controller(this);

	/**
	 * Type of the current run. This can be "S", "E" or "M". Set by default as "M"
	 */
	public String typeOfSimulation;

	/**
	 * Path of the directory where the output txt will go
	 */
	public String dir;

	/**
	 * Code of the simulation (a-b-c-d indicates the amount of elevators per type)
	 */
	public String code;

	//-------------------------------------------------------------------------------------------
	//-------------------------------------- Constructor ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constructor of the class.
	 * @param typeOfSimulation Type of the current run
	 */
	public Simulator_Base_Model(String typeOfSimulation) {
		reset();

		//Add exiting paths to spaces.
		Enumeration<Space> e = Building.getInstance().spaces.elements();
		while (e.hasMoreElements()) {
			e.nextElement().updateExitingPaths();
		}

		enteringSpace = Building.getInstance().enteringSpace;
		timeOfMovement = new Hashtable<String, Tally>();
		waitingLines = new Hashtable<String, Accumulate>();
		firstFloorPeople = new Hashtable<String, Accumulate>();
		numberInBuilding = new Hashtable<String, Integer>();
		//Connect elevators 
		Building.getInstance().connectElevatorsToController(elevControl);

		//Set type of simulation {S, E, M}
		this.typeOfSimulation = typeOfSimulation;

		//set code of simulation
		this.code = Building.getInstance().code;

		dir = "./data/output/" + code;
		checkDirectory();
		dir += "/";

		//Create printwirter for Controller
		elevControl.createPrintWriter(dir);
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Methods ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Resets some attributes for the next rep
	 */
	public void reset() {
		currentArrivalRate = 0.0;
		currentPeriod = -1;
		nextArrival = new Arrival();
		balance = 0;

		//reset input queues and other values of the building
		Building.getInstance().reset();
	}

	/**
	 * Simulates one day
	 */
	public void simulateOneDay() {
		currentRep ++;
		Sim.init();
		reset();
		new NextPeriod().schedule(0);
		//755
		new EndOfSim().schedule(755);
		Sim.start();
	}

	/**
	 * Registers the time of movement between two floors of a specific person
	 * @param person Person whose time is to be registered
	 */
	//TODO CHECK revisar cuál periodo de tiempo se registra aquí. En este momento estoy registrando es el periodo de entrada de las personas pero esto no tiene sentido.
	public void addToTimeOfMovement(Person person) {
		String key = currentRep + "\t" + person.originFloor + "\t" + person.destinationFloor + "\t" + person.periodStartMovement + "\t" + person.typeOfMovement;
		double time = Sim.time() - person.timeStartMovement;
		if (timeOfMovement.containsKey(key)) {
			timeOfMovement.get(key).add(time);
		}
		else {
			Tally t = new Tally(key);
			t.add(time);
			timeOfMovement.put(key,t);
		}
	}

	/**
	 * Registers the size of a specific floor's waiting line
	 * @param floorNumber Floor to be registered
	 */
	public void addToWaitingLines(int floorNumber) {

		if(floorNumber != -1) {
			String key = currentRep + "\t" + floorNumber + "\t" + currentPeriod;
			int val = 0;
			val = Building.getInstance().getElevatorWaitingLine(floorNumber).size();
			if (waitingLines.containsKey(key)) {
				waitingLines.get(key).update(val);
			}
			else {
				Accumulate a = new Accumulate(key);
				a.update(val);
				waitingLines.put(key,a);
			}
		}
		else {
			for (int i = 0; i < Building.getInstance().floors.size();i++) {
				String key = currentRep + "\t" + i + "\t" + currentPeriod;
				waitingLines.get(key).setCollecting(false);
			}
		}
	}

	/**
	 * Registers the number of people doing different activities on the first floor
	 * @param type 1 if its end of period, 2 if its elevator waiting line, 3 if its turnstile input queue, 4 if its traveling.
	 */
	public void addToFirstFloorPeople(int type) {

		String key = currentRep + "\t" + 1 + "\t" + currentPeriod + "\t";
		if(type == 1) {
			for (int i = 2; i <= 4; i++)
				firstFloorPeople.get(key + i).setCollecting(false);
		}
		else {
			key += type;
			int val = 0;
			switch(type) {
			case 2:
				val = Building.getInstance().sizeElevFF();
				break;
			case 3:
				val = Building.getInstance().sizeTurnstilesFF();
				break;
			case 4:
				val = Building.getInstance().sizeTravelingFF();
				break;
			}

			if (firstFloorPeople.containsKey(key)) {
				firstFloorPeople.get(key).update(val);
			}
			else {
				Accumulate a = new Accumulate(key);
				a.update(val);
				firstFloorPeople.put(key,a);
			}
		}
	}

	/**
	 * Registers the amount of people in the building
	 */
	public void addToNumberInBuilding() {
		String key = currentRep + "\t" + currentPeriod;
		numberInBuilding.put(key,balance);
	}

	/**
	 * Gives the time until exit for a specific person inside a specific path. 
	 * @param path Path where the person is traveling at the moment
	 * @param person Person whose time is to be calculated
	 * @return time until exit
	 */
	public double timeUntilExit(Path path, Person person) {
		//TODO calculate time until a person exits the path 
		double distance = path.length - person.positionLastVelocityChange;
		double time = distance / person.velocity;
		if (time < 0) {
			if (Math.abs(time) < 0.001) {
				//TODO CHECK person is trying to exit at the same time as another entity.
				time = 0;
			}
			else {
				//ERROR: Negative distances
				new Error("ERROR: " + Sim.time() + " timeUntilExit calculates negative distances for person #" + person.id + ". Time = " + distance / (person.velocity)).printStackTrace();
				System.exit(1);
			}
		}
		return time;
	}

	/**
	 * Reschedule the exit of a specific path for all people traveling through it
	 * @param path Path where the rescheduling will be done
	 */
	public void rescheduleEndOfMovement(Path path) {
		for(Person p: path.traveling) {
			if (p.id == 741639 && path.id.equals("11B")) 
				System.out.println(Sim.time());
			p.updatePositionLastVelocityChange();
			p.timeLastVelocityChange = Sim.time();
			p.updateVelocity(path);
			if (p.connectionDeparture.time() < 0) {
				new Error("ERROR ON PERSON #" + p.id +
						". Path:" + path.id + " [" + Space.TYPE_NAMES[path.initialSpace.type] + 
						" / Floor #" + path.initialSpace.floor.floorNumber + "] - [" + Space.TYPE_NAMES[path.finalSpace.type] + 
						" / Floor #" + path.finalSpace.floor.floorNumber +"] " + 
						". # of people at path = " + path.traveling.size() +
						". Position - Length: " + p.positionLastVelocityChange + " - " + path.length).printStackTrace();
				System.exit(1);
			}
			p.connectionDeparture.reschedule(timeUntilExit(path, p));
			if (p.id == 741639 && path.id.equals("11B")) 
				System.out.println(p.connectionDeparture.time());
		}
	}

	/**
	 * Initializes every PrintWriter
	 */
	public void initializePrintWriters() {
		try {
			//progress
			progress = new PrintWriter(dir + "progress.txt");
			progress.println("INFO");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the directory is already created, if not it creates the directory.
	 */
	public void checkDirectory() {
		File directory = new File(dir);
		if (!directory.exists()){
			directory.mkdir();
			// If you require it to make the entire directory path including parents,
			// use directory.mkdirs(); here instead.
		}
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Events -----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Class that represents the arrival of a person
	 */
	class Arrival extends Event{
		/**
		 * Actions
		 */
		public void actions() {
			balance ++;
			nextArrival.schedule(ExponentialDist.inverseF(currentArrivalRate/5, streamArr.nextDouble()));
			Person entering = new Person(1, 2.5/60, 2.5/60, 0.8, typeOfSimulation);
			entering.duration = Information.getRandomDuration(currentPeriod, streamDuration.nextDouble());
			entering.setRoute();
			entering.destinationFloor = entering.route.get(0);
			entering.timeStartMovement = sim.time();
			new EntryToSpace(enteringSpace, entering).schedule(0);
		}
	}

	/**
	 * Class that represents the entry to a space by a person
	 */
	public class EntryToSpace extends Event{
		/**
		 * Space entered by a person
		 */
		Space space;

		/**
		 * Person who entered the space
		 */
		Person person;

		/**
		 * Constructor of the class
		 * @param pSpace Space to be entered
		 * @param pPerson Person entering
		 */
		public EntryToSpace(Space pSpace, Person pPerson) {
			this.space = pSpace;
			this.person = pPerson;
		}

		/**
		 * Actions
		 */
		public void actions() {
			person.positionLastVelocityChange = 0;
			person.timeLastVelocityChange = 0;
			Path path = null;

			//-------------------------------------
			// Entering a Space of type ENTRANCE
			//-------------------------------------
			if (space.type == Space.ENTRANCE) {
				//If the person is exiting the building
				if (person.destinationFloor == 1 && person.route.size() == 0) {
					double time = Building.getInstance().chooseTurnstileAndGiveTime(person);
					addToFirstFloorPeople(3);
					new Exit(person).schedule(time);
				}
				//If the person just arrived
				else {
					person.periodStartMovement = currentPeriod;
					path = space.pathToFloor();
					if (path.canTravel()) {
						new EntryToPath(path, person).schedule(TIME_AT_SPACE);
					}
					else {
						path.inputQueue.add(person);
					}
				}
			}

			//-------------------------------------
			// Entering a Space of type FLOOR
			//-------------------------------------
			else if (space.type == Space.FLOOR) {
				//If the space is located on entrance floor (Floor #1)
				if (space.floor.floorNumber == 1) {
					//If the person's route has only 1 floor left to go (Needs to be Floor #1)
					if (person.route.size() <= 1) {
						addToTimeOfMovement(person);
						person.destinationFloor = 1;
						path = space.pathToEntrance();
						if (path.canTravel()) {
							new EntryToPath(path, person).schedule(TIME_AT_SPACE);
						}
						else {
							path.inputQueue.add(person);
						}
					}
					else {
						//Set which path to use. Person will use elevator if it was assigned previously and if the queue's length is less than 45 people.
						if (person.types.get(0) == "E" && space.floor.elevatorsHall.inFloor.size() < 45) {
							person.typeOfMovement = Person.TYPE_ELEVATOR;
							path = space.pathToElevatorsHall();
						}
						else {
							person.typeOfMovement = Person.TYPE_STAIRS;
							path = space.pathToStairsHall();
						}
						//Check if entity can travel
						if (path.canTravel()) {
							new EntryToPath(path, person).schedule(TIME_AT_SPACE);
						}
						else {
							path.inputQueue.add(person);
						}

					}
				}
				else {
					if (space.floor.floorNumber == person.destinationFloor) {

						addToTimeOfMovement(person);
						space.inFloor.add(person);
						//TODO set path to travel depending on new type of movement (recalculate)
						if (person.types.get(0) == "S") {
							person.typeOfMovement = Person.TYPE_STAIRS;
							path = space.pathToStairsHall();

						}
						else {
							person.typeOfMovement = Person.TYPE_ELEVATOR;
							path = space.pathToElevatorsHall();
						}
						if (path.canTravel()) {
							double time = person.giveTimeAtFloor(space.floor.floorNumber);
							person.timeStartMovement = Sim.time() + time;
							//TODO CHECK revisar que se calcule bien el periodo en el que empieza el movimiento
							person.periodStartMovement = (int) (currentPeriod + Math.floor(time / 5));
							new EntryToPath(path, person).schedule(time);
						}

						else {
							path.inputQueue.add(person);
						}
					}
					else {
						new Error("ERROR: Person should not be in a floor that does not correspond to destination.").printStackTrace();
						System.exit(1);
					}
				}
				if (person.route.size() == 0) {
					person.destinationFloor = 1;
				}
				else {
					person.route.remove(0);
					person.types.remove(0);
					person.destinationFloor = person.route.size() == 0? 1 : person.route.get(0);
				}
				person.originFloor = space.floor.floorNumber;
			}

			//-------------------------------------
			// Entering a Space of type STAIRS_HALL
			//-------------------------------------
			else if (space.type == Space.STAIRS_HALL) {
				//Case 1: Destination floor is the floor in which the space is contained
				if (person.destinationFloor == space.floor.floorNumber) {
					path = space.pathToFloor();
					if (path == null) {
						//Remove person from the system by doing nothing
						balance --;
						addToTimeOfMovement(person);
					}
					else if (path.canTravel()) {
						new EntryToPath(path, person).schedule(TIME_AT_SPACE);
					}
					else {
						path.inputQueue.add(person);
					}
				}
				//Case 2: Destination floor is not in the floor in which the space is contained, i.e. the person needs to go upstairs or downstairs
				else {
					if ( person.destinationFloor < space.floor.floorNumber) {
						path = space.pathDownStairs();
					}
					else {
						path = space.pathUpStairs();
					}
					if (path.canTravel()) {
						new EntryToPath(path, person).schedule(TIME_AT_SPACE);

					}
					else {
						path.inputQueue.add(person);
					}
				}
			}

			//-------------------------------------
			// Entering a Space of type ELEVATORS_HALL
			//-------------------------------------
			else if (space.type == Space.ELEVATORS_HALL) {
				//TODO program arrival to elevators' hall
				Direction direction = null;

				//Arriving to destination floor
				if (person.destinationFloor == space.floor.floorNumber){
					path = space.pathToFloor();
					if (path == null) {
						//Remove person from the system by doing nothing
						balance --;
						addToTimeOfMovement(person);
					}
					else if (path.canTravel()) {
						new EntryToPath(path, person).schedule(TIME_AT_SPACE);
					}
					else {
						path.inputQueue.add(person);
					}
				}
				//Arriving to waiting line. 
				else {
					space.inFloor.add(person);
					addToWaitingLines(space.floor.floorNumber);
					addToFirstFloorPeople(2);
					person.status = Person.PassengerStatus.WAITING;
					//					boolean pressedBefore = false;

					if (person.destinationFloor > space.floor.floorNumber) {
						//						if (space.floor.upButton)
						//							pressedBefore = true;
						//						else
						space.floor.upButton = true;
						direction = Direction.UP;
					}
					else {
						//						if (space.floor.downButton)
						//							pressedBefore = true;
						//						else
						space.floor.downButton = true;
						direction = Direction.DOWN;

					}
					//					if (!pressedBefore)
					elevControl.updateAtFloor(space.floor.floorNumber, direction);
				}
			}

			//Update FF statistic
			if (space.floor.floorNumber == 1)
				addToFirstFloorPeople(4);
		}
	}

	/**
	 * Class that represents the entrance to a path. When this event is executed it is assumed that the person begins traveling through it.
	 */
	class EntryToPath extends Event {
		/**
		 * Path entered by a person
		 */
		Path path;

		/**
		 * Person who entered the path
		 */
		Person person;

		/**
		 * Constructor of the class
		 * @param path Path to be entered
		 * @param person Person entering
		 */
		public EntryToPath(Path path, Person person) {
			this.path = path;
			this.person = person;
		}

		/**
		 * Actions
		 */
		public void actions() {
			person.lastUsedPath = path;
			path.traveling.add(person);
			//Removes the person from the floor queue if it exists.
			if (path.initialSpace.inFloor.indexOf(person) != -1)
				path.initialSpace.inFloor.remove(person);
			person.positionLastVelocityChange = 0;
			person.timeLastVelocityChange = Sim.time();
			person.velocity = person.calculateVelocity(path);
			EndOfMovement eom = new EndOfMovement(path, person);
			double time = timeUntilExit(path,person);
			person.connectionDeparture = eom;
			eom.schedule(time);
			if (path.type == Path.DOWN || path.type == Path.UP) {
				person.updateAverageTravelingTime(time);
			}

			//Update FF statistic
			if (path.initialSpace.floor.floorNumber == 1 || path.finalSpace.floor.floorNumber == 1)
				addToFirstFloorPeople(4);
		}
	}

	/**
	 * Class that represents end of movement from a person in a path. This means that, when this event is executed, the person has reached the head node.
	 */
	class EndOfMovement extends Event {
		/**
		 * Path to be exited by a person
		 */
		Path path;

		/**
		 * Person exiting the path
		 */
		Person person;

		/**
		 * Space where the person will arrive
		 */
		Space arrivingSpace;

		/**
		 * Constructor of the class
		 * @param path Path to be exited
		 * @param person Person that exits the path
		 */
		public EndOfMovement(Path path, Person person) {
			this.path = path;
			this.person = person;
			arrivingSpace = path.finalSpace;
		}

		/**
		 * Actions
		 */
		public void actions() {
			//TODO CHECK Revisar esto
			path.traveling.remove(person);
			if (arrivingSpace.canEnter()) {
				new EntryToSpace(arrivingSpace, person).schedule(0);
				if(path.inputQueue.size() > 0) {
					Person newPerson = path.inputQueue.get(0);
					new EntryToPath(path, newPerson).schedule(0);
					path.inputQueue.remove(newPerson);
					if (path.initialSpace.inputQueue.size() > 0) {
						newPerson = path.initialSpace.inputQueue.get(0);
						new EntryToSpace(path.initialSpace, newPerson).schedule(0);
						path.initialSpace.inputQueue.remove(newPerson);
						person.lastUsedPath.numPeopleAtOutputQueue --;
					}
				}
				rescheduleEndOfMovement(path);
			}
			else {
				arrivingSpace.inputQueue.add(person);
				path.numPeopleAtOutputQueue ++;
			}
		}
	}

	/**
	 * Class that represents the exit from the building 
	 */
	class Exit extends Event{
		/**
		 * Person exiting the building
		 */
		Person person;

		/**
		 * Constructor of the class
		 * @param person Person exiting
		 */
		public Exit(Person person) {
			this.person = person;
		}

		/**
		 * Actions
		 */
		public void actions() {
			Building.getInstance().removeFromTurnstile(person);
			balance --;
			numberOut[currentPeriod][currentRep]++;
			addToFirstFloorPeople(3);
		}
	}

	/**
	 * Class that represents the event of changing periods 
	 */
	class NextPeriod extends Event{
		/**
		 * Actions
		 */
		public void actions() {

			//Disables collecting of data for the statistics prior to the current period
			if (currentPeriod > 0) {
				addToNumberInBuilding();
				addToWaitingLines(-1);
				addToFirstFloorPeople(1);
				elevControl.updateUsage(-1);
			}


			if (currentPeriod <= 152) {
				currentPeriod++;
				currentArrivalRate = Information.getArrivalRate(currentPeriod);
				if (currentPeriod == 0) {
					nextArrival.schedule(ExponentialDist.inverseF (currentArrivalRate/5, streamArr.nextDouble()));
				}
				else {
					nextArrival.reschedule ((nextArrival.time() - Sim.time()) * Information.getArrivalRate(currentPeriod-1) / Information.getArrivalRate(currentPeriod));
				}
				new NextPeriod().schedule (5);
			}
			else {
				currentArrivalRate = 0.0;
			}

			//Creates statistics for next period. This is so that statistics do not begin at 0 but at the last registered value
			for (int i = 0; i < Building.getInstance().floors.size(); i++)
				addToWaitingLines(i);
			for (int i = 1; i <= Building.getInstance().elevators.size(); i++)
				elevControl.updateUsage(i);
			for (int i = 2; i <= 4; i++)
				addToFirstFloorPeople(i);
		}
	}

	/**
	 * Class that represents the end of the simulation for a day
	 */
	class EndOfSim extends Event{
		public void actions() {
			System.out.println(Sim.time() + " End of simulation. Rep #" + (currentRep + 1) + ". Balance: " + balance);
			progress.println(Sim.time() + " End of simulation. Rep #" + (currentRep + 1) + ". Balance: " + balance);
			Sim.stop();
		}
	}

	//-------------------------------------------------------------------------------------------
	//------------------------------------------ MAIN ----------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Main
	 * @param args
	 */
		public static void main (String[] args) {
			boolean restart = true;
			int reps = 15;
			int reps2;
			Simulator_Base_Model SBM = null;
			Building.getInstance().loadElevators();
			//Set number of reps
			while(restart) {
				//Create new SBM
				SBM = new Simulator_Base_Model("M");
	
				//Create new printwriter to write person's registry
				SBM.initializePrintWriters();
	
				//Create matrix for the number of people exiting the building at a certain period
				SBM.numberOut = new int[152][reps];
	
				//Simulate for one day, for the amount of reps specified above
				for (int i = 0; i < reps; i++) {
					SBM.simulateOneDay();
				}
	
				//Check reps
				reps2 = Statistics.reps(Information.getInstance().out, SBM.numberOut);
	
				if (reps2> reps) {
					reps = reps2;
				} 
				else {
					restart = false;
				}
			}
	
			//Create new printwriter to write statistics
			PrintWriter pw;
			try {
				pw = new PrintWriter(SBM.dir + "tallies " + SBM.typeOfSimulation + ".txt");
				Set<String> keys = SBM.timeOfMovement.keySet();
				pw.println("Rep\tOrigin\tDestination\tPeriod\tMode\tAverage");
				for(String key: keys){
					Tally ta = SBM.timeOfMovement.get(key);
					pw.println(key + "\t" + ta.average());
				}
				pw.close();
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	
			try {
				pw = new PrintWriter(SBM.dir + "waitingLines " + SBM.typeOfSimulation +".txt");
				pw.println("Rep\tFloor\tPeriod\tNumber");
				Set<String> keys = SBM.waitingLines.keySet();
				for(String key: keys){
					Accumulate ac = SBM.waitingLines.get(key);
					pw.println(key + "\t" + (ac.average()));
				}
				pw.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			try {
				pw = new PrintWriter(SBM.dir + "firstFloor " + SBM.typeOfSimulation +".txt");
				pw.println("Rep\tFloor\tPeriod\tType\tNumber");
				Set<String> keys = SBM.firstFloorPeople.keySet();
				for(String key: keys){
					Accumulate ac = SBM.firstFloorPeople.get(key);
					pw.println(key + "\t" + (ac.average()));
				}
				pw.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	
			try {
				pw = new PrintWriter(SBM.dir + "NumberOut " + SBM.typeOfSimulation +".txt");
				pw.println("Period\tRep\tNumber");
				for (int i = 0; i < SBM.numberOut.length;i ++){
					for (int j = 0; j < SBM.numberOut[i].length;j ++){
						pw.println(i + "\t" + j + "\t" + SBM.numberOut[i][j]);
					}
				}
				pw.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	
			try {
				pw = new PrintWriter(SBM.dir + "numberInBuilding " + SBM.typeOfSimulation +".txt");
				pw.println("Rep\tPeriod\tNumber");
				Set<String> keys = SBM.numberInBuilding.keySet();
				for(String key: keys){
					int nib = SBM.numberInBuilding.get(key);
					pw.println(key + "\t" + nib);
				}
				pw.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	
			//Print accumulates of elevators (usage)
			SBM.elevControl.printElevatorUsage();
	
			//Notifies end of simulation
			System.out.println("End " + SBM.typeOfSimulation);
			SBM.progress.println("End " + SBM.typeOfSimulation);
	
			//close printwriter progress
			SBM.progress.close();
		}
	}

//	public static void main (String[] args) {
//		for(int n = 4; n >= 1; n--) {
//			for(int x = 0; x <= 3; x++) {
//				for(int ee = 0; ee <= 3; ee++) {
//					for(int o = 0; o <= 3; o++) {
//						if (n+x+ee+o == 4) {
//							try {
//								PrintWriter pwtemp = new PrintWriter("./data/simulation/Elevators.txt");
//								pwtemp.write("V1" + "\t" + "V2" + "\n");
//								pwtemp.write("N" + "\t" + n + "\n");
//								pwtemp.write("X" + "\t" + x + "\n");
//								pwtemp.write("E" + "\t" + ee + "\n");
//								pwtemp.write("O" + "\t" + o);
//								pwtemp.close();
//							}
//							catch (Exception e) {
//								e.printStackTrace();
//							}
//							int reps = 20;
//							Simulator_Base_Model SBM = null;
//							Elevator.num = 0;
//							Building.getInstance().loadElevators();
//							//Set number of reps
//							//Create new SBM
//							SBM = new Simulator_Base_Model("M");
//							SBM.code = "N-" + n + "_X-" + x + "_E-" + ee + "_O-" + o + "_"; 
//							SBM.dir =  "./data/output/" + SBM.code + "/";
//							SBM.checkDirectory();
//
//							//Create new printwriter to write person's registry
//							SBM.initializePrintWriters();
//
//							//Create matrix for the number of people exiting the building at a certain period
//							SBM.numberOut = new int[152][reps];
//
//							//Simulate for one day, for the amount of reps specified above
//							for (int i = 0; i < reps; i++) {
//								SBM.simulateOneDay();
//								if (SBM.balance > 500) {
//									i--;
//									SBM.currentRep--;
//								}
//							}
//
//							//Create new printwriter to write statistics
//							PrintWriter pw;
//							try {
//								pw = new PrintWriter(SBM.dir + "tallies " + SBM.typeOfSimulation + ".txt");
//								Set<String> keys = SBM.timeOfMovement.keySet();
//								pw.println("Rep\tOrigin\tDestination\tPeriod\tMode\tAverage");
//								for(String key: keys){
//									Tally ta = SBM.timeOfMovement.get(key);
//									pw.println(key + "\t" + ta.average());
//								}
//								pw.close();
//							} 
//							catch (FileNotFoundException e) {
//								e.printStackTrace();
//							}
//
//							try {
//								pw = new PrintWriter(SBM.dir + "waitingLines " + SBM.typeOfSimulation +".txt");
//								pw.println("Rep\tFloor\tPeriod\tNumber");
//								Set<String> keys = SBM.waitingLines.keySet();
//								for(String key: keys){
//									Accumulate ac = SBM.waitingLines.get(key);
//									pw.println(key + "\t" + (ac.average()));
//								}
//								pw.close();
//							}
//							catch (FileNotFoundException e) {
//								e.printStackTrace();
//							}
//
//							try {
//								pw = new PrintWriter(SBM.dir + "firstFloor " + SBM.typeOfSimulation +".txt");
//								pw.println("Rep\tFloor\tPeriod\tType\tNumber");
//								Set<String> keys = SBM.firstFloorPeople.keySet();
//								for(String key: keys){
//									Accumulate ac = SBM.firstFloorPeople.get(key);
//									pw.println(key + "\t" + (ac.average()));
//								}
//								pw.close();
//							}
//							catch (FileNotFoundException e) {
//								e.printStackTrace();
//							}
//
//							try {
//								pw = new PrintWriter(SBM.dir + "NumberOut " + SBM.typeOfSimulation +".txt");
//								pw.println("Period\tRep\tNumber");
//								for (int i = 0; i < SBM.numberOut.length;i ++){
//									for (int j = 0; j < SBM.numberOut[i].length;j ++){
//										pw.println(i + "\t" + j + "\t" + SBM.numberOut[i][j]);
//									}
//								}
//								pw.close();
//							}
//							catch (FileNotFoundException e) {
//								e.printStackTrace();
//							}
//
//							try {
//								pw = new PrintWriter(SBM.dir + "numberInBuilding " + SBM.typeOfSimulation +".txt");
//								pw.println("Rep\tPeriod\tNumber");
//								Set<String> keys = SBM.numberInBuilding.keySet();
//								for(String key: keys){
//									int nib = SBM.numberInBuilding.get(key);
//									pw.println(key + "\t" + nib);
//								}
//								pw.close();
//							}
//							catch (FileNotFoundException e) {
//								e.printStackTrace();
//							}
//
//							//Print accumulates of elevators (usage)
//							SBM.elevControl.printElevatorUsage();
//
//							//Notifies end of simulation
//							System.out.println("End " + SBM.typeOfSimulation + ". " + SBM.dir);
//							SBM.progress.println("End " + SBM.typeOfSimulation);
//
//							//close printwriter progress
//							SBM.progress.close();
//						}
//					}
//				}
//			}
//		}
//	}
//}
