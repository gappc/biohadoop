package at.ac.uibk.dps.biohadoop.handler.distribution;

public interface DataMerger<T> {

	public T merge(T o1, T o2) throws DataMergeException;

}