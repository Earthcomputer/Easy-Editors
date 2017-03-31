package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public abstract class CommandSlotSelectFromRegistry<T extends IForgeRegistryEntry<T>>
		extends CommandSlotSelectFromList<ResourceLocation> {

	private T value;
	private IForgeRegistry<T> registry;

	public CommandSlotSelectFromRegistry(String nullLabel, String nullErrorMessage, IForgeRegistry<T> registry) {
		super(nullLabel, nullErrorMessage);
		this.registry = registry;
	}

	@Override
	protected String getDisplayName(ResourceLocation val) {
		return getDisplayNameForRegistryEntry(registry.getValue(val));
	}

	protected abstract String getDisplayNameForRegistryEntry(T val);

	@Override
	protected ResourceLocation readArg(String arg) {
		ResourceLocation loc = new ResourceLocation(arg);
		if (!registry.containsKey(loc)) {
			return null;
		}
		return loc;
	}

	@Override
	protected String writeArg(ResourceLocation arg) {
		return GeneralUtils.resourceLocationToString(arg);
	}

	@Override
	public void setValue(ResourceLocation value) {
		super.setValue(value);
		this.value = registry.getValue(value);
	}

	public T getRegistryValue() {
		return value;
	}

	public void setRegistryValue(T value) {
		setValue(registry.getKey(value));
	}

}
