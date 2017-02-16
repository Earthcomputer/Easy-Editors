package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotInventorySlot;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotItemStack;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.util.ResourceLocation;

public class SyntaxReplaceItem extends CommandSyntax {

	private CommandSlotRadioList target;
	private CommandSlotRelativeCoordinate targetBlock;
	private CommandSlotPlayerSelector targetEntity;
	private CommandSlotInventorySlot slot;
	private CommandSlotItemStack stack;
	private ResourceLocation targetEntitySelectorType;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		slot = new CommandSlotInventorySlot();
		targetBlock = new CommandSlotRelativeCoordinate(Colors.miscBigBoxLabel.color);
		targetEntity = new CommandSlotPlayerSelector() {
			@Override
			protected void onSetEntityTo(ResourceLocation newEntityType) {
				slot.setEntityType(newEntityType);
				targetEntitySelectorType = newEntityType;
			}
		};
		target = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_TARGET_BLOCK,
						Colors.miscBigBoxLabel.color, targetBlock),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_TARGET_ENTITY,
						Colors.miscBigBoxLabel.color,
						new CommandSlotRectangle(targetEntity, Colors.playerSelectorBox.color))) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				// since the index has been offset in readFromArgs, we need to
				// offset it back
				String arg = args[index - 1];
				if ("block".equals(arg)) {
					return 0;
				} else if ("entity".equals(arg)) {
					return 1;
				} else {
					throw new CommandSyntaxException();
				}
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				return super.readFromArgs(args, index + 1) + 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				switch (getSelectedIndex()) {
				case 0:
					args.add("block");
					break;
				case 1:
					args.add("entity");
					break;
				default:
					throw new IllegalStateException();
				}
				super.addArgs(args);
			}

			@Override
			protected void onValueChanged() {
				switch (getSelectedIndex()) {
				case 0:
					slot.setEntityType(null);
					break;
				case 1:
					slot.setEntityType(targetEntitySelectorType);
					break;
				default:
					throw new IllegalStateException();
				}
			}
		};
		stack = new CommandSlotItemStack(1, CommandSlotItemStack.COMPONENT_ITEM,
				CommandSlotItemStack.COMPONENT_STACK_SIZE, CommandSlotItemStack.COMPONENT_DAMAGE,
				CommandSlotItemStack.COMPONENT_NBT);
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_TARGET,
						Translate.GUI_COMMANDEDITOR_REPLACEITEM_TARGET_TOOLTIP,
						new CommandSlotRectangle(target, Colors.miscBigBoxBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_SLOT,
						Translate.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_TOOLTIP, slot),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_ITEM,
						Translate.GUI_COMMANDEDITOR_REPLACEITEM_ITEM_TOOLTIP,
						new CommandSlotRectangle(stack, Colors.itemBox.color)) };
	}

}
