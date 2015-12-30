package net.earthcomputer.easyeditors.gui;

/**
 * An interface for use with GuiColorPicker. When the user has chosen a color,
 * setColor will be invoked
 * 
 * @author Earthcomputer
 *
 */
public interface IColorPickerCallback {

	/**
	 * 
	 * @return The color, in ARGB format
	 */
	int getColor();

	/**
	 * Sets the color, in ARGB format
	 * 
	 * @param color
	 */
	void setColor(int color);

}
