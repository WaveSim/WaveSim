package general;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import interfaces.Tool;

public class ResetBrush implements Tool {
	
		
	private JPanel pane;
	private JTextArea resetText;
	private JButton resetB;
	
	private Editor e;
	public String ID; 
	

	//float rate;
	//float size;
	public ResetBrush(Editor ei){
		e=ei;
		pane=new JPanel();
		resetText=new JTextArea("Are you sure \nyou want to reset\n the swimming Pool?");
		
		pane.setLayout(new GridLayout(3, 2));
		pane.add(resetText);
		resetB=new JButton("Reset!");
		resetB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				reset();
				e.display();
			}
		});
		pane.add(resetB);
		ID=Math.random()+"";
		//rate=height;
		//size=radius;
		System.out.println("here i am");
	}
	
	
	private void reset() {
		Memory m=e.getMemory();
		for (int h = 1; h < m.getCol(); h++) {
			for (int v = 1; v < m.getRow(); v++) {
			    			      
			    m.getB()[h][v] = -3.0f;
				m.getH()[h][v] = 3.0f;
				m.getHu()[h][v] =0;
				m.getHv()[h][v] =0;
			}
		}

		//boundary
		for (int x = 0; x < m.getCol(); x++) {
			m.getB()[x][0] = 2;
			m.getH()[x][0] = 0;
			m.getB()[x][m.getRow() - 1] = 2;
			m.getH()[x][m.getRow() - 1] = 0;
		}
		for (int i = 0; i < m.getRow(); i++) {
			m.getH()[0][i] = 0;
			m.getB()[0][i] = 2;
			m.getH()[m.getCol() - 1][i] = 0;
			m.getB()[m.getCol() - 1][i] = 2;
		}
		
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
	    try{
	    	//URL imgURL = new URL("http://www.oracle.com/ocom/groups/public/@otn/documents/digitalasset/149240.gif");
	    	URL imgURL=getClass().getResource("/new.gif");
			System.out.println(imgURL);

	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    } 
	    }catch( Exception e){}
	    return null;
	    
	}
	
	
}
