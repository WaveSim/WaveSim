package interfaces;
import general.*;

public interface Display {

	public void setMemory(Memory m);
	
	public void setCalcModel(CalcModel cm);
	
	public void play();
	
	public void pause();

	public Memory getMemory();
	
	public CalcModel getCm();

	public void setVisible(boolean b);
}
