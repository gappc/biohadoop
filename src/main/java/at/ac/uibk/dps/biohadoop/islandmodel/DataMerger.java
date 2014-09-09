package at.ac.uibk.dps.biohadoop.islandmodel;

public interface DataMerger<T> {

	public T merge(T o1, T o2) throws IslandModelException;

}
