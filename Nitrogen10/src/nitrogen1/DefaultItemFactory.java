package nitrogen1;

/** default class that implements the factory methods using the new operator */
public class DefaultItemFactory implements ItemFactory{
	
	private int allocatedItems = 0;
	private int allocatedBacksides = 0;
	private int allocatedVertexes = 0;
	private int allocatedCollisionVertexes = 0;
	
	private int allocatedItemMax = 0;
	private int allocatedBacksidesMax = 0;
	private int allocatedVertexesMax = 0;
	private int allocatedCollisionVertexesMax = 0;	
	
	private int maxFreeItems = 0;
	private int maxFreeBackside = 0;
	private int maxFreeVertex = 0;
	private int maxFreeCollisionVertex = 0;
	
	public Item getItem(SharedImmutableSubItem in_sisi, Transform t)
	{
		if(++allocatedItems > allocatedItemMax)allocatedItemMax = allocatedItems;
		Item retval = new Item();
		retval.initializeItem(in_sisi, t, this);
		return(retval);
	}
	
	public Backside getBackside()
	{
		if(++allocatedBacksides > allocatedBacksidesMax)allocatedBacksidesMax = allocatedBacksides;
		return new Backside();
	}
	
	public Vert getVertex(ImmutableVertex iv)
	{
		if(++allocatedVertexes > allocatedVertexesMax)allocatedVertexesMax = allocatedVertexes;
		return new Vert(iv);
	}
	
	public CollisionVert getCollisionVertex(ImmutableCollisionVert icv)
	{
		if(++allocatedCollisionVertexes > allocatedCollisionVertexesMax)allocatedCollisionVertexesMax = allocatedCollisionVertexes;
		return new CollisionVert(icv);
	}
	
	public int getAllocatedItemCount(){return allocatedItems;}
	public int getAllocatedBacksideCount(){return allocatedBacksides;}
	public int getAllocatedVertexCount(){return allocatedVertexes;}
	public int getAllocatedCollisionVertexCount(){return allocatedCollisionVertexes;}

	public int getAllocatedItemMax(){return allocatedItemMax;}	
	public int getAllocatedBacksideMax(){return allocatedBacksidesMax;}
	public int getAllocatedVertexMax(){return allocatedVertexesMax;}
	public int getAllocatedCollisionVertexMax(){return allocatedCollisionVertexesMax;}
	public void clearAllocatedMaximums()
	{
		allocatedBacksidesMax = allocatedBacksides;
		allocatedVertexesMax = allocatedVertexes;
		allocatedCollisionVertexesMax = allocatedCollisionVertexes;
	}

	public int getFreeItemCount(){return 0;};
	public int getFreeBacksideCount(){return 0;};
	public int getFreeVertexCount(){return 0;};
	public int getFreeCollisionVertexCount(){return 0;};	

	public void setFreeItems(int free){};	
	public void setFreeBackside(int free){};
	public void setFreeVertex(int free){};
	public void setFreeCollisionVertexes(int free){};
	public void trimToSize(){}

	public int getMaxFreeItems() {
		return maxFreeItems;
	}
	
	public void setMaxFreeItems(int maxFreeItems) {
		this.maxFreeItems = maxFreeItems;
	}	
	public int getMaxFreeBackside() {
		return maxFreeBackside;
	}

	public void setMaxFreeBackside(int maxFreeBackside) {
		this.maxFreeBackside = maxFreeBackside;
	}

	public int getMaxFreeVertex() {
		return maxFreeVertex;
	}

	public void setMaxFreeVertex(int maxFreeVertex) {
		this.maxFreeVertex = maxFreeVertex;
	}

	public int getMaxFreeCollisionVertex() {
		return maxFreeCollisionVertex;
	}

	public void setMaxFreeCollisionVertex(int maxFreeCollisionVertex) {
		this.maxFreeCollisionVertex = maxFreeCollisionVertex;
	};

}
