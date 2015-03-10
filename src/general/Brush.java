package general;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import interfaces.Tool;

public class Brush implements Tool {
	
	private static final String RADIUS="radius";
	private static final String HEIGHT="height";
	private static final double MAX_H=500.0, MIN_H=-500.0, STEP_H=0.2, START_VALUE_H=0;
	private static final int MAX_R=500, MIN_R=1, STEP_R=5, START_VALUE_R=10;

	
	private JPanel pane;
	private JLabel radiusText;
	private JLabel heightText;
	private JSpinner radiusSpinner;
	private JSpinner heightSpinner;
	
	public String ID; 
	

	//float rate;
	//float size;
	public Brush(float height, float radius){
		pane=new JPanel();
		
		radiusText=new JLabel(RADIUS);
		heightText=new JLabel(HEIGHT);
		
		heightSpinner=new JSpinner(new SpinnerNumberModel(height, MIN_H, MAX_H, STEP_H));
		radiusSpinner=new JSpinner(new SpinnerNumberModel((int)radius, MIN_R, MAX_R, STEP_R));
		
		pane.setLayout(new GridLayout(3, 2));
		pane.add(radiusText);
		pane.add(radiusSpinner);
		pane.add(heightText);
		pane.add(heightSpinner);
		ID=Math.random()+"";
		//rate=height;
		//size=radius;
	}
	
	
	@Override
	public void performAction(Memory m, int mode, int x, int y) {
		// TODO Auto-generated method stub
		
		int size= new Integer(radiusSpinner.getValue()+"");
		float rate=new Float(heightSpinner.getValue()+"");
		int top, bottom, left, right;
		top=(int)(y-size);
		if(top<=1)
			top=1;
		bottom =(int)(y+size);
		if(bottom>=m.getRow()-1)
			bottom=m.getRow()-1;
		left=x-size;
		if(left<1)left=1;
		right=x+size;
		if(right>m.getCol()-1)
			right=m.getCol()-1;
		
		
//		System.out.println("x: "+x+" y: "+y+"left: "+left+" right: "+right+" top: "+ top+" bottom: "+bottom);
		
		for(int xx=left;xx<right;xx++){
			for(int yy=top;yy<bottom;yy++){
				if((xx-x)*(xx-x)+(yy-y)*(yy-y)<size*size){
					switch (mode){
						case Editor.H:
							if(m.getB()[xx][yy]<0){
								m.getH()[xx][yy]=(-m.getB()[xx][yy])+rate;
							}
							else m.getH()[xx][yy]=0;
							break;
						case Editor.B:
							m.getB()[xx][yy]=rate;
							if (rate >= 0) {
								m.getH()[xx][yy] = 0.f;
							}
							break;
						case Editor.HU:
							m.getHu()[xx][yy]=rate;
							break;
						case Editor.HV:
							m.getHv()[xx][yy]=rate;
							break;
					}
				}
			}
		}
	}
	
	
	public void setH(float height){
//		rate=height;
		heightSpinner.setValue(height);
	}


	public void setR(float r) {
		radiusSpinner.setValue(r);
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
		// TODO Auto-generated method stub
		BufferedImage i=new BufferedImage(40, 40, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=i.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 40, 40);
		g.setColor(Color.BLACK);
		g.fillOval(10, 10, 20, 20);

		return new ImageIcon(i);
	}
	
	
}
