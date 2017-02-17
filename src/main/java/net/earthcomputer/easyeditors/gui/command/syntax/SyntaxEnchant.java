package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotEnchantment;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SyntaxEnchant extends CommandSyntax {

	private CommandSlotPlayerSelector target;
	private CommandSlotEnchantment enchantment;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		target = new CommandSlotPlayerSelector();

		enchantment = new CommandSlotEnchantment(true) {
			@Override
			public void checkValid() throws UIInvalidException {
				super.checkValid();
				Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(getEnchantment());
				int level = getLevel();
				if (level < enchantment.getMinLevel() || level > enchantment.getMaxLevel()) {
					throw new UIInvalidException(
							Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ENCHANTMENTINVALID_LEVELOUTOFBOUNDS,
							enchantment.getMinLevel(), enchantment.getMaxLevel());
				}
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ENCHANT_TARGET,
						Translate.GUI_COMMANDEDITOR_ENCHANT_TARGET_TOOLTIP,
						new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ENCHANT_ENCHANTMENT,
						Translate.GUI_COMMANDEDITOR_ENCHANT_ENCHANTMENT_TOOLTIP, enchantment) };
	}

}
