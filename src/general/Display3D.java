/*
 * Copyright (c) 2010-2011, Martin Pernollet
 * All rights reserved. 
 *
 * Redistribution in binary form, with or without modification, is permitted.
 * Edition of source files is allowed.
 * Redistribution of original or modified source files is FORBIDDEN.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package general;


import interfaces.CalcModel;
import interfaces.Display;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.camera.ICameraMouseController;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.*;
import org.jzy3d.chart.Settings;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Scale;
import org.jzy3d.maths.TicToc;
import org.jzy3d.maths.Utils;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.Renderer2d;
import org.jzy3d.plot3d.transform.*;


public class Display3D extends Frame implements Display{


	private static final long serialVersionUID = 1L;

	public static final String HB = "h+b";
	public static final String B = "b";
	public static final String HU = "hu";
	public static final String HV = "hv";
	public static final String HUHV = "huhv";
	public static final String NONE = "none";

	protected static org.jzy3d.maths.Rectangle DEFAULT_WINDOW = new org.jzy3d.maths.Rectangle(0,50,600,600);

	private Panel pane;
	private OptionsPane optionPane;
	private Shape surfaceHB;
	private Shape surfaceB;
	private Shape surfaceHU;
	private Shape surfaceHV;
	private Shape surfaceHUHV;
	private List<Shape> surfaces;

	private Mapper mapperHB;	
	private Mapper mapperB;
	private Mapper mapperHU;	
	private Mapper mapperHV;
	private Mapper mapperHUHV;	
	private String timeText;
	private boolean running=false;

	
	
	protected Thread t;
	private Memory m;
	private CalcModel cm;
	
	
    protected AWTChart chart;
    protected String canvasType="awt";

//	private CameraThreadController cameraThread;

	//private ICameraMouseController mouse;

	
	public Display3D(Memory m, CalcModel cm){
		//super();
		this.setName(this.getName());
		this.m=m;
		this.cm=cm;
		pane=new Panel();
		pane.setPreferredSize(new Dimension(600, 600));
		pane.setLayout(new BorderLayout());
		optionPane=new OptionsPane();
		try {
			Settings.getInstance().setHardwareAccelerated(true);
			this.init();
			
//			mouse=SwingChartLauncher.configureControllers(chart, getName(), false, false);
			chart.addMouseController();

			this.setLayout(new BorderLayout(5, 5));
			Component c=(Component)this.getChart().getCanvas();
			((ICanvas) c).addMouseController(new MouseL());
			pane.add(c, BorderLayout.CENTER);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		this.setBounds(DEFAULT_WINDOW);
		//this.pack();
		
		this.add(pane, BorderLayout.CENTER);
		this.add(optionPane, BorderLayout.EAST);
		this.setBounds(50,105,600,600);
		this.pack();
		

	}
	
	
	public void init(){
		
	    mapperHB = new Mapper(){
            public double f(double x, double y) {
            	if(m.getB()[(int)x][(int)y]>0)
            		return 0;
            	
            	return m.getH()[(int)x][(int) y]+m.getB()[(int)x][(int)y];
            }
        };
        
        mapperB = new Mapper(){
        	public double f(double x, double y) {
//            	return m.getB()[(int)x][(int)y]>1 ? 1 : m.getB()[(int)x][(int)y];
        		return m.getB()[(int)x][(int)y];
            }
        };
        
	    mapperHU = new Mapper(){
            public double f(double x, double y) {
            	return m.getHu()[(int)x][(int)y];
            }
        };
        
        mapperHV = new Mapper(){
        	public double f(double x, double y) {
            	return m.getHv()[(int)x][(int)y];
            }
        };
        
        mapperHUHV = new Mapper(){
        	public double f(double x, double y) {
        		return m.getHu()[(int)x][(int)y] + m.getHv()[(int)x][(int)y];
            }
        };
        
        
        Range Xrange = new Range(2,m.getCol()-2);
        Range Yrange = new Range(2,m.getRow()-2);

        int xSteps   = m.getCol()/5;
        int ySteps   = m.getRow()/5;
        
        // Create the object to represent the function over the given range.
	    surfaceHB = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(Xrange, xSteps, Yrange, ySteps), mapperHB);
	    surfaceHB.setColorMapper(new ColorMapper(new ColorMapHotCold(), surfaceHB.getBounds().getZmin(), surfaceHB.getBounds().getZmax(), new Color(1,1,1,.8f)));
	    surfaceHB.setFaceDisplayed(true);
	    surfaceHB.setWireframeDisplayed(false);

        surfaceB = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(Xrange, xSteps, Yrange, ySteps), mapperB);
        BathyColorMap colMap= new BathyColorMap();
        colMap.setDirection(false);
        surfaceB.setColorMapper(new ColorMapper(colMap, surfaceB.getBounds().getZmin(), surfaceB.getBounds().getZmax()>0?1:surfaceB.getBounds().getZmax()));
        surfaceB.setFaceDisplayed(true);
        surfaceB.setWireframeDisplayed(false);
			
        surfaceHU = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(Xrange, xSteps, Yrange, ySteps), mapperHU);
	    surfaceHU.setColorMapper(new ColorMapper(new ColorMapHotCold(), surfaceHB.getBounds().getZmin(), surfaceHB.getBounds().getZmax(), new Color(1,1,1,.8f)));
	    surfaceHU.setFaceDisplayed(true);
	    surfaceHU.setWireframeDisplayed(false);
	    
	    surfaceHV = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(Xrange, xSteps, Yrange, ySteps), mapperHV);
	    surfaceHV.setColorMapper(new ColorMapper(new ColorMapHotCold(), surfaceHB.getBounds().getZmin(), surfaceHB.getBounds().getZmax(), new Color(1,1,1,.8f)));
	    surfaceHV.setFaceDisplayed(true);
	    surfaceHV.setWireframeDisplayed(false);
	    
	    surfaceHUHV = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(Xrange, xSteps, Yrange, ySteps), mapperHUHV);
	    surfaceHUHV.setColorMapper(new ColorMapper(new ColorMapHotCold(), surfaceHB.getBounds().getZmax()*-2, surfaceHB.getBounds().getZmax()*2, new Color(1,1,1,.8f)));
	    surfaceHUHV.setFaceDisplayed(true);
	    surfaceHUHV.setWireframeDisplayed(false);
       
		surfaces = new ArrayList<Shape>();
		surfaces.add(surfaceB);
		surfaces.add(surfaceHB);
		surfaces.add(surfaceHU);
		surfaces.add(surfaceHV);
		surfaces.add(surfaceHUHV);
		
        // Create a chart 
//        Quality quality=new Quality(true, true, true, false, false, false, false);
        Quality quality=Quality.Advanced;
//        chart = AWTChartComponentFactory.chart(Quality.Advanced);
//        chart = new Chart(Quality.Nicest, getCanvasType());
        quality.setDisableDepthBufferWhenAlpha(true);
        quality.setDepthActivated(false);
        chart = (AWTChart) AWTChartComponentFactory.chart(quality, "awt");
        ICameraMouseController mouse=chart.addMouseController();
        CameraThreadController cameraThread = new CameraThreadController(chart);
        mouse.addSlaveThreadController(cameraThread);
        chart.pauseAnimator();
    	chart.addKeyController();
    	chart.addScreenshotKeyController();
    	
    	if(m.getRow()!=m.getCol()){
	        Transform trans = new Transform();
	        org.jzy3d.plot3d.transform.Scale scale = new org.jzy3d.plot3d.transform.Scale(new Coord3d(1.f, 1.f, 0.01f));
	        trans.add(scale);
	    	chart.getView().setSquared(false);
	        surfaceB.setTransformBefore(trans);
    	}
//        for(Shape surface : surfaces)
//        	surface.setTransformBefore(trans);


        //Colorbars are too big -.-
        
//        IAxeLayout layoutHB=chart.getView().getAxe().getLayout();
//        ColorbarLegend colorbarHB=new ColorbarLegend(surfaceHB, layoutHB);
//        colorbarHB.setViewPort(100, 100);
//        colorbarHB.setStretchToFill(true);
//        
//        IAxeLayout layoutB=chart.getView().getAxe().getLayout();
//        ColorbarLegend colorbarB=new ColorbarLegend(surfaceB, layoutHB);
//        colorbarB.setViewPort(100, 100);
//        colorbarB.setStretchToFill(true);
//        
//        surfaceHB.setLegend(colorbarHB);
//        surfaceB.setLegend(colorbarB);
        
        for (Shape surface : surfaces)
        	chart.getScene().getGraph().add(surface);

        chartUpdate();

        // display FPS
        timeText = "";
        chart.addRenderer(new Renderer2d(){
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setColor(java.awt.Color.BLACK);
                g2d.drawString(timeText, 50, 50);
            }
        });
	}
	
	public void chartUpdate(){
		synchronized (this) {
			
			try{
				if(optionPane.getActive(HB)){
		        	surfaceHB.setDisplayed(true);
		        }else{
					surfaceHB.setDisplayed(false);
		        }
				if(optionPane.getActive(B)){
		        	surfaceB.setDisplayed(true);
		        }else{
					surfaceB.setDisplayed(false);
		        }
				if(optionPane.getActive(HU)){
		        	surfaceHU.setDisplayed(true);
		        }else{
					surfaceHU.setDisplayed(false);
		        }
				if(optionPane.getActive(HV)){
		        	surfaceHV.setDisplayed(true);
		        }else{
					surfaceHV.setDisplayed(false);
		        }
				if(optionPane.getActive(HUHV)){
		        	surfaceHUHV.setDisplayed(true);
		        }else{
					surfaceHUHV.setDisplayed(false);
		        }
			}catch(Exception e){
				e.printStackTrace();
			}
			chart.render();
			System.out.println(chart.getScene().getGraph());
		}
		
    }
		
	public void start(){
		chart.resumeAnimator();
		timeText = "";
		t = new Thread(){
			TicToc tt1 = new TicToc();
			TicToc tt2 = new TicToc();

			@Override
			public void run() {
				running=true;
				while(running){
					try {
						sleep(1);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
					tt1.tic();
					//cm.simulateStepByTime(m, ((float)tt1.elapsedSecond()+(float)tt2.elapsedSecond())*3);
					float a=cm.simulateStep(m);
					//a+=cm.simulateStep(m);
					tt1.toc();
					tt2.tic();
					try{
						if(optionPane.getActive(HB))mapperHB.remap(surfaceHB);
						if(optionPane.getActive(HU))mapperHU.remap(surfaceHU);
						if(optionPane.getActive(HV))mapperHV.remap(surfaceHV);
						if(optionPane.getActive(HUHV))mapperHUHV.remap(surfaceHUHV);
					}catch(Exception e){
						e.printStackTrace();
					}
					
					//chart.render();
					tt2.toc();
					
					timeText = "timeStep: "+a+" calc: "+Utils.num2str(tt1.elapsedSecond(), 4) + "s disp: "+ Utils.num2str(tt2.elapsedSecond(), 4)+" s";
				}
			}
		};
		t.start();
	}
	
	
	public void stop(){
		//if(t!=null)
			//t.interrupt();
		running=false;
		try {
			t.join(500);
			if(t!=null)
				t.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		chart.pauseAnimator();
	}


	@Override
	public void setMemory(Memory m) {
		this.m=m;
		mapperB.remap(surfaceB);
		mapperHB.remap(surfaceHB);
		mapperHU.remap(surfaceHU);
		mapperHV.remap(surfaceHV);
		mapperHUHV.remap(surfaceHUHV);
		colorUpdate();
		chartUpdate();
		
	}

	private void colorUpdate() {
// 		as 
//		new Scale(surfaceHU.getBounds().getZmin(), surfaceHU.getBounds().getZmax())
//		stays on the values it gets when first invoked (0,0) it is not usable to change the ColorScale while running
	    float newMin=Float.MAX_VALUE;
	    float newMax=Float.MIN_VALUE;
		for(int x=1;x<m.getCol()-1;x++){
			for(int y=1;y<m.getRow()-1;y++){
				
				if(m.getB()[x][y]<newMin)newMin=m.getB()[x][y];
				if(m.getB()[x][y]>newMax)newMax=m.getB()[x][y];
			}
		}
	    surfaceB.getColorMapper().setScale(new Scale(newMin, newMax>0?1:newMax));
	    
	    
	    newMin=Float.MAX_VALUE;
	    newMax=Float.MIN_VALUE;
		for(int x=1;x<m.getCol()-1;x++){
			for(int y=1;y<m.getRow()-1;y++){
				
				if(m.getH()[x][y]+m.getB()[x][y]<newMin)newMin=m.getB()[x][y]+m.getH()[x][y];
				if(m.getH()[x][y]+m.getB()[x][y]>newMax)newMax=m.getB()[x][y]+m.getH()[x][y];
			}
		}
	    
	    surfaceHB.getColorMapper().setScale(new Scale(newMin, newMax));

	    newMin=Float.MAX_VALUE;
	    newMax=Float.MIN_VALUE;
		for(int x=1;x<m.getCol()-1;x++){
			for(int y=1;y<m.getRow()-1;y++){
				
				if(m.getHu()[x][y]<newMin)newMin=m.getHu()[x][y];
				if(m.getHu()[x][y]>newMax)newMax=m.getHu()[x][y];
			}
		}
	    
        surfaceHU.getColorMapper().setScale(new Scale(newMin, newMax));
        
	    newMin=Float.MAX_VALUE;
	    newMax=Float.MIN_VALUE;
		for(int x=1;x<m.getCol()-1;x++){
			for(int y=1;y<m.getRow()-1;y++){
				
				if(m.getHv()[x][y]<newMin)newMin=m.getHv()[x][y];
				if(m.getHv()[x][y]>newMax)newMax=m.getHv()[x][y];
			}
		}
        surfaceHV.getColorMapper().setScale(new Scale(newMin, newMax));

	}



	public Memory getMemory() {
		return m;
	}


	public CalcModel getCm() {
		return cm;
	}


	@Override
	public void setCalcModel(CalcModel cm) {
		this.cm=cm;
	}

	@Override
	public void play() {
		start();
	}

	@Override
	public void pause() {
		stop();
	}



	public String getName() {
		return this.getClass().getSimpleName();
//		return "Display 3D";
	}
	
	
	public String getPitch(){
		return "";
	}
	
	
	public boolean isInitialized(){
	    return chart!=null;
	}
	
	
	public Chart getChart(){
        return chart;
    }
	
	public String getCanvasType(){
	    return canvasType;
	}
	
	
    public boolean hasOwnChartControllers(){
	    return false;
	}
    
    public void dispose(){
    	chart.clear();
    	chart.dispose();
    }


	private class MouseL implements MouseListener, MouseWheelListener, MouseMotionListener {
		
		@Override
		public void mouseClicked( MouseEvent arg0) {
			// TODO Auto-generated method stub
			chart.render();
		}

		@Override
		public void mouseDragged( MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered( MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited( MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved( MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed( MouseEvent arg0) {
			chart.resumeAnimator();
		}

		@Override
		public void mouseReleased( MouseEvent arg0) {
			if(!running)
				chart.pauseAnimator();
		}

		@Override
		public void mouseWheelMoved( MouseWheelEvent arg0) {
			chart.render();
		}
	}

	private class OptionsPane extends Panel{
		
		private static final long serialVersionUID = 1L;
		Checkbox[] modeSelect=new Checkbox[6];
		JPanel checkboxesPane;
		JSlider scaleSlide;
		JLabel scaleText;
		JPanel all;

		public OptionsPane() {
			super();
			all=new JPanel(new SpringLayout());
			
			checkboxesPane=new JPanel();
			checkboxesPane.setLayout(new GridLayout(3, 2));
			modeSelect[0]=new Checkbox(HB, true);
			modeSelect[1]=new Checkbox(B, true);
			CheckboxGroup groupeHuHv=new CheckboxGroup();
			modeSelect[2]=new Checkbox(NONE, true, groupeHuHv);
			modeSelect[3]=new Checkbox(HU, false, groupeHuHv);
			modeSelect[4]=new Checkbox(HV, false, groupeHuHv);
			modeSelect[5]=new Checkbox(HUHV, false, groupeHuHv);
			
			
			for(int i=0;i<modeSelect.length;i++){
				modeSelect[i].addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {
						new Thread(){
							public void run(){
								chartUpdate();
								colorUpdate();

							}
						}.start();
					}
				});
				checkboxesPane.add(modeSelect[i]);
				
			}
			all.add(checkboxesPane);
			
			
			
			JPanel sliderPane=new JPanel(new SpringLayout());
			scaleSlide=new JSlider(JSlider.HORIZONTAL, 0, 1000, 1);
			scaleSlide.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent arg0) {
					scaleText.setText("scale H: "+scaleSlide.getValue());
					
			        Transform trans = new Transform();
			        org.jzy3d.plot3d.transform.Scale scale = new org.jzy3d.plot3d.transform.Scale(new Coord3d(1.f, 1.f, scaleSlide.getValue()));
			        trans.add(scale);
			        
			        surfaceHB.setTransformBefore(trans);
			        chart.render();
				}
			});
			scaleText=new JLabel("scale H: "+scaleSlide.getValue());
			sliderPane.add(scaleText);
			sliderPane.add(scaleSlide);
			SpringUtilities.makeGrid(sliderPane, 2, 1, 5, 5, 0, 0);
			all.add(sliderPane);
			
			SpringUtilities.makeGrid(all, 2, 1, 5, 5, 0, 0);
			this.add(all);
			

			
		}
		
		boolean getActive(String arg) throws Exception{
			for(int i=0;i<modeSelect.length;i++){
				if(modeSelect[i].getLabel().equals(arg)){
					return modeSelect[i].getState();
				}
			}
			System.out.println(arg+" "+modeSelect[0].getLabel());
			throw new Exception("Invalide Argument: "+arg);
			
		}
	}
	
}
