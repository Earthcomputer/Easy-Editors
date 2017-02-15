package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.IEntitySelectorCallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * A command slot which represents a selection of an entity
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotEntity extends CommandSlotHorizontalArrangement implements IEntitySelectorCallback {

	private boolean includePlayer;
	private boolean includeLightning;
	private ResourceLocation[] additionalOptions;

	private CommandSlotLabel entityLabel;
	private ResourceLocation entity;

	public CommandSlotEntity() {
		this(false, false);
	}

	public CommandSlotEntity(boolean includePlayer, boolean includeLightning, ResourceLocation... additionalOptions) {
		this.includePlayer = includePlayer;
		this.includeLightning = includeLightning;
		this.additionalOptions = additionalOptions;
		addChild(entityLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_NOENTITY, 0xff0000));
		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft()
						.displayGuiScreen(new GuiSelectEntity(Minecraft.getMinecraft().currentScreen,
								CommandSlotEntity.this, CommandSlotEntity.this.includePlayer,
								CommandSlotEntity.this.includeLightning, CommandSlotEntity.this.additionalOptions));
			}
		});
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		setEntity(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(args[index])).delegate.name());
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		args.add(GeneralUtils.resourceLocationToString(entity));
	}

	@Override
	public ResourceLocation getEntity() {
		return entity;
	}

	@Override
	public void setEntity(ResourceLocation entityName) {
		this.entity = entityName;
		entityLabel.setText(GuiSelectEntity.getEntityName(entityName));
		entityLabel.setColor(0);
	}

	/**
	 * 
	 * @throws UIInvalidException
	 *             - when this doesn't have a valid set of child components
	 */
	public void checkValid() throws UIInvalidException {
		if (entity == null)
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOENTITYSELECTED);
	}

}
