package net.earthcomputer.easyeditors.api.util;

import net.minecraft.entity.Entity;

/**
 * A class which renders an entity in a custom-defined way.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 * @param <T>
 *            The type of entity to render
 */
public interface ICustomEntityRenderer<T extends Entity> {

	/**
	 * Renders the entity
	 * 
	 * @param entity
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param mouseX
	 * @param mouseY
	 */
	void renderEntity(T entity, int x, int y, int width, int height, float mouseX, int mouseY);

}
