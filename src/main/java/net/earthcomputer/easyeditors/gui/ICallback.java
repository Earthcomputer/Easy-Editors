package net.earthcomputer.easyeditors.gui;

public interface ICallback<T> {

	T getCallbackValue();
	
	void setCallbackValue(T value);
	
}
