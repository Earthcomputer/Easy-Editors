package net.earthcomputer.easyeditors.api;

import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.util.ICustomEntityRenderer;
import net.minecraft.entity.Entity;

public class EntityRendererRegistry {

	private static Map<Predicate<Entity>, ICustomEntityRenderer<? extends Entity>> customRenderers = Maps
			.newLinkedHashMap();

	@SuppressWarnings("unchecked")
	public static <T extends Entity> void registerCustomEntityRenderer(Class<T> clazz,
			ICustomEntityRenderer<T> renderer) {
		registerCustomEntityRenderer((Predicate<T>) (Predicate<?>) Predicates.instanceOf(clazz), renderer);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> void registerCustomEntityRenderer(Predicate<T> predicate,
			ICustomEntityRenderer<T> renderer) {
		customRenderers.put((Predicate<Entity>) predicate, renderer);
	}

	public static ICustomEntityRenderer<? extends Entity> findCustomRenderer(Entity entity) {
		for (Map.Entry<Predicate<Entity>, ICustomEntityRenderer<? extends Entity>> entry : customRenderers.entrySet()) {
			if (entry.getKey().apply(entity))
				return entry.getValue();
		}
		return null;
	}

}
