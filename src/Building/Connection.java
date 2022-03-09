package Building;


/**
 * This class represents a connection between two spaces
 * @author Jorge Huertas (huertas.ja@uniandes.edu.co)
 *
 */
public class Connection {
	
	//-------------------------------------------------------------------------------------------
	//---------------------------------------- Constants ----------------------------------------
	//-------------------------------------------------------------------------------------------
	/**
	 * Corridor type of connection
	 */
	public final static String CORRIDOR = "Corridor";
	
	/**
	 * Stairs type of connection
	 */
	public final static String STAIRS = "Stairs";

	//-------------------------------------------------------------------------------------------
	//----------------------------------- Public attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Connection id
	 */
	public int id;

	/**
	 * First space connected by the connection
	 */
	public Space space1;

	/**
	 * Second space connected by the connection
	 */
	public Space space2;

	/**
	 * Connection type
	 */
	public String type;
	
	/**
	 * Connection length (meters)
	 */
	public double length;

	/**
	 * Connection width (meters)
	 */
	public double width;
	
	public Path path1;
	
	public Path path2;
	

	//-------------------------------------------------------------------------------------------
	//------------------------------- Constructor of the class ----------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * Constructor of the class
	 * @param id the connection id
	 * @param type the connection type
	 * @param space1 the first space connected by the connection
	 * @param space2 the second space connected by the connection
	 * @param length the connection length
	 * @param width the connection width
	 */
	public Connection(int id, Space space1, Space space2, double length, double width) {
		this.id = id;
		this.space1 = space1;
		this.space2 = space2;
		this.length = length;
		this.width = width;
		this.type = space1.floor.floorNumber == space2.floor.floorNumber ? CORRIDOR : STAIRS;
		space1.connections.add(this);
		space2.connections.add(this);
		
		path1 = new Path(space1, space2, this, "A");
		path2 = new Path(space2, space1, this, "B");
	}
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Methods ----------------------------------------
	//-------------------------------------------------------------------------------------------
	
	public void reset() {
		path1.reset();
		path2.reset();
	}
	
	public Path pathToUse(Space initialS) {
		if (path1.initialSpace == initialS) {
			return path1;
		}
		else if (path2.initialSpace == initialS) {
			return path2;
		}
		else {
			return null;
		}
	}
}
