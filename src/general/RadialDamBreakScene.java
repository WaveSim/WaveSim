package general;
import interfaces.Scenario;

public class RadialDamBreakScene implements Scenario {
	
	public RadialDamBreakScene() {
		
	}
	
	
	public float getWaterHeight(float x, float y)
	{
		return getBathymetry(x, y) + 6.0f;

	}

	public float getBathymetry(float x, float y)
	{
		return ( Math.sqrt( x*x + y*y ) < 40.f ) ? -3.f + 1.f: -3.f + 0.0f ;
	}
	
    public int getBoundaryPos(int i_edge)
    {
    	if (i_edge == 0 || i_edge == 3) // left/bottom is -200
    		return -200;
    	
    	// right/top is 200 -> full size: 400x400
    	return 200;
    }
	
	
	
}
