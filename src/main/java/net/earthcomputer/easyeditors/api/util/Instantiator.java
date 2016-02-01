package net.earthcomputer.easyeditors.api.util;

/**
 * Used whenever Easy Editors needs to create a new instance of something, but
 * doesn't know how to instantiate it.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 * @param <T>
 *            The type you're creating a new instance of
 */
public interface Instantiator<T> {

	/**
	 * Creates a new instance of type T
	 * 
	 * @return
	 */
	public T newInstance();

}
