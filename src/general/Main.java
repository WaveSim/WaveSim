package general;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import interfaces.CalcModel;
import interfaces.Display;
import interfaces.Scenario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;
	private Display disp;
	private Editor edit;
	private Memory m;
	private boolean isRunning = false, isEdit = false;
	private CalcModel cm;
	private Scenario scenario;
	private int maxRes, rangeX, rangeY;
	
	private JButton playB;
	private JButton editB;
	private JButton exit;
	private JButton open;
	
	public Main() {
		super("WaveSim");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// scenario = new RadialDamBreakScene();
		
		scenario = new EmptyScene();
		m=new Memory(300, 300);
		fillM(m, 1, 1, scenario);

		cm=new JavaCalc(m);

		// disp=new Display2D(m, cm);
		
		disp = new Display3D(m, cm);
		// disp.setVisible(true);
		
		this.setLayout(new FlowLayout());

		exit = new JButton("Exit");
		exit.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});

		this.add(exit);

		playB = new JButton("Play");

		playB.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!isEdit) {
						if (!isRunning) {
							isRunning = true;
							new Thread() {
								public void run() {
									disp.play();
								}
							}.start();
							editB.setEnabled(false);
							open.setEnabled(false);
						} else {
							isRunning = false;
							new Thread() {
								public void run() {
									disp.pause();
									// deleteNetUpdates on pause to avoid crash
									// when drawing new bathymetry over waves
									cm.deleteNetUpdates();
								}
							}.start();
							editB.setEnabled(true);
							open.setEnabled(true);
						}
					}
				}
			});

		this.add(playB);
		
		open=new JButton("Open");
		
		open.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					if(!isRunning&&!isEdit)
						openScenario();
					
				}
			});
		
		this.add(open);

		editB = new JButton("Edit");

		editB.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!isRunning) {
						if (isEdit) {
							isEdit = false;
							disp.setMemory(edit.getMemory());
							// disp.setVisible(true);
	
							edit.setVisible(false);
							playB.setEnabled(true);
							open.setEnabled(true);
						} else {
							isEdit = true;
							edit = new Editor(disp.getMemory(), Editor.H);
							edit.setVisible(true);
							// disp.setVisible(false);
							playB.setEnabled(false);
							open.setEnabled(false);
						}
					}
				}
			});
		
		this.add(editB);
		this.setMinimumSize(new Dimension(400, editB.getHeight() + 20));

		disp.setVisible(true);
		this.setBounds(50, 30, 400, 50);
		this.pack();

	}
	
	
	private void openScenario()
	{
		try {
			scenario = new ReadNcScene();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// maximum square root of simulation's resolution
		// maxRes equals maximum range of x or/and y
		maxRes = new Integer(
				 JOptionPane.showInputDialog("What resolution do you prefer?\n "
						+ "Remember the amount of calculations rises quadratic\n"
						+ "(values between 300-600 are recommended on PCs)"));
		
		if (maxRes < 10)
			maxRes=20;
				
		// range from horizontal and vertical scenario
		rangeX = Math.abs(scenario.getBoundaryPos(0)) + Math.abs(scenario.getBoundaryPos(1));
		rangeY = Math.abs(scenario.getBoundaryPos(2)) + Math.abs(scenario.getBoundaryPos(3));
		
		// if scenario is smaller than maxRes, use the origin size
		int maxRange = Math.max(rangeX, rangeY);
		float divMinMax = (float) Math.min(maxRes, Math.min(rangeX, rangeY)) / (float) maxRange; 
		
		if (maxRange <= maxRes)
			m = new Memory(rangeX+2, rangeY+2);
		else
			m = new Memory((int) (rangeX*divMinMax), (int) (rangeY*divMinMax));
		
		cm = new JavaCalc(m);

		// compute the size of a single cell
		float dX = ((float) rangeX) / m.getCol();
		float dY = ((float) rangeY) / m.getRow();
		fillM(m, dX, dY, scenario);
		
//		if(disp instanceof Display3D){
//			((Display3D)disp).setVisible(false);
//			((Display3D)disp).dispose();
//			
//			disp=new Display3D(m, cm);
//			disp.setVisible(true);
//			System.gc();
//		}
	}

	
	public static void fillM(Memory m, float dx, float dy, Scenario scenario) 
	{
		for (int h = 1; h < m.getCol(); h++)
			for (int v = 1; v < m.getRow(); v++)
			{
			    float x = scenario.getBoundaryPos(0) + (h-0.5f)*dx;
			    float y = scenario.getBoundaryPos(3) + (v-0.5f)*dy;
			    
			    m.getB()[h][v] = scenario.getBathymetry(x, y);
				m.getH()[h][v] = scenario.getWaterHeight(x, y);
			}
	

		for (int x = 0; x < m.getCol(); x++)
		{
			m.getB()[x][0] = 2;
			m.getH()[x][0] = 0;
			m.getB()[x][m.getRow() - 1] = 2;
			m.getH()[x][m.getRow() - 1] = 0;
		}
		
		for (int y = 0; y < m.getRow(); y++)
		{
			m.getH()[0][y] = 0;
			m.getB()[0][y] = 2;
			m.getH()[m.getCol() - 1][y] = 0;
			m.getB()[m.getCol() - 1][y] = 2;
		}
		

	}

	public static void main(String[] args) {
		Main m = new Main();
		m.setVisible(true);
	}

}
