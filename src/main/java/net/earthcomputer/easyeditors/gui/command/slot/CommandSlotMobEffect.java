package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectEffect;
import net.earthcomputer.easyeditors.gui.command.IEffectSelectorCallback;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommandSlotMobEffect extends CommandSlotHorizontalArrangement implements IEffectSelectorCallback {

	private CommandSlotModifiable<CommandSlotLabel> label;
	private ResourceLocation effect;

	public CommandSlotMobEffect() {
		label = new CommandSlotModifiable<CommandSlotLabel>(
				new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, Translate.GUI_COMMANDEDITOR_NOEFFECT,
						Colors.invalidItemName.color));
		addChild(label);
		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectEffect(Minecraft.getMinecraft().currentScreen, CommandSlotMobEffect.this));
			}
		});
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		ResourceLocation effect = new ResourceLocation(args[index]);
		if (!ForgeRegistries.POTIONS.containsKey(effect)) {
			throw new CommandSyntaxException();
		}
		setEffect(effect);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (effect == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOEFFECTSELECTED);
		}
		args.add(GeneralUtils.resourceLocationToString(effect));
	}

	@Override
	public ResourceLocation getEffect() {
		return effect;
	}

	@Override
	public void setEffect(ResourceLocation effect) {
		this.effect = effect;
		label.setChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format(ForgeRegistries.POTIONS.getValue(effect).getName())));
	}

}
