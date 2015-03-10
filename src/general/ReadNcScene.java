package general;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import interfaces.Scenario;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;

public class ReadNcScene implements Scenario {

	private NetcdfFile bathyNc = null;
	private NetcdfFile displNc = null;

	private boolean zoomOut = false; // if reading an WaveSim created file zoomOut 20 times
	
	// distance between two entries
	float dxBath, dyDisp, dyBath, dxDisp;

	// ranges of the bathymetry file, 0: min(x), 1: max(x), 2: min(y), 3: max(y)
	private double[] bathyRange = new double[4];

	// ranges of the displacement file, 0: min(x), 1: max(x), 2: min(y), 3:
	// max(y)
	private double[] displRange = new double[4];

	// arrays that contain bathymetry and displacement data
	private ArrayFloat.D2 bathyArray, displArray;

	public ReadNcScene() throws IOException {
		
		// Dialog: which files should get opened?
		JFileChooser chooser = new JFileChooser();
		
		// which bathymetry file should get opened?
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "Bathymetry-netCDF-Files", "nc");
	    chooser.setFileFilter(filter);
	    chooser.setName("Bathymetrie File");
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	    }
		File bathyFile=chooser.getSelectedFile();
		
		// which displacement file should get opened?
		filter = new FileNameExtensionFilter(
		        "Displacement-netCDF-Files", "nc");
		chooser.setFileFilter(filter);
		chooser.setName("Displacement");
	    returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	    }
		File displFile=chooser.getSelectedFile();
		
		  
		try {
		  bathyNc = NetcdfFile.open(bathyFile.getAbsolutePath(), null);
		  displNc = NetcdfFile.open(displFile.getAbsolutePath(), null);
		} catch (IOException ioe) {
		  System.out.println("Opening of at least one netCDF-file unfortunatly failed.");
		}
		  
		// calc range of bathymetry file
		Variable bathyVarX = bathyNc.findVariable("x");
		Variable bathyVarY = bathyNc.findVariable("y");
		  
//		int dimBathyX = bathyVarX.getDimensions().size();
//		int dimBathyY = bathyVarY.getDimensions().size();
		  
		ArrayDouble.D1 bathyArrayX, bathyArrayY;
		bathyArrayX = (ArrayDouble.D1) bathyVarX.read();
		bathyArrayY = (ArrayDouble.D1) bathyVarY.read();
		  
		int[] shape = bathyArrayX.getShape();
		bathyRange[0] = bathyArrayX.getInt(0);
		bathyRange[1] = bathyArrayX.get(shape[0]-1); 
		  
		shape = bathyArrayY.getShape();
		bathyRange[2] = bathyArrayY.getInt(0);
		bathyRange[3] = bathyArrayY.get(shape[0]-1); 
		  
		dxBath = Math.abs(bathyArrayX.getInt(0) - bathyArrayX.getInt(1));
		dyBath = Math.abs(bathyArrayY.getInt(0) - bathyArrayY.getInt(1));

		// calc range of displacement file
		Variable displVarX = displNc.findVariable("x");
		Variable displVarY = displNc.findVariable("y");
		  
//		int dimDisplX = displVarX.getDimensions().size();
//		int dimDisplY = displVarY.getDimensions().size();
		  
		ArrayDouble.D1 displArrayX, displArrayY;
		displArrayX = (ArrayDouble.D1) displVarX.read();
		displArrayY = (ArrayDouble.D1) displVarY.read();
		
		shape = displArrayX.getShape();
		displRange[0] = displArrayX.getInt(0);
		displRange[1] = displArrayX.get(shape[0]-1); 
		 
		shape = displArrayY.getShape();
		displRange[2] = displArrayY.getInt(0);
		displRange[3] = displArrayY.get(shape[0]-1); 
		 
		dxDisp = Math.abs(displArrayX.getInt(0) - displArrayX.getInt(1));
		dyDisp = Math.abs(displArrayY.getInt(0) - displArrayY.getInt(1));
		  
		// preparing "work"
		List<Attribute> bathyAttr = bathyNc.getGlobalAttributes();
		List<Attribute> displAttr = displNc.getGlobalAttributes();

		System.out.println(bathyAttr);
		
		if ( bathyAttr.contains(new Attribute("Writer", "WaveSim by SWR"))
				&& displAttr.contains(new Attribute("Writer", "WaveSim by SWR")) )
			zoomOut = true;
	
		Variable bathyVarZ = bathyNc.findVariable("z");
		Variable displVarZ = displNc.findVariable("z");
		  
	    bathyArray = (ArrayFloat.D2) bathyVarZ.read();
	    displArray = (ArrayFloat.D2) displVarZ.read();
		  
	}

	public void close() {
		try {
			bathyNc.close();
		} catch (IOException ioe) {
			System.out.println("Closing Bathy.nc failed");
		}
		try {
			displNc.close();
		} catch (IOException ioe) {
			System.out.println("Closing Displ.nc failed");
		}
	}

	/**
	 * Find my nearby(est) existing neighbour
	 * 
	 * @param xy
	 *            , is a x or y coorinate minus the first value (range)
	 * 
	 * @param dxy
	 *            , is the distance between two entries
	 * @return right value where data exists
	 */
	float getNearby(double xy, float dxy) {
		int temp;
		float ret;

		temp = (int) (xy / dxy);
		ret = (float) temp * dxy;

		if (Math.abs(ret - xy) < Math.abs(ret + dxy - xy))
			return ret;

		ret += dxy;
		return ret;
	}

	public float getWaterHeight(float x, float y) {
		float ret;
		ret = getBathymetryBefore(x, y);
		
		if (zoomOut)
			return ret / (-20);
		
		if (ret <= -20)
			return -ret;
		else if (ret < 0)
			return 20;

		return 0;
	}

	public float getBathymetry(float x, float y) {
		float bathy = getBathymetryBefore(x, y) + getDisplacement(x, y);

		if (zoomOut)
			return bathy / 20;
		
		if (bathy > -20 && bathy <= 0)
			bathy = -20;

		if (bathy < 20 && bathy > 0)
			bathy = 20;

		return bathy;
	}

	/**
	 * Get the bathymetry before tsunami
	 * 
	 * @param x
	 *            position relative to the origin of the displacement grid in
	 *            x-direction
	 * @param y
	 *            position relative to the origin of the displacement grid in
	 *            y-direction
	 * @return bathymetry before tsunami w/o displacement
	 */
	float getBathymetryBefore(float x, float y) {
		int[] start = new int[2];

		start[0] = (int) (getNearby(x - bathyRange[0], dxBath) / dxBath);
		start[1] = (int) (getNearby(y - bathyRange[2], dyBath) / dyBath);

		// System.out.println("x" + x + "start" + start[0]);
		return bathyArray.get(start[1], start[0]);
	}

	/**
	 * Get displacement
	 * 
	 * @param x
	 *            position relative to the origin of the displacement grid in
	 *            x-direction
	 * @param y
	 *            position relative to the origin of the displacement grid in
	 *            y-direction
	 * @return displacement in (x|y)
	 */
	float getDisplacement(float x, float y) {
		if (x < displRange[0] | x > displRange[1] | y < displRange[2]
				| y > displRange[3])
			return 0;

		int[] start = new int[2];

		start[0] = (int) (getNearby(x - displRange[0], dxDisp) / dxDisp);
		start[1] = (int) (getNearby(y - displRange[2], dyDisp) / dyDisp);

		return displArray.get(start[1], start[0]);

	}

	public int getBoundaryPos(int i_edge) {
		if (i_edge == 0)
			return (int) bathyRange[0];
		else if (i_edge == 1)
			return (int) bathyRange[1];
		else if (i_edge == 3)
			return (int) bathyRange[2];
		else
			return (int) bathyRange[3];
	}

}
