package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectSound;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommandSlotSound extends CommandSlotHorizontalArrangement implements ICallback<ResourceLocation> {

	private ResourceLocation sound;
	private CommandSlotLabel label;

	public CommandSlotSound() {
		addChild(label = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_NOSOUND, 0xff0000));
		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectSound(Minecraft.getMinecraft().currentScreen, CommandSlotSound.this));
			}
		});
	}

	public ResourceLocation getSound() {
		return sound;
	}

	public void setSound(ResourceLocation sound) {
		this.sound = sound;

		String soundId = GeneralUtils.resourceLocationToString(sound);
		ITextComponent subtitleComponent = Minecraft.getMinecraft().getSoundHandler().getAccessor(sound).getSubtitle();
		String subtitle = subtitleComponent == null ? soundId : subtitleComponent.getFormattedText();
		label.setText(subtitle);
		label.setHoverText(soundId);
		label.setColor(0x000000);
	}

	@Override
	public ResourceLocation getCallbackValue() {
		return getSound();
	}

	@Override
	public void setCallbackValue(ResourceLocation value) {
		setSound(value);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		ResourceLocation sound = new ResourceLocation(args[index]);
		if (!ForgeRegistries.SOUND_EVENTS.containsKey(sound)) {
			throw new CommandSyntaxException();
		}
		setSound(sound);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (sound == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOSOUNDSELECTED);
		}
		args.add(GeneralUtils.resourceLocationToString(sound));
	}

}
