package nitrogen1;

public interface ItemFactory {
	Item getItem(SharedImmutableSubItem in_sisi, Transform t);
	Backside getBackside();
	Vert getVertex(ImmutableVertex iv);
	Vert getVertex(ImmutableCollisionVert icv);
	
	int getAllocatedItemCount();
	int getAllocatedBacksideCount();
	int getAllocatedVertexCount();
	
	int getAllocatedItemMax();
	int getAllocatedBacksideMax();
	int getAllocatedVertexMax();
	void clearAllocatedMaximums();
	
	int getFreeItemCount();
	int getFreeBacksideCount();
	int getFreeVertexCount();
	
	void setFreeItems(int free);
	void setFreeBackside(int free);
	void setFreeVertex(int free);
	void trimToSize();

	int getMaxFreeItems();
	int getMaxFreeBackside();
	int getMaxFreeVertex();
	
	void setMaxFreeItems(int max);	
	void setMaxFreeBackside(int max);
	void setMaxFreeVertex(int max);		
}
