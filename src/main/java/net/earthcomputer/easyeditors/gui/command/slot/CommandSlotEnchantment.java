package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.command.GuiSelectEnchantment;
import net.earthcomputer.easyeditors.gui.command.IEnchantmentSelectorCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;

/**
 * A command slot which represents a selection of an enchantment
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotEnchantment extends CommandSlotHorizontalArrangement implements IEnchantmentSelectorCallback {

	private int enchantmentId = -1;
	private CommandSlotLabel enchantmentName;
	private CommandSlotIntTextField enchantmentLevel;

	public CommandSlotEnchantment() {
		addChild(enchantmentName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format("gui.commandEditor.noEnchantment"), 0xff0000));
		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectEnchantment(Minecraft.getMinecraft().currentScreen, CommandSlotEnchantment.this));
			}
		});
		addChild(enchantmentLevel = new CommandSlotIntTextField(30, 30, 1, 100));
		enchantmentLevel.setText("1");
	}

	@Override
	public int getEnchantment() {
		return enchantmentId;
	}

	@Override
	public void setEnchantment(int id) {
		if (id != this.enchantmentId) {
			this.enchantmentId = id;
			enchantmentName.setColor(0);
			enchantmentName.setText(I18n.format(Enchantment.getEnchantmentById(id).getName()));
			onChanged();
		}
	}

	/**
	 * 
	 * @return The enchantment level
	 */
	public int getLevel() {
		return enchantmentLevel.getIntValue();
	}

	/**
	 * Sets the enchantment level
	 * 
	 * @param level
	 */
	public void setLevel(int level) {
		if (level >= 1 && level <= 100 && level != enchantmentLevel.getIntValue()) {
			enchantmentLevel.setText(String.valueOf(level));
			onChanged();
		}
	}

	/**
	 * Called when either the enchantment ID or the enchantment level is changed
	 */
	protected void onChanged() {
	}

	/**
	 * 
	 * @return Whether this has a valid set of child components
	 */
	public boolean isValid() {
		return Enchantment.getEnchantmentById(enchantmentId) != null && enchantmentLevel.isValid();
	}

}
