package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotEntityNBT;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.util.ResourceLocation;

public class SyntaxTestFor extends CommandSyntax {

	private CommandSlotPlayerSelector testingFor;
	private CommandSlotEntityNBT nbt;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		nbt = new CommandSlotEntityNBT(getContext());

		testingFor = new CommandSlotPlayerSelector() {
			@Override
			protected void onSetEntityTo(ResourceLocation newEntityType) {
				nbt.setEntityType(CommandSlotPlayerSelector.ENTITY_ANYTHING.equals(newEntityType) ? null
						: GeneralUtils.getEntityClassFromLocation(newEntityType));
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFOR_TESTINGFOR,
						new CommandSlotRectangle(testingFor, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFOR_NBT,
						new CommandSlotRectangle(nbt, Colors.nbtBox.color)) };
	}

}
