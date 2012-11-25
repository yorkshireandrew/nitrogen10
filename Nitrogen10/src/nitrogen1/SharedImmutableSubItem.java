package nitrogen1;

//imports to read input files
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

// imports for map
import java.util.Map;
import java.util.HashMap;

/** contains the immutable component data of an Item allowing
 * it to be shared with other Items that are identical 
 * apart from their location, orientation and visibility.
 * <br/><br/>
 * Also contains the default values for an Items mutable fields 
 */
public class SharedImmutableSubItem {
	
	static final float hysteresis = (float) 1.02;
	
	/** The radius of a sphere containing the item completely */
	float boundingRadius;
		
	// *************** VALUES RELATING TO RENDERER SELECTION ************
	/** Distance at which to switch from using normal renderer to (slower)near renderer e.g. an interpolating renderer */
	final float nearRendererDist;	
	/** Distance at which to switch from using (slower)near renderer to normal renderer*/
	final float nearRendererDistPlus;
	
	/** Distance at which to switch from using (faster)far renderer to normal renderer */
	final float farRendererDist;	
	/** Distance at which to switch from using normal renderer to (faster)far renderer e.g. fixed colour renderer */
	final float farRendererDistPlus;

	// ***************** VALUES RELATING TO HLP POLYGON BREAKING ****************
	/** Distance at which  (slower) high level of perspective (HLP) breaking is enabled*/
	final float hlpBreakingDist;
	/** Distance at which  (slower) high level of perspective (HLP) breaking is disabled*/	
	final float hlpBreakingDistPlus;
	
	// ***************** VALUES RELATING TO BILLBOARD ORIENTATION DISTANCE ****************
	/** Distance at which (faster) billboard orientation computation is disabled*/
	final float billboardOrientationDist;
	/** Distance at which (faster) billboard orientation computation is enabled*/	
	final float billboardOrientationDistPlus;
	
	// **************** VALUES RELATING TO LEVEL OF DETAIL ***************
	/** Start index of polygons to render at a typical distance */
	final int normalDetailPolyStart;
	/** Start index of polygons to render if the Item is closer than improveDetailDistance */
	final int improvedDetailPolyStart;
	
	/** End index plus one of polygons to render at a typical distance */
	final int normalDetailPolyFinish;
	/** End index plus one of polygons to render if the Item is closer than improveDetailDistance */
	final int improvedDetailPolyFinish;
	
	/** Distance at which to switch to improved detail */
	final float improvedDetailDist;	
	/** Distance at which to switch to normal detail */
	final float improvedDetailDistPlus;
	
	//******************** VALUES RELATED TO MISCILLANIOUS ITEM THINGS *********************
	/** Backside culling should be overridden if the Item collides with the near plane
	 * so the interior of the item is displayed as black */
	boolean nearPlaneCrashBacksideOverride = true;
	
	/** Polygon data */
	final ImmutablePolygon[] immutablePolygons;
	final ImmutableBackside[] immutableBacksides;
	
	/** Vertex Data */
	final ImmutableVertex[] immutableVertexs;
	
	/**
	 * Constructs a SharedImmutableSubItem from a text file
	 * @param filename The name of text file used to initialise the SharedImmutableSubItem
	 * @param renderMap	A Map that contains instances of all the available Renderer classes mapped to a name String */
	SharedImmutableSubItem(final String filename) throws NitrogenCreationException{
		
        System.out.println("loading SISI from " + filename);
        Scanner in = null;
        
        try{
            File f = new File(filename);
            System.out.println(f.getAbsolutePath());
            in = new Scanner(new File(filename));
            int polygonVertexDataMax; 	// number of PolygonVertexData objects the file contains
            Map<String,PolygonVertexData> polygonVertexDataMap = new HashMap<String,PolygonVertexData>();
            int polygonMax; 	// number of ImmutablePolygons the file indicates it contains
            int textureMapMax; 	// number of texture maps the file indicates it references
            Map<String,TexMap> textureMaps = new HashMap<String,TexMap>();
            int backsideMax;	// number of ImmutableBacksides the file indicates it contains       
            int vertexMax; 		// number of ImmutableVertexs
            
            // read bounding radius
        	boundingRadius 	= readFloat(in, " unable to find boundingRadius loading " + filename);

        	// read values related to renderer
        	nearRendererDist = readFloat(in, "unable to find nearRendererDist loading " + filename);
        	farRendererDist  = readFloat(in, "unable to find farRendererDist loading " + filename);
        	hlpBreakingDist  = readFloat(in, "unable to find hlpBreakingDist loading " + filename);
        	billboardOrientationDist = readFloat(in, "unable to find billboardOrientationDist loading " + filename);

        	// read values related to level of detail
        	normalDetailPolyStart 	= readInt(in, "unable to find normalDetailPolyStart loading " + filename);
        	improvedDetailPolyStart = readInt(in, "unable to find improvedDetailPolyStart loading " + filename); 
        	normalDetailPolyFinish 	= readInt(in, "unable to find normalDetailPolyFinish loading " + filename);
        	improvedDetailPolyFinish = readInt(in, "unable to find improvedDetailPolyFinish loading " + filename);
        	improvedDetailDist = readFloat(in, "unable to find improvedDetailDist loading " + filename);
        	
        	//calculate hysteresis distances from read values
    		nearRendererDistPlus = nearRendererDist * hysteresis;
    		farRendererDistPlus = farRendererDist * hysteresis;	
    		hlpBreakingDistPlus = hlpBreakingDist * hysteresis;
    		billboardOrientationDistPlus = billboardOrientationDist * hysteresis;   
    		improvedDetailDistPlus = improvedDetailDist * hysteresis;
    		
    		// load all the TexMap object referenced by the SISI polygons
    		// and place them in the textureMaps map
    		textureMapMax = readInt(in, "unable to find textureMapMax loading" + filename);
 		
    		for(int i = 0; i < textureMapMax; i++)
    		{
    			String textureMapName;
    			String textureMapResource;
    			
    			TexMap newTextureMap;    			
    			textureMapName = readLine(in, "unable to find textureMap [" + i + "] name loading " + filename);
    			textureMapResource = readLine(in, "unable to find textureMap [" + i + "] resource name loading " + filename);
    			
    			try{
    				newTextureMap = new TexMap(textureMapResource);
    				textureMaps.put(textureMapName, newTextureMap);
    			}
    			catch(NitrogenCreationException e){
    				throw new NitrogenCreationException("unable to find textureMap resource " + textureMapResource + " loading " + filename + "   " + e.getMessage());
    			}
    		}
    		
        	// load all the PolgonVertexData
    		String polygonVertexDataName;
        	polygonVertexDataMax = readInt(in, "unable to find polygonVertexDataMax loading " + filename);    		
        	for(int i = 0; i < polygonVertexDataMax; i++)
        	{		
        		polygonVertexDataName = readLine(in, "unable to find polygonVertexData [" + i + "] name loading " + filename);
        		try
        		{
        			polygonVertexDataMap.put(polygonVertexDataName, buildPolygonVertexData(in));
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on polygonVertexData" + i + " caused by: " + e.getMessage());
        		}
        	} 
        	
    		// load all the ImmutablePolygons
        	polygonMax = readInt(in, "unable to find polygonMax reading " + filename);    		
        	immutablePolygons = new ImmutablePolygon[polygonMax];
        	for(int i = 0; i < polygonMax; i++)
        	{
        		
        		try
        		{
        			immutablePolygons[i] = buildImmutablePolygon(in , textureMaps, polygonVertexDataMap);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutablePolygon " + i + " caused by: " + e.getMessage());
        		}
        	}
        	
        	// load all the ImmutableBacksides
        	backsideMax = readInt( in, "unable to find backsideMax loading " + filename);    		
        	immutableBacksides = new ImmutableBackside[backsideMax];
        	for(int i = 0; i < backsideMax; i++)
        	{
        		
        		try
        		{
        			immutableBacksides[i] = buildImmutableBackside(in);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableBackside" + i + " caused by: " + e.getMessage());
        		}
        	} 
        	
        	
        	// load all the ImmutableVertexs
        	vertexMax = readInt( in, "unable to find backsideMax loading " + filename);    		
        	immutableVertexs = new ImmutableVertex[vertexMax];
        	for(int i = 0; i < vertexMax; i++)
        	{		
        		try
        		{
        			immutableVertexs[i] = buildImmutableVertex(in);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableVertex" + i + " caused by: " + e.getMessage());
        		}
        	} 
        }
        catch(NoSuchElementException nsee)
        {
        	throw new NitrogenCreationException("NoSuchElementException reading:" + filename);
        }
        catch(FileNotFoundException fnfe)
        {
        	throw new NitrogenCreationException("FileNotFoundException reading:" + filename);
        }
        finally
        {
            if(in != null) in.close();
        }       	
	}
	
	/** creates an ImmutablePolygon using text from a Scanner, it also requires a textureMap Map created earlier in the parsing so that it can identify and inject into the polygon a TexMap reference */
	ImmutablePolygon buildImmutablePolygon(Scanner in , Map<String, TexMap> textureMaps, Map<String, PolygonVertexData> polygonVertexDataMap) throws NitrogenCreationException, NoSuchElementException
	{
			int 			temp_c1;	
			int 			temp_c2;	
			int 			temp_c3;	
			int 			temp_c4;
			int[] 			temp_polyData;
			RendererTriplet temp_rendererTriplet;
			String			textureMapName;
			String			polygonVertexDataName;
			String			rendererTripletName;
			TexMap 			temp_textureMap = null;
			
			PolygonVertexData temp_pvd_c1 = null;
			PolygonVertexData temp_pvd_c2 = null;
			PolygonVertexData temp_pvd_c3 = null;
			PolygonVertexData temp_pvd_c4 = null;
			
			int 			temp_backsideIndex;
			boolean 		temp_isBacksideCulled;
			boolean 		temp_isTransparent;
			
			temp_c1 = readInt( in, "Unable to find c1.");
			temp_c2 = readInt( in, "Unable to find c2.");
			temp_c3 = readInt( in, "Unable to find c3.");
			temp_c4 = readInt( in, "Unable to find c4.");

			// read in the polygonVertexData associated with c1
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c1 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

			// read in the polygonVertexData associated with c2
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c2 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

		    // read in the polygonVertexData associated with c3
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c3 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

		    // read in the polygonVertexData associated with c4
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c4 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");
		
			// read in the polygons polyData
			int polyDataMax;
			polyDataMax = readInt( in, "Unable to find polyDataMax.");
			temp_polyData = new int[polyDataMax];
			for(int j = 0; j < polyDataMax; j++)
			{
				String temp = readLine( in, "Unable to find polyData [" + j + "]. (polyData entries must be on seperate lines)");
				int read = Integer.decode(temp);
				temp_polyData[j] = read;
			}
				
			// obtain renderer triplet
			rendererTripletName = readLine( in, "Unable to find RenderTriplet name.");

			// The Exception that getRenderTriplet() may throw is suitably informative
			try{
				temp_rendererTriplet = RendererHelper.getRendererTriplet(rendererTripletName);
			}
			catch(Exception e)
			{
				throw new NitrogenCreationException("Unable to find a RenderTriplet named " + rendererTripletName + "in RendererHelper" );
			}

			// obtain the texture map
			textureMapName = readLine( in, "Unable to find TexMap name, should be a line containing \"null\" if no texture map is used.");
			if(!textureMapName.equals("null"))
			{
				if(textureMaps.containsKey(textureMapName)){temp_textureMap = textureMaps.get(textureMapName);}
				else throw new NitrogenCreationException("The TexMap named " + textureMapName + "is not loaded by the file.");
			}
			
			// obtain the backside index			
			temp_backsideIndex = readInt( in, "Unable to find backsideIndex.");
				
			// obtain the isBacksideCulled
		    temp_isBacksideCulled = readBoolean( in, "Unable to find isBacksideCulled ");

			// obtain isTransparent
		    temp_isTransparent = readBoolean( in, "Unable to find isTransparent");
	
			return new ImmutablePolygon(
					temp_c1,
					temp_c2,
					temp_c3,
					temp_c4,
					temp_pvd_c1,
					temp_pvd_c2,
					temp_pvd_c3,
					temp_pvd_c4,
					temp_polyData,
					temp_rendererTriplet,
					temp_textureMap,
					temp_backsideIndex,
					temp_isBacksideCulled,
					temp_isTransparent
			);
      	}
	
	/** Creates an ImmutableBackside using text from a scanner */ 
	ImmutableBackside buildImmutableBackside(Scanner in) throws NitrogenCreationException
	{
		float temp_ix;
		float temp_iy;
		float temp_iz;
		float temp_inx;
		float temp_iny;
		float temp_inz;
		boolean temp_calculateLighting;
		
		temp_ix = readFloat(in, "Unable to find ix");
		temp_iy = readFloat(in, "Unable to find iy");
		temp_iz = readFloat(in, "Unable to find iz");
		
		temp_inx = readFloat(in, "Unable to find inx");
		temp_iny = readFloat(in, "Unable to find iny");
		temp_inz = readFloat(in, "Unable to find inz");

		temp_calculateLighting = readBoolean(in, "Unable to find calculateLighting");

		return new ImmutableBackside(
				temp_ix,
				temp_iy,
				temp_iz,
				temp_inx,
				temp_iny,
				temp_inz,
				temp_calculateLighting
				);
		
	}
	
	/** Creates an ImmutableVertex using text from a scanner */ 
	ImmutableVertex buildImmutableVertex(Scanner in) throws NitrogenCreationException
	{
		float temp_is_x;
		float temp_is_y;
		float temp_is_z;
		
		temp_is_x = readFloat(in, "Unable to find is_x");
		temp_is_y = readFloat(in, "Unable to find is_y");
		temp_is_z = readFloat(in, "Unable to find is_z");

		return new ImmutableVertex(
				temp_is_x,
				temp_is_y,
				temp_is_z
				);		
	}
	
	PolygonVertexData buildPolygonVertexData(Scanner in) throws NitrogenCreationException
	{	
	    float temp_aux1;
	    float temp_aux2;
	    float temp_aux3;
		temp_aux1 = readFloat(in, "Unable to find aux1");
		temp_aux2 = readFloat(in, "Unable to find aux2");
		temp_aux3 = readFloat(in, "Unable to find aux3");
		return (new PolygonVertexData(temp_aux1,temp_aux2,temp_aux3));
	}
	
	/** reads the next line from the scanner or throws a NitrogenCreationException containing the exception text if not found */
	final static String readLine(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		String retval;
		do{
			if(in.hasNextLine()){retval = in.nextLine();}
			else throw new NitrogenCreationException(exceptionText);
		}while(!retval.equals("/r/n"));
		return retval;
		
	}

	final static int readInt(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		if(in.hasNextInt()){return in.nextInt();}
		else throw new NitrogenCreationException(exceptionText);
	}
	
	final static float readFloat(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		if(in.hasNextFloat()){return in.nextFloat();}
		else throw new NitrogenCreationException(exceptionText);
	}

	final static boolean readBoolean(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		String temp = readLine(in,exceptionText);
		if(temp.equals("yes")||temp.equals("YES")||temp.equals("true")||temp.equals("TRUE"))return true;
		if(temp.equals("no")||temp.equals("NO")||temp.equals("false")||temp.equals("FALSE"))return false;
		throw new NitrogenCreationException(exceptionText + " caused by " + temp + " not equating to a boolean");
	}
}
