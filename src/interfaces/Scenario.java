package interfaces;

public interface Scenario {

	/**
	 * Get initial water height in a scenario to initialize Memory.
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return water height at (x, y)
	 */
	float getWaterHeight(float x, float y);
	
	/**
	 * Get initial bathymetry in a scenario to initialize Memory.
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return bathymetry at (x, y)
	 */
	float getBathymetry(float x, float y);
	
    /** Get the boundary positions
     *
     * @param i_edge which edge (0: left, 1: right, 2: top, 3: bottom)
     * @return value in the corresponding dimension
     */
    int getBoundaryPos(int i_edge);

}
