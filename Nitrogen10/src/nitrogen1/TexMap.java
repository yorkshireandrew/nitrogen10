package nitrogen1;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andrew
 */

import java.awt.Graphics2D;
import java.awt.Image;
//import java.awt.Component;
import java.awt.image.BufferedImage;

import java.net.URL;

public class TexMap {
    int[] tex;
    int w, h;

    TexMap(String st) throws NitrogenCreationException
    {
        URL url = getClass().getResource(st);
        if(url == null)throw new NitrogenCreationException("TexMap resource " + st + "could not be found");
    	Image ii = new javax.swing.ImageIcon(getClass().getResource(st)).getImage();
        BufferedImage i = new BufferedImage(ii.getWidth(null),ii.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics2D osg = i.createGraphics();
        osg.drawImage(ii, 0, 0, null);
        h = i.getHeight();
        w = i.getWidth();
        tex = i.getRGB(0, 0, w, h, null, 0, w);

    }
    
    final int getRGB(int x, int y)
    {
        return(tex[(x+y*w)]);
    }
    
    final int[] getTex()
    {
        return tex;
    }
    
    final int getWidth()
    {
        return w;
    }

    final int getHeight()
    {
        return h;
    }


}
