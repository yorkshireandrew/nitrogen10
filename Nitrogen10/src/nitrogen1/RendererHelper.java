package nitrogen1;
import java.util.Map;
import java.util.HashMap;

public class RendererHelper {
	
	static Map<String,Renderer> renderers = new HashMap<String,Renderer>();
	static Map<String,RendererTriplet> rendererTriplets = new HashMap<String,RendererTriplet>();
	
	final static void addRenderer(String s, Renderer r)
	{
		renderers.put(s, r);
	}
	
	final static Renderer getRenderer(String s) throws Exception
	{
		if(renderers.containsKey(s)){return renderers.get(s);}
		else throw new Exception("RendererHelper could not find renderer " + s);
	}
	
	final static void addRendererTriplet(String name, String r1name, String r2name, String r3name) throws NitrogenCreationException
	{
		Renderer r1, r2, r3;
		try{
			r1 = getRenderer(r1name);
			r2 = getRenderer(r2name);
			r3 = getRenderer(r3name);
		}
		catch(Exception e)
		{
			throw new NitrogenCreationException("Exception creating RenderTriplet " + name + " because " + e.getMessage());
		}
		rendererTriplets.put(name, new RendererTriplet(r1,r2,r3));
	}
	
	final static void addRendererTriplet(String name, RendererTriplet rt) throws Exception
	{
		if(rt == null) throw new Exception("RendererTriplet null when adding " + name);
		rendererTriplets.put(name, rt);
	}
	
	final static RendererTriplet getRendererTriplet(String name) throws Exception
	{
		if(rendererTriplets.containsKey(name))
		{
			return rendererTriplets.get(name);
		}
		else throw new Exception("The RendererTriplet named " + name + " does not exist");
	}
}
