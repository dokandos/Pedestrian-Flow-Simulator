package Building;

import Entities.Person;
import umontreal.iro.lecuyer.simevents.LinkedListStat;

public class Path {
	
	public final static String UP = "up";
	
	public final static String DOWN = "down";
	
	public final static String FLAT = "flat";
	
	public String id;

	public Space initialSpace;
	
	public Space finalSpace;
	
	public LinkedListStat<Person> inputQueue;
	
	public LinkedListStat<Person> traveling;
	
	public double length;
	
	public double width;
	
	public Connection connection;
	
	public String type;
	
	public int numPeopleAtOutputQueue;
	
	public Path(Space initialSpace, Space finalSpace, Connection connection, String id) {
		this.initialSpace = initialSpace;
		this.finalSpace = finalSpace;
		this.connection = connection;
		this.id = connection.id + id;
		length = connection.length;
		width = connection.width / 2;
		inputQueue = new LinkedListStat<Person>();
		traveling = new LinkedListStat<Person>();
		numPeopleAtOutputQueue = 0;
		
		if(initialSpace.floor.floorNumber < finalSpace.floor.floorNumber)
			type = UP;
		else if(initialSpace.floor.floorNumber > finalSpace.floor.floorNumber)
			type = DOWN;
		else
			type = FLAT;
	}
	
	public void reset() {
		inputQueue.clear();
		traveling.clear();
		numPeopleAtOutputQueue = 0;
	}
	
	public boolean canTravel() {
		return connection.path1.traveling.size() + connection.path1.numPeopleAtOutputQueue + connection.path2.traveling.size() + connection.path2.numPeopleAtOutputQueue <=  Person.SPACE_OCCUPIED * width * length;
	}
}
