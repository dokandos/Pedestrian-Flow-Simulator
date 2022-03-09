package Building;

import java.util.ArrayList;

public class Floor {

	//-------------------------------------------------------------------------------------------
	//----------------------------------- Public attributes -------------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * The floor id
	 */
	public int floorNumber;
	
	/**
	 * The floor height
	 */
	public double height;
	
	/**
	 * Spaces of the floor
	 */
	public ArrayList<Space> spaces;
	
	/**
	 * The elevators hall of the floor
	 */
	public Space elevatorsHall;
	
	/**
	 * If the up button is pressed
	 */
	public boolean upButton;
	
	/**
	 * If the down button is pressed
	 */
	public boolean downButton;
	
	//-------------------------------------------------------------------------------------------
	//------------------------------- Constructor of the class ----------------------------------
	//-------------------------------------------------------------------------------------------

	/**
	 * COnstructor of the class
	 * @param building building this floor belongs to
	 * @param floorNumber number of the floor
	 * @param height height of the floor
	 */
	public Floor(int floorNumber, double height) {
		this.floorNumber = floorNumber;
		this.height = height;
		this.upButton = false;
		this.downButton = false;
		spaces = new ArrayList<>();
	}
	
	//-------------------------------------------------------------------------------------------
	//------------------------------------------ Methods ----------------------------------------
	//-------------------------------------------------------------------------------------------
	public void reset() {
		upButton = false;
		downButton = false;
	}
}
