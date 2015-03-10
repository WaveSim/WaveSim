package general;
import interfaces.Scenario;

public class EmptyScene implements Scenario {
	
	public EmptyScene() {
		
	}
	
	
	public float getWaterHeight(float x, float y)
	{
		return 3.f;
	}

	public float getBathymetry(float x, float y)
	{
		return -3.f;
	}
	
    public int getBoundaryPos(int i_edge)
    {
    	if (i_edge == 0 || i_edge == 3) // left/bottom is -150
    		return -150;
    	
    	// right/top is 150 -> full size: 300x300
    	return 150;
    }
	
	
	
}
