package com.bombheadgames.nitrogen1;

public interface ItemFactory {

	/** generates an Item either from the preallocated pool or constructs a new one */ 
	public Item getItem(SharedImmutableSubItem in_sisi, Transform t);
	
	/** generates a Backside either from the preallocated pool or constructs a new one */	
	public Backside getBackside(ImmutableBackside ib);

	/** generates a Vertex either from the preallocated pool or constructs a new one */	
	public Vertex getVertex(ImmutableVertex iv);

	/** generates a (collision) Vertex either from the preallocated pool or constructs a new one */		
	public Vertex getVertex(ImmutableCollisionVertex icv);

	/** returns the number of Items waiting to be allocated */
	public int getFreeItemCount();
	
	/** returns the number of Backsides waiting to be allocated */
	public int getFreeBacksideCount();
	
	/** returns the number of Vertexes waiting to be allocated */
	public int getFreeVertexCount();
	
	/** Alters the size of the Item pool,, adjusting maxFreeItems  */
	public void setFreeItems(int free);

	/** Alters the size of the Backside pool, adjusting maxFreeBacksides  */
	public void setFreeBacksides(int free);

	/** Alters the size of the pool of preallocated Vertexes, adjusting maxFreeVertexes */
	public void setFreeVertexes(int free);

	
	/** Trims the size of all the pools to zero, releasing memory  */
	public void trimToSize();

	/** returns the maximum size of the Item pool */
	public  int getMaxFreeItems();
	
	/** sets the maximum size of the Item pool can grow too */	
	public void setMaxFreeItems(int maxFreeItems);
		
	/** returns the maximum size of the Backside pool */	
	public int getMaxFreeBacksides();

	/** sets the max size the Backside pool can grow too */	
	public void setMaxFreeBacksides(int maxFreeBackside);
	
	/** returns the maximum size of the Vertex pool */	
	public int getMaxFreeVertexes();

	/** sets the max size the Vertex pool can grow too */	
	public void setMaxFreeVertexes(int maxFreeBackside);
	
	public void recycle(Item item);

// additional methods for development only
	
//		int getAllocatedItemCount();		
//		int getAllocatedBacksideCount();
//		int getAllocatedVertexCount();
	
//		int getAllocatedItemMax();
//		int getAllocatedBacksideMax();
//		int getAllocatedVertexMax();
//		void clearAllocatedMaximums();
		
	

}
