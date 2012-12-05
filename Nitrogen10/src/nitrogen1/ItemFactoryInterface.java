package nitrogen1;

public interface ItemFactoryInterface {
	Item getItem(SharedImmutableSubItem in_sisi, Transform t);
	Backside getBackside();
	Vert getVertex(ImmutableVertex iv);
	CollisionVert getCollisionVertex(ImmutableCollisionVert icv);
	
	int getAllocatedItemCount();
	int getAllocatedBacksideCount();
	int getAllocatedVertexCount();
	int getAllocatedCollisionVertexCount();
	
	int getAllocatedItemMax();
	int getAllocatedBacksideMax();
	int getAllocatedVertexMax();
	int getAllocatedCollisionVertexMax();
	void clearAllocatedMaximums();
	
	int getFreeItemCount();
	int getFreeBacksideCount();
	int getFreeVertexCount();
	int getFreeCollisionVertexCount();	
	
	void setFreeItems(int free);
	void setFreeBackside(int free);
	void setFreeVertex(int free);
	void setFreeCollisionVertexes(int free);
	void trimToSize();

	int getMaxFreeItems();
	int getMaxFreeBackside();
	int getMaxFreeVertex();
	int getMaxFreeCollisionVertex();	
	
	void setMaxFreeItems(int max);	
	void setMaxFreeBackside(int max);
	void setMaxFreeVertex(int max);
	void setMaxFreeCollisionVertex(int max);		
	
	
	
}
