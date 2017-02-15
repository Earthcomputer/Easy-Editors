package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.util.ResourceLocation;

/**
 * An interface for use with the mob effect selector. Once the user has selected
 * a mob effect, setEffect will be invoked
 * 
 * @author Earthcomputer
 *
 */
public interface IEffectSelectorCallback {

	/**
	 * Gets the effect to initialize the effect selection GUI with
	 * 
	 * @return
	 */
	ResourceLocation getEffect();

	/**
	 * Callback to set the effect once the user has used to selection GUI to
	 * select one
	 * 
	 * @param effect
	 */
	void setEffect(ResourceLocation effect);

}
