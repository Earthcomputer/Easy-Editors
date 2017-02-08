package net.earthcomputer.easyeditors.gui;

import net.minecraft.util.ResourceLocation;

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
	ResourceLocation getEntity();

	/**
	 * Sets the selected entity by internal entity name
	 * 
	 * @param entityName
	 */
	void setEntity(ResourceLocation entityName);

}
