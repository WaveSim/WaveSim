package general;
import interfaces.CalcModel;
import interfaces.Display;
import general.Editor;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class Display2D extends JFrame implements Display{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Memory m;
	private CalcModel cm;
	private Graphics2D g;
	private Image image;
	private DrawArea pane;
	private boolean running;
	int zoome=1;
	
	float maxValue, minValue;
	private int mode;

	
	public Display2D(Memory m_i, CalcModel cm_i) {
		super("WaveSim");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        mode=Editor.H;
		this.m=m_i;
		this.cm=cm_i;
		setSize((m.getCol()-2)*zoome, (m.getRow()-2)*zoome);
		pane= new DrawArea();
        this.setContentPane(pane);
		pane.setPreferredSize(this.getSize());
		pane.addNotify();
		image = new BufferedImage(getSize().width, getSize().height, DISPOSE_ON_CLOSE);
		//System.out.println(image);
		g=(Graphics2D)image.getGraphics(); 
		setLocation(50, 90);

		
		display();
		repaint();
	}
	
		
	/**
	 * draws the wave on the image
	 */
	private void display() {
		float newMax= getArray()[0][0], newMin= getArray()[0][0];
		
		for(int x=1;x<m.getCol()-1;x++){
			for(int y=1;y<m.getRow()-1;y++){
				
				if(getArray()[x][y]<newMin)newMin=getArray()[x][y];
				if(getArray()[x][y]>newMax)newMax=getArray()[x][y];
			}
		}
		maxValue=newMax;
		minValue=newMin;
		
		//System.out.println("Max: "+maxValue+" Min: "+minValue);
		float div=maxValue-minValue;
		for(int x=1;x<m.getCol()-1;x++){
			for(int y=1;y<m.getRow()-1;y++){
				
				int c=50;
				if(div>0.001){
					c=(int)(((getArray()[x][y]-minValue)/div)*255);
					//System.out.println(c);
				}
				
				g.setColor(new Color(c,0,255-c));
				g.fillRect((x-1)*zoome, (y-1)*zoome, zoome, zoome);
				
			}
		}
		
	}

	private float[][] getArray() {
		switch(mode){
			case Editor.H: return m.getH();
			case Editor.B: return m.getB();
			case Editor.HU: return m.getHu();
			case Editor.HV: return m.getHv();
			default: throw new IllegalArgumentException("Wrong mode: "+mode);
		}
	}



	@Override
	public void setMemory(Memory m) {
		this.m=m;
	}

	@Override
	public void setCalcModel(CalcModel cm) {
		this.cm=cm;
	}

	@Override
	public void play() {
		running=true;
		long t=System.currentTimeMillis();
		long t2=System.currentTimeMillis();
		long sec=System.currentTimeMillis();
		
		long calc=0;
		long disp=0;
		int c=0;
		int fps=0;
		long old=500;
		while(running){
			t2=t;
			t=System.currentTimeMillis();
			old=t-t2;
			
			long tmp=System.nanoTime();

			float a=cm.simulateStepByTime(m, old/1000.0f);
			calc+=System.nanoTime()-tmp;
			//if(Math.abs(a-(old/1000.0f))>0.0001)System.out.println("too slow a: "+a+" old/1000: "+old/1000.0f);
			tmp=System.nanoTime();
			display();
			disp+=System.nanoTime()-tmp;
			
			System.out.println("SimTimeStep: "+a+" Calc: "+calc+" Disp: "+disp+ " Calc/Disp: "+(1.0*calc)/disp);
			
			g.setColor(Color.WHITE);
			if(t-sec<500){
				c++;
			}else{
				fps=c;
				c=0;
				sec=System.currentTimeMillis();
			}
			g.drawString("fps: "+fps*2, 15, 15);
			
			
			repaint();
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}

	@Override
	public void pause() {
		running = false;
	}
	
	
	
    private class DrawArea extends JPanel{
        public void paint(Graphics g){
            g.drawImage(image, 0, 0, null);
        }

    }

    
    /*public static void main(String[] args){
    	
    	JColorChooser jc =new JColorChooser();
    	jc.setVisible(true);
    	jc.getColor();
    	
    	Memory m_i=new Memory(500, 700);
    	
    	for(int x=0;x<m_i.getCol();x++){
    		for(int y=0;y<m_i.getRow();y++){
    			m_i.getB()[x][y]= -3.0f;
    			m_i.getH()[x][y]= 3.0f;
    		}
    	}
    	
    	Display2D d=new Display2D(m_i, new JavaCalc());
    	//JColorChooser.showDialog(d, "sad", Color.BLUE);

    	
    	d.setVisible(true);
    	d.play();
    }
*/
    @Override
	public Memory getMemory() {
		return m;
	}

	@Override
	public CalcModel getCm() {
		return cm;
	}
	
}
