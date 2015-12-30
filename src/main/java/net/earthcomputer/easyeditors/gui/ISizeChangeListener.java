package net.earthcomputer.easyeditors.gui;

/**
 * An interface with the capability of listening to changes in size in
 * IGuiCommandSlots.
 * 
 * @author Earthcomputer
 *
 */
public interface ISizeChangeListener {

	/**
	 * Called when the IGuiCommandSlot's width changes
	 * 
	 * @param oldWidth
	 * @param newWidth
	 */
	void onWidthChange(int oldWidth, int newWidth);

	/**
	 * Called when the IGuiCommandSlot's height changes
	 * 
	 * @param oldHeight
	 * @param newHeight
	 */
	void onHeightChange(int oldHeight, int newHeight);

}
