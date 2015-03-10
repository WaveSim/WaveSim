package interfaces;
import general.*;

public interface CalcModel {

	/**
	 * Simulates next timeStep
	 * @param m Memory object the timestep is executed on
	 * @return simulated time in s
	 */
	float simulateStep(Memory m);
	
	/**
	 * Simulates n steps till time is reached
	 * @param m Memory object the timestep is executed on
	 * @param time how long is the simulated time interval?
	 * @return actually simulated time
	 */
	float simulateStepByTime(Memory m, float time);
	
	// -jw
	void deleteNetUpdates();

}
