package general;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.AbstractColorMap;
import org.jzy3d.colors.colormaps.IColorMap;

/**
* <pre>
* <code>
*       blue     green     red
*     /-------\/-------\/-------\
*    /        /\       /\        \
*   /        /  \     /  \        \
*  /        /    \   /    \        \
* |----------------|----------------|
* 0               0.5               1
* </code>
* </pre>
*/
public class BathyColorMap extends AbstractColorMap implements IColorMap {
    public BathyColorMap() {
    	super();
    }
    
    /** @inheritDoc */
    public Color getColor( double x, double y, double z, double zMin, double zMax ){
        double rel_value = processRelativeZValue(z, zMin, zMax);
        
        float r = (float) colorComponentRelative( rel_value, 0.7f, -0.4f, 1.4f );
        float g = (float) colorComponentRelative( rel_value, 0.25f, -0.5f, 2.5f ); 
        float b = 0;
        
        return new Color( r, g, b );
    }
}