package interfaces;

import javax.swing.Icon;
import javax.swing.JPanel;

import general.Memory;

/**
 * 
 * @author felix
 * Interface for the different Tools to edit the sandbox
 */
public interface Tool{
	
	/**
	 * a click at the specified position is performed
	 * @param m Memory object on which the action is performed
	 * @param mode on what data should the action be performed? 
	 * @param x x-Position
	 * @param y y-Position
	 */
	public void performAction(Memory m, int mode, int x, int y); 
	
	
	/**
	 * This panel is used to make changes on the Tool, it should not be too big
	 * @return
	 */
	public JPanel getOptionPanel();
	
	
	/**
	 * 
	 * Returns an Icon, shown on the button to select this Tool  
	 */
	public Icon getIcon();


}
