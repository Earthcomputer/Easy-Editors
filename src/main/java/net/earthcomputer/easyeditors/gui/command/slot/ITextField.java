package net.earthcomputer.easyeditors.gui.command.slot;

import com.google.common.base.Predicate;

/**
 * An abstract text field
 * 
 * @author Earthcomputer
 *
 * @param <T>
 *            - the type of data stored/being editied by this text field
 */
public interface ITextField<T> {

	/**
	 * Returns the maximum string length
	 * 
	 * @return
	 */
	int getMaxStringLength();

	/**
	 * Sets the maximum string length. The length of the text in this text field
	 * will never exceed this value. Use it to limit the size of the text.
	 * 
	 * @param length
	 */
	void setMaxStringLength(int length);

	/**
	 * Sets the text of this text field. Will truncate if greater than the
	 * maximum string length (see {@link #setMaxStringLength(int)}
	 * 
	 * @param text
	 */
	void setText(T text);

	/**
	 * Sets the text of this text field from a String representation of the
	 * text, appropriate for use in methods like
	 * {@link IGuiCommandSlot#readFromArgs(String[], int)}
	 * 
	 * @param text
	 */
	void setTextAsString(String text);

	/**
	 * Gets the text in this text field
	 * 
	 * @return
	 */
	T getText();

	/**
	 * Returns a string representation of the text is this text field,
	 * appropriate for use in methods like
	 * {@link IGuiCommandSlot#addArgs(java.util.List)}
	 * 
	 * @return
	 */
	String getTextAsString();

	/**
	 * Sets the content filter for this text field. Text which the given
	 * predicate will not accept will not be accepted into this text field.
	 * 
	 * @param filter
	 */
	void setContentFilter(Predicate<T> contentFilter);

	/**
	 * Sets the content filter for this formatted text field. The text accepted
	 * by the content field will be displayed text (not necessarily the same as
	 * the text returned by {@link #getTextAsString()}). For example, in a
	 * formatted text field, this will filter the unformatted text.
	 * 
	 * @param filter
	 */
	void setStringContentFilter(Predicate<String> contentFilter);

	/**
	 * Gets the content filter.
	 * 
	 * @return
	 */
	Predicate<T> getContentFilter();

	/**
	 * Gets whether this text field has focus
	 * 
	 * @return
	 */
	boolean isFocused();

	/**
	 * Sets whether this text field has focus
	 * 
	 * @param focused
	 */
	void setFocused(boolean focused);

}
