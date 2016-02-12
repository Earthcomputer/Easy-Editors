package net.earthcomputer.easyeditors.api.util;

/**
 * An alternative to the return statement for operations that take a long time
 * and run on a separate thread.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 * @param <T>
 *            The type of the returned values
 */
public interface ReturnedValueListener<T> {

	/**
	 * Called when the value is returned successfully. When this method is
	 * called, it is guaranteed that {@link #abortFindingValue(int)} won't be
	 * called
	 * 
	 * @param value
	 *            - the returned value
	 */
	void returnValue(T value);

	/**
	 * Called when the value could not be returned successfully. When this
	 * method is called, it is guaranteed that {@link #returnValue(Object)}
	 * won't be called
	 * 
	 * @param reason
	 *            - an abort code. The meanings of different values are defined
	 *            in the methods which use this class
	 */
	void abortFindingValue(int reason);

}
