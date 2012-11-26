package nitrogen1;
/** Encapsulates the immutable details about a polygon so that they can be shared across several Item instances */
public class ImmutablePolygon {
	
	/** index of first vertex of polygon */
	int c1;	
	/** index of second vertex of polygon */
	int c2;	
	/** index of third vertex of polygon */
	int c3;	
	/** index of fourth vertex of polygon */
	int c4;
	
	/** vertex data to be associated with c1 for this immutable polygon */
	PolygonVertexData pvd_c1;
	/** vertex data to be associated with c2 for this immutable polygon */	
	PolygonVertexData pvd_c2;
	/** vertex data to be associated with c3 for this immutable polygon */	
	PolygonVertexData pvd_c3;
	/** vertex data to be associated with c4 for this immutable polygon */	
	PolygonVertexData pvd_c4;
	
	/** Information to pass to the renderer, for example the polygons colour */
	int[] polyData;
	
	/** The renderer triplet to use to render the polygon */
	RendererTriplet rendererTriplet;
	
	/** The texture map */
	TexMap textureMap;
	
	/** The backside index to use to determine which side is being viewed */
	int backsideIndex;
	
	/** True if the polygon uses backside culling */
	boolean isBacksideCulled;
	
	/** True if the polygon is transparent, and gets rendered during a transparent render pass */
	boolean isTransparent;
	
	ImmutablePolygon(
			int c1,
			int c2,
			int c3,
			int c4,
			PolygonVertexData pvd_c1,
			PolygonVertexData pvd_c2,
			PolygonVertexData pvd_c3,
			PolygonVertexData pvd_c4,
			int[] polyData,
			RendererTriplet rendererTriplet,
			TexMap textureMap,
			int backsideIndex,
			boolean isBacksideCulled,
			boolean isTransparent)
			{
				this.c1=c1;
				this.c2=c2;
				this.c3=c3;
				this.c4=c4;
				
				this.pvd_c1 = pvd_c1;
				this.pvd_c2 = pvd_c2;
				this.pvd_c3 = pvd_c3;
				this.pvd_c4 = pvd_c4;
				
				this.polyData=polyData;
				this.rendererTriplet = rendererTriplet;
				this.textureMap=textureMap;
				this.backsideIndex=backsideIndex;
				this.isBacksideCulled=isBacksideCulled;
				this.isTransparent=isTransparent;
			}
	
	/** Selects a Renderer from the Polygons RendererTriplet based on the whichRenderer parameter 
	 * @return The selected renderer*/
	Renderer getRenderer(int whichRenderer)
	{
		return(rendererTriplet.getRenderer(whichRenderer));
	}
}
