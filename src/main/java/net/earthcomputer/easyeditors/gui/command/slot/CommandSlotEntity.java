package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.IEntitySelectorCallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * A command slot which represents a selection of an entity
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotEntity extends CommandSlotHorizontalArrangement implements IEntitySelectorCallback {

	private boolean includePlayer;
	private boolean includeLightning;
	private String[] additionalOptions;

	private CommandSlotLabel entityLabel;
	private String entity;

	public CommandSlotEntity() {
		this(false, false);
	}

	public CommandSlotEntity(boolean includePlayer, boolean includeLightning, String... additionalOptions) {
		this.includePlayer = includePlayer;
		this.includeLightning = includeLightning;
		this.additionalOptions = additionalOptions;
		addChild(entityLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format("gui.commandEditor.noEntity"), 0xff0000));
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
		setEntity(args[index]);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) {
		args.add(entity);
	}

	@Override
	public String getEntity() {
		return entity;
	}

	@Override
	public void setEntity(String entityName) {
		this.entity = entityName;
		entityLabel.setText(GuiSelectEntity.getEntityName(entityName));
		entityLabel.setColor(0);
	}

	/**
	 * 
	 * @return Whether this would write to arguments in a valid way when the
	 *         done button is clicked
	 */
	public boolean isValid() {
		return entity != null;
	}

}
