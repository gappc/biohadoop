package at.ac.uibk.dps.biohadoop.distributionmanager;

public interface DataMerger {

	public Object merge(Object o1, Object o2) throws DataMergeException;

}
