package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotEntityNBT;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class SyntaxTestFor extends CommandSyntax {

	private CommandSlotPlayerSelector testingFor;
	private CommandSlotEntityNBT nbt;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		nbt = new CommandSlotEntityNBT();

		testingFor = new CommandSlotPlayerSelector() {
			@Override
			protected void onSetEntityTo(ResourceLocation newEntityType) {
				Class<? extends Entity> entityClass;
				if (CommandSlotPlayerSelector.ENTITY_ANYTHING.equals(newEntityType)) {
					entityClass = null;
				} else if (GuiSelectEntity.PLAYER.equals(newEntityType)) {
					entityClass = EntityPlayer.class;
				} else if (EntityList.LIGHTNING_BOLT.equals(newEntityType)) {
					entityClass = EntityLightningBolt.class;
				} else {
					entityClass = EntityList.getClass(newEntityType);
				}
				nbt.setEntityType(entityClass);
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFOR_TESTINGFOR,
						new CommandSlotRectangle(testingFor, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFOR_NBT,
						new CommandSlotRectangle(nbt, Colors.nbtBox.color)) };
	}

}
