package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotEntity;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotEntityNBT;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.util.ResourceLocation;

public class SyntaxSummon extends CommandSyntax {

	private CommandSlotEntity entityType;
	private CommandSlotRelativeCoordinate pos;
	private CommandSlotEntityNBT nbt;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		entityType = new CommandSlotEntity(false, true) {
			@Override
			public void setEntity(ResourceLocation entityName) {
				super.setEntity(entityName);
				nbt.setEntityType(GeneralUtils.getEntityClassFromLocation(entityName));
			}
		};

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();
		pos = new CommandSlotRelativeCoordinate();

		nbt = new CommandSlotEntityNBT(getContext());

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SUMMON_ENTITY, entityType),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SUMMON_POS,
						new CommandSlotOptional.Impl(pos, optionalGroup)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SUMMON_NBT, new CommandSlotRectangle(
						new CommandSlotOptional.Impl(nbt, optionalGroup), Colors.nbtBox.color)) };
	}

}
