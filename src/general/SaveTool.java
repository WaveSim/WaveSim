package general;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index2D;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import interfaces.Tool;

import java.util.ArrayList;
import java.util.List;

public class SaveTool implements Tool {
	
		
	private JPanel pane;
	private JTextArea saveText;
	private JButton saveB;
	
	private Editor e;
	public String ID; 
	

	//float rate;
	//float size;
	public SaveTool(Editor ei){
		e=ei;
		pane=new JPanel();
		saveText=new JTextArea("Do you want to save \nyour awesome new \nszenario?");
		
		pane.setLayout(new GridLayout(3, 2));
		pane.add(saveText);
		saveB=new JButton("Save");
		saveB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					save();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InvalidRangeException e1) {
					e1.printStackTrace();
				}
				e.display();
			}
		});
		pane.add(saveB);
		ID=Math.random()+"";

	}
	
	
	private void save() throws IOException, InvalidRangeException {
		Memory m=e.getMemory();
		
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "netCDF-File", "nc");
	    chooser.setFileFilter(filter);
	    chooser.setName("Save Szenario");
	    int returnVal = chooser.showSaveDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("Saving scenario to this pref"
	       		+ "ix: " +
	            chooser.getSelectedFile().getName());
	    }
		File saveFile=chooser.getSelectedFile();
		
		writeNc(m, saveFile, 'd');
		writeNc(m, saveFile, 'w');
		
//		//boundary
//		for (int x = 0; x < m.getCol(); x++) {
//			m.getB()[x][0] = 2;
//			m.getH()[x][0] = 0;
//			m.getB()[x][m.getRow() - 1] = 2;
//			m.getH()[x][m.getRow() - 1] = 0;
//		}
//		for (int i = 0; i < m.getRow(); i++) {
//			m.getH()[0][i] = 0;
//			m.getB()[0][i] = 2;
//			m.getH()[m.getCol() - 1][i] = 0;
//			m.getB()[m.getCol() - 1][i] = 2;
//		}
		
	}

	
	private void writeNc(Memory m, File saveFile, char mode) throws IOException, InvalidRangeException
	{
		NetcdfFileWriter dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, saveFile.getPath()+"."+mode+".nc");
		float[][] memTemp = m.getMem(mode);
		Dimension dimX = dataFile.addDimension(null, "x", memTemp.length);
		Dimension dimY = dataFile.addDimension(null, "y", memTemp[0].length);
		
		assert(memTemp.length == dimX.getLength());
		assert(memTemp[0].length == dimY.getLength());
		
		dataFile.addGroupAttribute(null, new Attribute("Conventions", "COARDS/CF-1.0"));
		dataFile.addGroupAttribute(null, new Attribute("Writer", "WaveSim by SWR"));
		
		List<Dimension> dims = new ArrayList<Dimension>();
		dims.add(dimX);
		dims.add(dimY);
		
		Variable varX = dataFile.addVariable(null, "x", DataType.DOUBLE, "x");
		Variable varY = dataFile.addVariable(null, "y", DataType.DOUBLE, "y");
		
		Variable varZ = dataFile.addVariable(null, "z", DataType.FLOAT, dims);
		
		dataFile.create();
		
		System.out.println("dimX length" + dimX.getLength());
		Array dataX = Array.factory(DataType.DOUBLE, new int[]{dimX.getLength()});
		Array dataY = Array.factory(DataType.DOUBLE, new int[]{dimY.getLength()});
		
		for(int i=0; i < dimX.getLength(); i++)
			dataX.setDouble(i, (double) i);

		for(int j=0; j < dimY.getLength(); j++)
			dataY.setDouble(j, (double) j);
		
		dataFile.write(varX, dataX);
		dataFile.write(varY, dataY);
		
		int[] iDim = new int[]{dimX.getLength(), dimY.getLength()};
		Array dataZ = Array.factory(DataType.FLOAT, iDim);
		Index2D idx = new Index2D(iDim);
		
		System.out.println("dimX.getLength " + dimX.getLength() + "dimY.getLength "+ dimY.getLength());
		
		for (int i = 0; i < dimX.getLength(); i++)
			for (int j=0; j < dimY.getLength(); j++)
			{
				idx.set(i, j);
				dataZ.setFloat(idx, memTemp[j][i]);
//				if (m.getMem(mode)[i][j] == 0)
//					System.out.println("getMem mode " + mode + "i "+ i + "j " + j);
			}
		
		dataFile.write(varZ, dataZ);
		dataFile.flush();
		dataFile.close();
	}

	@Override
	public void performAction(Memory m, int mode, int x, int y) {
		//does nothing
	}
	
	
	@Override
	public JPanel getOptionPanel() {
		return pane;
	}

	
	public String toString(){
		return super.toString()+" "+ID;
	}

	@Override
	public Icon getIcon() {
	    URL imgURL=getClass().getResource("/save.png");
	    
	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    }

    	BufferedImage i=new BufferedImage(40, 40, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=i.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 40, 40);
		g.setColor(Color.BLACK);
		g.drawString("S", 10, 10);
	    return null;
	}
}
