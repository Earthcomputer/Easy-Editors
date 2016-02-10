package net.earthcomputer.easyeditors.api.util;

public interface ReturnedValueListener<T> {

	void returnValue(T value);
	
	void abortFindingValue(int reason);
	
}
