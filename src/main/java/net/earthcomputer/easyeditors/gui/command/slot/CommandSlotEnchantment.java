package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.command.GuiSelectEnchantment;
import net.earthcomputer.easyeditors.gui.command.IEnchantmentSelectorCallback;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * A command slot which represents a selection of an enchantment
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotEnchantment extends CommandSlotHorizontalArrangement implements IEnchantmentSelectorCallback {

	private ResourceLocation enchantmentName = null;
	private CommandSlotLabel enchantmentNameLabel;
	private CommandSlotIntTextField enchantmentLevel;

	public CommandSlotEnchantment() {
		addChild(enchantmentNameLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
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
		enchantmentLevel.setNumberInvalidMessage("gui.commandEditor.playerSelector.enchantmentInvalid.levelInvalid")
				.setOutOfBoundsMessage("gui.commandEditor.playerSelector.enchantmentInvalid.levelOutOfBounds");
	}

	@Override
	public ResourceLocation getEnchantment() {
		return enchantmentName;
	}

	@Override
	public void setEnchantment(ResourceLocation id) {
		if (!id.equals(this.enchantmentName)) {
			this.enchantmentName = id;
			enchantmentNameLabel.setColor(0);
			enchantmentNameLabel.setText(I18n.format(Enchantment.getEnchantmentByLocation(id.toString()).getName()));
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
	 * @throws UIInvalidException
	 *             - when this doesn't have a valid set of child components
	 */
	public void checkValid() throws UIInvalidException {
		if (!ForgeRegistries.ENCHANTMENTS.containsKey(enchantmentName))
			throw new UIInvalidException("gui.commandEditor.enchantmentInvalid.noEnchantment");
		enchantmentLevel.checkValid();
	}

}
