package net.earthcomputer.easyeditors.api.util;

import net.minecraft.entity.Entity;

public interface ICustomEntityRenderer<T extends Entity> {

	void renderEntity(T entity, int x, int y, int width, int height, float baseScale, int mouseX);

}
