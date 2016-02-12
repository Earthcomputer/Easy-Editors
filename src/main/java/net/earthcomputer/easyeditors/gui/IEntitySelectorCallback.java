package net.earthcomputer.easyeditors.gui;

/**
 * An interface for use with GuiSelectEntity. When the user has chosen an
 * entity, setEntity will be invoked
 * 
 * @author Earthcomputer
 *
 */
public interface IEntitySelectorCallback {

	/**
	 * 
	 * @return The internal entity name of the currently selected entity
	 */
	String getEntity();

	/**
	 * Sets the selected entity by internal entity name
	 * 
	 * @param entityName
	 */
	void setEntity(String entityName);

}
