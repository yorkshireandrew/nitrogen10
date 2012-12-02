package nitrogen1;

import java.io.Serializable;

public class PolygonVertexData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3230168654732316627L;
	float aux1;
	float aux2;
	float aux3;
	
	PolygonVertexData(float aux1, float aux2, float aux3)
	{
		this.aux1 = aux1;
		this.aux2 = aux2;
		this.aux3 = aux3;
	}
}
