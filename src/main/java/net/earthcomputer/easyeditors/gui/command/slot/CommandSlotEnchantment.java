package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectEnchantment;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
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
public class CommandSlotEnchantment extends CommandSlotHorizontalArrangement implements ICallback<ResourceLocation> {

	private ResourceLocation enchantmentName = null;
	private CommandSlotLabel enchantmentNameLabel;
	private CommandSlotIntTextField enchantmentLevel;
	private boolean levelOptional;

	public CommandSlotEnchantment() {
		this(false);
	}

	public CommandSlotEnchantment(boolean levelOptional) {
		addChild(enchantmentNameLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_NOENCHANTMENT, 0xff0000));
		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectEnchantment(Minecraft.getMinecraft().currentScreen, CommandSlotEnchantment.this));
			}
		});
		addChild(enchantmentLevel = new CommandSlotIntTextField(30, 30, 1, 100));
		enchantmentLevel.setText("1");
		enchantmentLevel
				.setNumberInvalidMessage(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ENCHANTMENTINVALID_LEVELINVALID)
				.setOutOfBoundsMessage(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ENCHANTMENTINVALID_LEVELOUTOFBOUNDS);
		this.levelOptional = levelOptional;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		ResourceLocation enchantment = null;
		try {
			Enchantment ench = Enchantment.getEnchantmentByID(Integer.parseInt(args[index]));
			if (ench != null) {
				enchantment = ench.delegate.name();
			}
		} catch (NumberFormatException e) {
			// ignore
		}
		if (enchantment == null) {
			enchantment = new ResourceLocation(args[index]);
		}
		if (!ForgeRegistries.ENCHANTMENTS.containsKey(enchantment)) {
			throw new CommandSyntaxException();
		}
		setEnchantment(enchantment);

		index++;
		if (args.length == index) {
			if (!levelOptional) {
				throw new CommandSyntaxException();
			}
			setLevel(1);
			return 1;
		}

		try {
			setLevel(Integer.parseInt(args[index]));
		} catch (NumberFormatException e) {
			throw new CommandSyntaxException();
		}

		return 2;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		args.add(GeneralUtils.resourceLocationToString(enchantmentName));
		if (!levelOptional || getLevel() != 1) {
			args.add(String.valueOf(getLevel()));
		}
	}

	public ResourceLocation getEnchantment() {
		return enchantmentName;
	}

	public void setEnchantment(ResourceLocation id) {
		if (!id.equals(this.enchantmentName)) {
			this.enchantmentName = id;
			enchantmentNameLabel.setColor(0);
			enchantmentNameLabel.setText(I18n.format(Enchantment.getEnchantmentByLocation(id.toString()).getName()));
			onChanged();
		}
	}

	@Override
	public ResourceLocation getCallbackValue() {
		return getEnchantment();
	}

	@Override
	public void setCallbackValue(ResourceLocation value) {
		setEnchantment(value);
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
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_ENCHANTMENTINVALID_NOENCHANTMENT);
		enchantmentLevel.checkValid();
	}

}
