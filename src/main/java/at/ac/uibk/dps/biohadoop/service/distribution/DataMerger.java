package at.ac.uibk.dps.biohadoop.service.distribution;

public interface DataMerger<T> {

	public T merge(T o1, T o2) throws DataMergeException;

}
