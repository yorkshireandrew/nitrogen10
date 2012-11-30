/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nitrogen1;

// 

/**
 *
 * @author andrew
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;  // needed for Dimension class
import java.awt.event.*;  // needed for ActionListener

final public class MyApplet extends JApplet{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1799576836511527595L;
	static final int APP_WIDTH = 501;
    static final int APP_HEIGHT =501;
    static final Renderer simpleTextureRenderer = new SimpleTextureRenderer();

    Transform t4_class;
    Transform t3_class;
    NitrogenContext cnc;
    float rot = 0.0f;	// linked to t4
    float climb = 0.0f;	// linked to t3
    
    public void init()
	{
            getContentPane().setLayout(new BorderLayout());
            Box box_image = Box.createHorizontalBox();
            Box button_box = Box.createVerticalBox();

            final NitrogenContext nc = new NitrogenContext(510,510,1,1,1,1000);
            
            JButton turnit = new JButton("turn");
            JButton turnit2 = new JButton("turn2");
            JButton climbit = new JButton("climb");
            JButton climbit2 = new JButton("climb2");
            JButton debug = new JButton("debug");
         
            button_box.add(turnit);
            button_box.add(turnit2);
            button_box.add(climbit);
            button_box.add(climbit2);
            button_box.add(debug);
            
            box_image.add(nc);
            box_image.add(button_box);
            
            getContentPane().add(box_image);
            getContentPane().validate();
            getContentPane().setVisible(true);

            nc.cls(0xFF0000FF);
            
            // start rendering process
            final Transform t1, t2, t3, t4;
            SharedImmutableSubItem sisi = null;
           
            t1	= new 	Transform(
            						null,
            						1f, 0f, 0f, 0f,
            						0f, 1f, 0f, 0f,
            						0f, 0f, 1f, 0f);
            
            t2	= new 	Transform(
					t1,
					1f, 0f, 0f, 0f,
					0f, 1f, 0f, 0f,
					0f, 0f, 1f, -20f);
            
            t3	= new 	Transform(
					t2,
					1f, 0f, 0f, 0f,
					0f, 1f, 0f, 0f,
					0f, 0f, 1f, 0f);            
            
            t4	= new 	Transform(
					t3,
					1f, 0f, 0f, 0f,
					0f, 1f, 0f, 0f,
					0f, 0f, 1f, 0f);
            
            // add renderers to RendererHelper
            SimpleTextureRenderer str = new SimpleTextureRenderer();
            RendererTriplet rt = new RendererTriplet(str);
            try
            {
            	RendererHelper.addRendererTriplet("str",rt);
            }
            catch(Exception e){System.out.println(e.getMessage());}

            SimpleSingleColourRenderer sscr = new SimpleSingleColourRenderer();           
            RendererTriplet sscrt = new RendererTriplet(sscr);
            try
            {
            	RendererHelper.addRendererTriplet("sscr",sscrt);
            }
            catch(Exception e){System.out.println(e.getMessage());}
            
            try{
            	
            	sisi = new SharedImmutableSubItem("test1.txt");
            }
            catch(NitrogenCreationException e)
            {
            	e.printStackTrace();           	
            }
            
            this.t3_class = t3;                  
            this.t4_class = t4;           
            Item i = new Item(sisi,t4_class);
            i.setVisibility(true);
            
            t1.render(nc);
            nc.repaint();
            this.cnc = nc;
            
            turnit.addActionListener(
            		new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
							rot += 0.05f;
							t4_class.setTurn(rot);
							System.out.println("rot:"+rot);
							cnc.cls(0xFF0000FF);
							t1.render(nc);
							nc.repaint();
							outputPerformanceData(nc);
						}}
            		);
            
            turnit2.addActionListener(
            		new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
							rot -= 0.05f;
							System.out.println("rot:"+rot);
							t4_class.setTurn(rot);
							cnc.cls(0xFF0000FF);
							t1.render(nc);
							nc.repaint();
							outputPerformanceData(nc);
						}}
            		);  
            
            climbit.addActionListener(
            		new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
							climb += 0.05f;
							System.out.println("climb:"+climb);
							t3_class.setClimb(climb);
							cnc.cls(0xFF0000FF);
							t1.render(nc);
							nc.repaint();
							outputPerformanceData(nc);
						}}
            		);              
            climbit2.addActionListener(
            		new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
							climb -= 0.05f;
							System.out.println("climb:"+climb);
							t3_class.setClimb(climb);
							cnc.cls(0xFF0000FF);
							t1.render(nc);
							nc.repaint();
							outputPerformanceData(nc);
						}}
            		);              
            
            debug.addActionListener(
            		new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
							nc.debug = true;
						}}
            		);  
            
            
            
            
    /*        

            int wid = 500;
            int high = 500;
            Vert    v_a     = new Vert(wid,0);
            Vert    v_b     = new Vert(0,1);
            Vert    v_c     = new Vert(0,high);
            Vert    v_d     = new Vert(wid,high+1);
            
            // set texture coordinates
            v_a.setAux2(101,1);
            v_b.setAux2(1,1);
            v_c.setAux2(1,101);
            v_d.setAux2(101,101);
            
            v_a.setZ(255);
            v_b.setZ(255);
            v_c.setZ(125);
            v_d.setZ(0);
            
            long t1, t2, test1;
            test1 = 0;

            int maxloops = 1;
            int innerloops = 100;
            
            for(int loop = 0; loop < maxloops; loop++)
            {
                mb.cls(0xFF0000FF);
                t1 = System.currentTimeMillis();
                for(int x = 0; x < innerloops; x++)
                {
                    v_a.setZ(-x);
                    v_b.setZ(-x);
                    v_c.setZ(-x);
                    v_d.setZ(-x);
                    mb.Plot(v_a, v_b, v_c, v_d, hello_world_texture, null, simpleTextureRenderer);
                }
                t2 = System.currentTimeMillis();

                test1 += (t2 - t1);

                mb.repaint();

        }
            System.out.println(" Normal      = " + test1);
*/
    }


    @Override
        public Dimension getMinimumSize()
    {
        System.out.printf("getMinimumSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize()
    {
        System.out.printf("getPreferredSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize()
    {
        System.out.printf("getMaximumSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }
    
    public void outputPerformanceData(NitrogenContext nc)
    {
    	System.out.println("items rendered ............" + nc.itemsRendered);
    	System.out.println("polygonsRendered rendered ." + nc.polygonsRendered);
    	System.out.println("clippedPolygonsRendered ..." + nc.clippedPolygonsRendered);
    	System.out.println("polygonRendererCalls ......" + nc.polygonRendererCalls);
    	System.out.println("linesRendered ............." + nc.linesRendered);
    }

    
}// end of MyApplet
