package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBlock;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCommand;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SyntaxExecute extends CommandSyntax {

	private CommandSlotPlayerSelector entity;
	private CommandSlotRelativeCoordinate origin;
	private CommandSlotCheckbox detect;
	private CommandSlotModifiable modifiableDetectArgs;
	private IGuiCommandSlot detectArgs;
	private CommandSlotRelativeCoordinate detectPos;
	private CommandSlotBlock detectBlock;
	private CommandSlotCommand command;

	private ExecuteContext context;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		context = new ExecuteContext(getContext());
		entity = new CommandSlotPlayerSelector() {
			@Override
			protected void onSetEntityTo(ResourceLocation newEntityType) {
				if (CommandSlotPlayerSelector.ENTITY_ANYTHING.equals(newEntityType)) {
					context.setEntityClass(null);
				} else {
					context.setEntityClass(GeneralUtils.getEntityClassFromLocation(newEntityType));
				}
			}
		};
		origin = new CommandSlotRelativeCoordinate() {
			@Override
			protected void onChanged() {
				if (origin != null) {
					refreshOrigin();
				}
			}
		};
		detect = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_EXECUTE_DETECT) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				if ("detect".equals(args[index])) {
					setChecked(true);
					return 1;
				} else {
					setChecked(false);
					return 0;
				}
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (isChecked()) {
					args.add("detect");
				}
			}

			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					modifiableDetectArgs.setChild(detectArgs);
				} else {
					modifiableDetectArgs.setChild(null);
				}
			}
		};
		modifiableDetectArgs = new CommandSlotModifiable();
		detectPos = new CommandSlotRelativeCoordinate() {
			@Override
			protected void onChanged() {
				if (detectPos != null) {
					refreshOrigin();
				}
			}
		};
		detectBlock = new CommandSlotBlock(true, 2, CommandSlotBlock.COMPONENT_BLOCK,
				CommandSlotBlock.COMPONENT_PROPERTIES);
		detectArgs = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EXECUTE_DETECTPOS, detectPos),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EXECUTE_DETECTBLOCK,
						new CommandSlotRectangle(detectBlock, Colors.itemBox.color)));
		command = new CommandSlotCommand();
		command.detachContext();
		command.setContext(context);
		refreshOrigin();

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EXECUTE_ENTITY,
						Translate.GUI_COMMANDEDITOR_EXECUTE_ENTITY_TOOLTIP,
						new CommandSlotRectangle(entity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EXECUTE_ORIGIN,
						Translate.GUI_COMMANDEDITOR_EXECUTE_ORIGIN_TOOLTIP, origin),
				detect, modifiableDetectArgs, CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EXECUTE_COMMAND,
						new CommandSlotRectangle(command, Colors.commandBox.color)) };
	}

	private void refreshOrigin() {
		if (origin.getXArg().getRelative().isChecked() || origin.getYArg().getRelative().isChecked()
				|| origin.getZArg().getRelative().isChecked()) {
			context.setOrigin(null);
			return;
		}
		double x = origin.getXArg().getTextField().getDoubleValue();
		double y = origin.getYArg().getTextField().getDoubleValue();
		double z = origin.getZArg().getTextField().getDoubleValue();
		if (detectPos.getXArg().getRelative().isChecked()) {
			x += detectPos.getXArg().getTextField().getDoubleValue();
		} else {
			x = detectPos.getXArg().getTextField().getDoubleValue();
		}
		if (detectPos.getYArg().getRelative().isChecked()) {
			y += detectPos.getYArg().getTextField().getDoubleValue();
		} else {
			y = detectPos.getYArg().getTextField().getDoubleValue();
		}
		if (detectPos.getZArg().getRelative().isChecked()) {
			z += detectPos.getZArg().getTextField().getDoubleValue();
		} else {
			z = detectPos.getZArg().getTextField().getDoubleValue();
		}
		context.setOrigin(new BlockPos(x, y, z));
	}

	private static class ExecuteContext extends CommandSlotContext {
		private CommandSlotContext parent;
		private Class<? extends Entity> entityClass;
		private BlockPos origin;

		public ExecuteContext(CommandSlotContext parent) {
			this.parent = parent;
		}

		public void setEntityClass(Class<? extends Entity> entityClass) {
			this.entityClass = entityClass;
		}

		public void setOrigin(BlockPos origin) {
			this.origin = origin;
		}

		@Override
		public World getWorld() {
			return parent.getWorld();
		}

		@Override
		public BlockPos getPos() {
			return origin;
		}

		@Override
		public ICommandSender getSender() {
			return null;
		}

		@Override
		public Class<? extends ICommandSender> getSenderClass() {
			return entityClass;
		}

		@Override
		public boolean isEntity() {
			return true;
		}

		@Override
		public boolean canHoldFormatting() {
			return parent.canHoldFormatting();
		}

		@Override
		public boolean isMouseInBounds(int mouseX, int mouseY) {
			return parent.isMouseInBounds(mouseX, mouseY);
		}

		@Override
		public void ensureXInView(int x) {
			parent.ensureXInView(x);
		}

		@Override
		public void ensureYInView(int y) {
			parent.ensureYInView(y);
		}

		@Override
		public void commandSyntaxError() {
			parent.commandSyntaxError();
		}
	}

}
