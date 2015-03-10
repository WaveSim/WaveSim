package general;

import interfaces.CalcModel;
import interfaces.Scenario;

public class JavaCalc implements CalcModel {

	private static final int WetWet = 5;
	private static final int WetDryWall = 6;
	private static final int DryWetWall = 7;
	private static final int DryDry = 8;
	
	public static final float g=9.81f; 
	public static final float zeroTol=0.0000001f;
	
	float maxWaveSpeed=0;
	float maxTimeStep=0;
	
	private float[][] m_hNetUpdatesLeft; 
	private float[][] m_hNetUpdatesRight;
	private float[][] m_hNetUpdatesUp;
	private float[][] m_hNetUpdatesDown;
	private float[][] m_huNetUpdatesLeft; 
	private float[][] m_huNetUpdatesRight;
	private float[][] m_hvNetUpdatesUp;
	private float[][] m_hvNetUpdatesDown;
	
	private final float[] innerMaxWaveSpeed;//=new float[80];//Runtime.getRuntime().availableProcessors()];

	private final Memory m;
	private Thread[] tVert;//=new ThreadWithID[ 80];//Runtime.getRuntime().availableProcessors()];
	private Thread[] tHori;//=new ThreadWithID[ 80];//Runtime.getRuntime().availableProcessors()];

	long tUp=0;
	long tCalc=0;
	
	public JavaCalc(Memory me){
		this(me, Runtime.getRuntime().availableProcessors());
	}
	
	public JavaCalc(Memory me, int threads){
		this.m=me;
		innerMaxWaveSpeed=new float[threads];
		tHori=new ThreadWithID[ threads];
		tVert=new ThreadWithID[ threads];
		
		m_hNetUpdatesLeft= new float[m.getCol()][m.getRow()]; 
		m_hNetUpdatesRight=new float[m.getCol()][m.getRow()];
		m_hNetUpdatesUp=new float[m.getCol()][m.getRow()];
		m_hNetUpdatesDown=new float[m.getCol()][m.getRow()];
		m_huNetUpdatesLeft=new float[m.getCol()][m.getRow()]; 
		m_huNetUpdatesRight=new float[m.getCol()][m.getRow()];
		m_hvNetUpdatesUp=new float[m.getCol()][m.getRow()];
		m_hvNetUpdatesDown=new float[m.getCol()][m.getRow()];
	}
	
	// deleteNetUpdates; called when 'pause' to avoid crash after restarting with new bathymetry
	public void deleteNetUpdates() {
		m_hNetUpdatesLeft= new float[m.getCol()][m.getRow()]; 
		m_hNetUpdatesRight=new float[m.getCol()][m.getRow()];
		m_hNetUpdatesUp=new float[m.getCol()][m.getRow()];
		m_hNetUpdatesDown=new float[m.getCol()][m.getRow()];
		m_huNetUpdatesLeft=new float[m.getCol()][m.getRow()]; 
		m_huNetUpdatesRight=new float[m.getCol()][m.getRow()];
		m_hvNetUpdatesUp=new float[m.getCol()][m.getRow()];
		m_hvNetUpdatesDown=new float[m.getCol()][m.getRow()];		
	}
	
	@Override
	public float simulateStep(Memory m) {
		return simulateStepByTime(m, 0);
	}

	@Override
	public float simulateStepByTime(final Memory m, float time) {		

		//left to right
		//horizontal
		long t1=System.nanoTime();

		for(int i=0;i< tHori.length;i++){
			
			tHori[i]=new ThreadWithID(i, tVert.length){
				public void run(){
					for(int i=(ID%maxProcessors)+1;i<m.getCol();i+=maxProcessors){
						for(int j=1;j<m.getRow();j++){	
							
							float waveSpeed=computeNetUpdates(i-1, j, i, j, m.getH(), m.getHu(), m.getB(),
							m_hNetUpdatesLeft, m_hNetUpdatesRight, m_huNetUpdatesLeft, m_huNetUpdatesRight);
	
						innerMaxWaveSpeed[ID] = waveSpeed>innerMaxWaveSpeed[ID] ? waveSpeed : innerMaxWaveSpeed[ID];
						}
				}
			}
			};
			tHori[i].start();
		}
		for(int i=0;i<tHori.length;i++){
			try {
				tHori[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			maxWaveSpeed = innerMaxWaveSpeed[i]>maxWaveSpeed ? innerMaxWaveSpeed[i] : maxWaveSpeed;
		}
		tCalc+=System.nanoTime()-t1;
		

		if(maxWaveSpeed<zeroTol) //probably possible if there is no water
			maxTimeStep=m.getDX(); 
		else
			maxTimeStep=0.45f*m.getDX()/maxWaveSpeed;
		if(time>zeroTol&&maxTimeStep>time)
			maxTimeStep=time;


		
	

		updateUnknownsHorizontal(m.getDX(), maxTimeStep);

		//up, down
		//vertical

		t1=System.nanoTime();

		for(int i=0;i< tVert.length;i++){
			tVert[i]=new ThreadWithID(i, tVert.length){
				public void run(){
					for(int i=ID%maxProcessors;i<m.getCol();i+=maxProcessors){
						for(int j=1;j<m.getRow();j++){					 
							computeNetUpdates(i, j-1, i, j, m.getH(), m.getHv(), m.getB(),
									m_hNetUpdatesUp, m_hNetUpdatesDown, m_hvNetUpdatesUp, m_hvNetUpdatesDown);
						}
					}
				}
			};
			tVert[i].start();
		}
		
		for(Thread th:tVert){
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		tCalc+=System.nanoTime()-t1;

		updateUnknownsVertical(m.getDY(), maxTimeStep);
		return maxTimeStep;
	}

	
	void updateUnknownsHorizontal(float dx, float dt) {

		long t1=System.nanoTime();

		for(int j=1;j<m.getCol();j++){
			for(int i=1;i<m.getRow();i++){
				m.getH()[j][i] -= dt/dx * (m_hNetUpdatesLeft[j][i]+ m_hNetUpdatesRight[j][i]);
				m.getHu()[j][i] -= dt/dx * (m_huNetUpdatesLeft[j][i] + m_huNetUpdatesRight[j][i]);
			}
		}
		tUp+=System.nanoTime()-t1;


	}
	
	void updateUnknownsVertical(float dy, float dt) {
		long t1=System.nanoTime();
		for(int j=1;j<m.getCol();j++){
			for(int i=1;i<m.getRow();i++){
				m.getH()[j][i] -= dt/dy * (m_hNetUpdatesUp[j][i]+m_hNetUpdatesDown[j][i]); //like wavePropagation.cpp in SWE1D
				m.getHv()[j][i] -= dt/dy * (m_hvNetUpdatesUp[j][i]+m_hvNetUpdatesDown[j][i]); //like SWE1D
			}
		}
		tUp+=System.nanoTime()-t1;
		
	}

	
	private float computeNetUpdates(int col1, int row1, int col2, int row2, float[][] h, float[][] hu,
			float[][] b, float[][] hUpdateLeft,
			float[][] hUpdateRight, float[][] huUpdateLeft,
			float[][] huUpdateRight) {
		
		int wetstate=computeWetDryState(col1, row1, col2, row2, h);
		
		switch(wetstate){
			case WetWet:
				return computeWetWet(col1, row1, col2, row2, h[col1][row1], h[col2][row2], hu[col1][row1], hu[col2][row2],
						b[col1][row1], b[col2][row2], hUpdateLeft, hUpdateRight, huUpdateLeft, huUpdateRight);
			case DryDry:
				return 0;
			case WetDryWall:
				float ret= computeWetWet(col1, row1, col2, row2, h[col1][row1], h[col1][row1], hu[col1][row1], -hu[col1][row1],
						b[col1][row1], b[col1][row1], hUpdateLeft, hUpdateRight, huUpdateLeft, huUpdateRight);
				hUpdateRight[col2][row2]=0;
				huUpdateRight[col2][row2]=0; //don't change dry
				return ret;
			case DryWetWall:

				float ret2 = computeWetWet(col1, row1, col2, row2, h[col2][row2], h[col2][row2], -hu[col2][row2], hu[col2][row2],
						b[col2][row2], b[col2][row2], hUpdateLeft, hUpdateRight, huUpdateLeft, huUpdateRight);
				hUpdateLeft[col1][row1]=0;
				huUpdateLeft[col1][row1]=0; //don't change dry
				return ret2;
		}
		
		
		
		return 0;
	}
	
	
	private int computeWetDryState(int col1, int row1, int col2, int row2, float[][] h){
		if(h[col1][row1]>0)
			return h[col2][row2]>0?WetWet : WetDryWall;
		else
			return h[col2][row2]>0?DryWetWall : DryDry;
	}
	
	
	private float computeWetWet(int col1, int row1, int col2, int row2, float i_hLeft, float i_hRight, 
			float i_huLeft, float i_huRight, float i_bLeft, float i_bRight, 
			float[][] hUpdateLeft, float[][] hUpdateRight, float[][]huUpdateLeft, float[][]huUpdateRight){
		
		float[]ev=getEigenvalues(i_hLeft, i_hRight, i_huLeft, i_huRight);
		float ev_1 = ev[0];
		float ev_2 = ev[1];



		// equation (2) and (3) in assignment 2
		float oneDivEwDif = 1 / (ev_2 - ev_1);
		float huDif = i_huRight - i_huLeft;
		float modFluxxDif = i_huRight * (i_huRight / i_hRight) + 0.5f * g * i_hRight * i_hRight
						 - (i_huLeft * (i_huLeft / i_hLeft) + 0.5f * g * i_hLeft * i_hLeft)
						 + g * (i_bRight-i_bLeft) * (i_hLeft + i_hRight) *0.5f;

		// alpha calculation
		float a_1 = (ev_2 * huDif - modFluxxDif)  * oneDivEwDif;
		float a_2 = (-ev_1 * huDif + modFluxxDif) * oneDivEwDif;

		// net-updates
		
		hUpdateLeft[col1][row1] =0;
		huUpdateLeft[col1][row1] =0;
		hUpdateRight[col2][row2] =0;
		huUpdateRight[col2][row2] =0;
		if (ev_1 < 0) {
			hUpdateLeft[col1][row1] += a_1;
			huUpdateLeft[col1][row1] += a_1 * ev_1;
		}
		if (ev_2 < 0) {
			hUpdateLeft[col1][row1] += a_2;
			huUpdateLeft[col1][row1] += a_2 * ev_2;
		}
		if (ev_1 > 0) {
			hUpdateRight[col2][row2] += a_1;
			huUpdateRight[col2][row2] += a_1 * ev_1;
		}
		if (ev_2 > 0) {
			hUpdateRight[col2][row2] += a_2;
			huUpdateRight[col2][row2] += a_2 * ev_2;
		}

		
		//o_maxWaveSpeed = 0;
		return Math.max(Math.abs(ev_1), Math.abs(ev_2));
	}

	private float[] getEigenvalues(float i_hLeft, float i_hRight,
		float i_huLeft, float i_huRight) {

		// Height h^(Roe)
		float h_roe = 0.5f * (i_hLeft + i_hRight);

		// Partikel-Geschwindigkeit u^(Roe)
		float h_rs = (float) Math.sqrt(i_hRight);
		float h_ls = (float) Math.sqrt(i_hLeft);
		float u_l = i_huLeft / i_hLeft;
		float u_r = i_huRight / i_hRight;

		// Assert div 0?
		float u_roe = ((u_l * h_ls) + (u_r * h_rs)) / (h_ls + h_rs);

		// Eigenvalues
		float sq=(float)Math.sqrt(g * h_roe); //sqrt is only needed once to be computet
		float[] ret={u_roe - sq, u_roe + sq};
		return ret;
			
	}


	@SuppressWarnings("unused")
	private static void printM(Memory m) {
		for(int x=0;x<m.getCol();x++)
		{
    		for(int y=0;y<m.getRow();y++)
    			System.out.print(m.getH()[x][y]+" ");
    		
    		System.out.println();
    	}
	}
	
	private class ThreadWithID extends Thread{
		final int ID;
		final int maxProcessors;
		ThreadWithID(int p, int max){
			super();
			ID=p;
			maxProcessors=max;
		}

	}
	
	/**
	 * Method to test how many threads used for the computeNetUpdates-Loops are the fastest
	 * May take some time until first output is shown, but it gets faster ;)
	 * 100 Threads is the highest tested
	 * @param args none
	 */
	public static void main(String[] args){
		for(int j=1;j<10;j++){
			Scenario s = new RadialDamBreakScene();
			Memory m= new Memory(2000, 2000);
			Main.fillM(m, 1, 1, s);
			JavaCalc jc=new JavaCalc(m, j);
			long tUpSum=0;
			long tCalcSum=0;
			jc.simulateStep(m);
			jc.tUp=0;
			jc.tCalc=0;
			for(int i=0;i<20;i++){
				jc.simulateStep(m);
				tUpSum+=jc.tUp;
				tCalcSum+=jc.tCalc;
				//System.out.println(i+": tUp: "+jc.tUp+ " tCalc: "+jc.tCalc+ " tUpSum/tCalcSum: "+(1.0*tUpSum)/tCalcSum);
				jc.tUp=0;
				jc.tCalc=0;
			}
			System.out.println(j+"\t"+(tUpSum+tCalcSum)/1000000);
		}
		for(int j=10;j<=100;j+=10){
			Scenario s = new RadialDamBreakScene();
			Memory m= new Memory(2000, 2000);
			Main.fillM(m, 1, 1, s);
			JavaCalc jc=new JavaCalc(m, j);
			long tUpSum=0;
			long tCalcSum=0;
			jc.simulateStep(m);
			jc.tUp=0;
			jc.tCalc=0;
			for(int i=0;i<20;i++){
				jc.simulateStep(m);
				tUpSum+=jc.tUp;
				tCalcSum+=jc.tCalc;
				//System.out.println(i+": tUp: "+jc.tUp+ " tCalc: "+jc.tCalc+ " tUpSum/tCalcSum: "+(1.0*tUpSum)/tCalcSum);
				jc.tUp=0;
				jc.tCalc=0;
			}
			System.out.println(j+"\t"+(tUpSum+tCalcSum)/1000000);
		}
	}
}
