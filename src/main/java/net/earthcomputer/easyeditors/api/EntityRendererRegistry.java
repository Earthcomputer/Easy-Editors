package net.earthcomputer.easyeditors.api;

import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.util.ICustomEntityRenderer;
import net.minecraft.entity.Entity;

/**
 * A registry for rendering entities which may not render properly otherwise in
 * the entity selection GUI.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class EntityRendererRegistry {

	private static Map<Predicate<Entity>, ICustomEntityRenderer<? extends Entity>> customRenderers = Maps
			.newLinkedHashMap();

	/**
	 * Registers a custom entity renderer for the given class of entity
	 * 
	 * @param clazz
	 * @param renderer
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> void registerCustomEntityRenderer(Class<T> clazz,
			ICustomEntityRenderer<T> renderer) {
		registerCustomEntityRenderer((Predicate<T>) (Predicate<?>) Predicates.instanceOf(clazz), renderer);
	}

	/**
	 * Registers a custom entity renderer which will render and entity if
	 * predicate applies. The predicate is allowed to assume that T is whatever
	 * subclass of entity it wants - ClassCastExceptions are caught
	 * 
	 * @param predicate
	 * @param renderer
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> void registerCustomEntityRenderer(Predicate<T> predicate,
			ICustomEntityRenderer<T> renderer) {
		customRenderers.put((Predicate<Entity>) predicate, renderer);
	}

	/**
	 * Finds an appropriate custom entity renderer for the given entity
	 * 
	 * @param entity
	 * @return The entity renderer found, or null if none is found
	 */
	public static ICustomEntityRenderer<? extends Entity> findCustomRenderer(Entity entity) {
		for (Map.Entry<Predicate<Entity>, ICustomEntityRenderer<? extends Entity>> entry : customRenderers.entrySet()) {
			try {
				if (entry.getKey().apply(entity))
					return entry.getValue();
			} catch (ClassCastException e) {
			}
		}
		return null;
	}

}
