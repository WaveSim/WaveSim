package general;

import interfaces.Tool;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapHotCold;

public class Editor extends JFrame {

	private static final long serialVersionUID = 1L;
	public static final int H = 1;
	public static final int B = 2;
	public static final int HU = 3;
	public static final int HV = 4;

	private Memory m;
	private Graphics2D g;
	private Image image;
	private DrawArea drawArea;
	private float maxValue;
	private float minValue;
	private JPanel optionPane;
	
	
	private ArrayList<Tool> allTools;
	private Tool currentTool;

	private int mode;

	public Editor(Memory m_i, int mode) {
		super("Editor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// this.setUndecorated(true);

		this.m = m_i;
		this.mode = mode;

		// this.setSize(m.getCol(), m.getRow());
		// this.setLocation(50, 90);
		allTools=new ArrayList<Tool>();
		allTools.add(new Brush(4, 15));
		allTools.add(new RectBrush(4, 15));
		allTools.add(new ResetBrush(this));
//		allTools.add(new OpenTool(this));
		allTools.add(new SaveTool(this));
		
		currentTool = allTools.get(0);

		drawArea = new DrawArea();
		this.setLayout(new BorderLayout());
		this.add(drawArea, BorderLayout.CENTER);
		optionPane = new OptionsPane();
		
		JPanel tmp=new JPanel();
		tmp.add(optionPane);
		this.add(tmp, BorderLayout.EAST);
		drawArea.setPreferredSize(new Dimension(m.getCol(), m.getRow()));
		this.setLocation(50, 100);
		this.pack();
		// pane.setPreferredSize(this.getSize());
		image = new BufferedImage(m.getCol(), m.getRow(), DISPOSE_ON_CLOSE);
		g = (Graphics2D) image.getGraphics();

		AL actionL = new AL();
		drawArea.addMouseListener(actionL);
		drawArea.addMouseMotionListener(actionL);

		maxValue = minValue = getArray()[0][0];

		display();
	}

	/**
	 * draws the wave on the image
	 */
	protected void display() {
		float newMax = Float.MIN_VALUE, newMin = Float.MAX_VALUE;

		
		for (int x = 0; x < m.getCol(); x++) {
			for (int y = 0; y < m.getRow(); y++) {
				if (getValue(x, y) < newMin)
					newMin = getValue(x, y);
				if (getValue(x, y) > newMax)
					newMax = getValue(x, y);
			}
		}

		maxValue = newMax;
		minValue = newMin;

		// float div=maxValue-minValue;
		ColorMapper cm = new ColorMapper(new ColorMapHotCold(), minValue, maxValue);
		
		for (int x = 0; x < m.getCol(); x++)
			for (int y = 0; y < m.getRow(); y++)
			{
				float[] c = cm.getColor(getValue(x, y)).toArray();
				java.awt.Color printC = new java.awt.Color(c[0], c[1], c[2]);

				g.setColor(printC);
				g.drawRect(x, m.getRow()-1-y, 0, 0);
			}

		drawArea.repaint();

	}

	
	private float getValue(int x, int y) {
		switch (mode) {
		case H:
			return m.getH()[x][y] + m.getB()[x][y];
		case B:
			return m.getB()[x][y];
		case HU:
			return m.getHu()[x][y];
		case HV:
			return m.getHv()[x][y];
		}
		return 0;
	}

	@Deprecated
	private float[][] getArray() {
		switch (mode) {
		case H:
			return m.getH();
		case B:
			return m.getB();
		case HU:
			return m.getHu();
		case HV:
			return m.getHv();
		default:
			throw new IllegalArgumentException("Wrong mode: " + mode);
		}
	}

	public void setMemory(Memory m) {
		this.m = m;
		display();
	}

	private class DrawArea extends JPanel {
		private static final long serialVersionUID = 1L;

		public synchronized void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
		}
	}

	private class AL implements MouseListener, MouseMotionListener {
		@Override
		public void mouseClicked(final MouseEvent arg0) {
			Thread t = new Thread() {
				public void run() {
					currentTool
							.performAction(m, mode, arg0.getX(), m.getRow()-arg0.getY());
				}
			};
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			display();

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(final MouseEvent arg0) {
			Thread t = new Thread() {
				public void run() {
					currentTool
							.performAction(m, mode, arg0.getX(), m.getRow()-arg0.getY());
					lastX = arg0.getX();
					lastY = arg0.getY();
					// System.out.println("x: "+arg0.getX()+" y: "+arg0.getY());
					display();
				}
			};
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			display();

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			// lastX=lastY=0;
		}

		int lastX, lastY;

		@Override
		public void mouseDragged(final MouseEvent arg0) {
			Thread t = new Thread() {
				public void run() {
					double dx = arg0.getX() - lastX;
					double dy = arg0.getY() - lastY;
					if (dx > 0 || dy > 0) {
						for (int i = 0; i < Math.max(dx, dy); i++) {
							currentTool.performAction(	m,
														mode,
														(int) (lastX + i * dx / Math.max(dx, dy)),
														m.getRow() - (int) (lastY + i * dy / Math.max(dx, dy)));
						}
					} else {
						for (int i = 0; i < -1 * Math.min(dx, dy); i++) {
							currentTool.performAction(m, mode, (int) (lastX - i
									* dx / Math.min(dx, dy)), m.getRow() - (int) (lastY - i
									* dy / Math.min(dx, dy)));
						}
					}
					lastX = arg0.getX();
					lastY = arg0.getY();
					// System.out.println("x: "+arg0.getX()+" y: "+arg0.getY());
					display();
				}
			};
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			display();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	public static void main(String[] args) {

		Memory m_i = new Memory(500,500);

		for (int x = 0; x < m_i.getCol(); x++) {
			for (int y = 0; y < m_i.getRow(); y++) {
				m_i.getB()[x][y] = -3.0f;
				m_i.getH()[x][y] = 3.0f;
			}
		}

		Editor d = new Editor(m_i, H);
		d.setVisible(true);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		display();
	}

	public Memory getMemory() {
		return m;
	}
	
	

	private class OptionsPane extends JPanel {

		private static final long serialVersionUID = 1L;
		JRadioButton[] modeSelect = new JRadioButton[6];
		JPanel checkboxesPane;
		ButtonGroup groupe = new ButtonGroup();

		JPanel toolPane, toolChooserPane;

		public OptionsPane() {
			super();

			this.setLayout(new BorderLayout());

			checkboxesPane = new JPanel();
			checkboxesPane.setLayout(new GridLayout(2, 2));

			modeSelect[0] = new JRadioButton("H", true);
			modeSelect[1] = new JRadioButton("B", true);
			modeSelect[2] = new JRadioButton("HU", false);
			modeSelect[3] = new JRadioButton("HV", false);

			for (int i = 0; i < modeSelect.length; i++) {
				groupe.add(modeSelect[i]);
			}

			modeSelect[0].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					mode = H;
					display();
				}
			});
			checkboxesPane.add(modeSelect[0]);

			modeSelect[1].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					mode = B;
					display();
				}
			});
			checkboxesPane.add(modeSelect[1]);

			modeSelect[2].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					mode = HV;
					display();
				}
			});
			checkboxesPane.add(modeSelect[2]);

			modeSelect[3].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					mode = HU;
					display();
				}
			});
			checkboxesPane.add(modeSelect[3]);

			this.add(checkboxesPane, BorderLayout.NORTH);

			
			System.out.println(allTools.size()/2);
			toolChooserPane=new JPanel(new GridLayout(3,2));
			for(Tool t:allTools){
				if(t.getIcon()!=null){
					JButton b=new JButton(t.getIcon());
					b.setSize(new Dimension(45, 45));
					
					b.addActionListener(new ActionListener() {
						Tool t;
						public ActionListener setTool(Tool ti){
							t=ti;
							return this;
						}
						
						@Override
						public void actionPerformed(ActionEvent arg0) {
							currentTool=t;
							CardLayout cl = (CardLayout)(toolPane.getLayout());
						    cl.show(toolPane, currentTool+"");
						    toolPane.repaint();
						}
					}.setTool(t));
					toolChooserPane.add(b);
				}
			}
			
			
			
			this.add(toolChooserPane, BorderLayout.CENTER);
			
			
			toolPane=new JPanel(new CardLayout());
			
			for(Tool t:allTools){
				if(t.getOptionPanel()!=null){
					toolPane.add(t.getOptionPanel(), t+"");
				}
			}
			this.add(toolPane, BorderLayout.SOUTH);
			
			CardLayout cl = (CardLayout)(toolPane.getLayout());
		    cl.show(toolPane, currentTool+"");

		}

	}

}
