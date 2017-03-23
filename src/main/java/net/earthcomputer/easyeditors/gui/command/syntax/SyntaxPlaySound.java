package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotNumberTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotSound;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundCategory;

public class SyntaxPlaySound extends CommandSyntax {

	private CommandSlotSound sound;
	private CommandSlotMenu category;
	private CommandSlotPlayerSelector target;
	private CommandSlotRelativeCoordinate position;
	private CommandSlotNumberTextField.Optional volume;
	private CommandSlotNumberTextField.Optional pitch;
	private CommandSlotNumberTextField.Optional minVolume;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		sound = new CommandSlotSound();

		SoundCategory[] soundCategories = SoundCategory.values();
		String[] soundCategoryIds = new String[soundCategories.length];
		String[] soundCategoryNames = new String[soundCategories.length];
		for (int i = 0; i < soundCategories.length; i++) {
			soundCategoryIds[i] = soundCategories[i].getName();
			soundCategoryNames[i] = I18n.format("soundCategory." + soundCategoryIds[i]);
		}
		category = new CommandSlotMenu(soundCategoryNames, soundCategoryIds);

		target = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY);

		position = new CommandSlotRelativeCoordinate();

		// TODO: convert max to fraction
		volume = new CommandSlotNumberTextField.Optional(50, 50, 0, 3.4028234663852886E38D, 1);

		pitch = new CommandSlotNumberTextField.Optional(50, 50, 0, 2, 1);

		minVolume = new CommandSlotNumberTextField.Optional(50, 50, 0, 1, 0);

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_SOUND, sound),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_CATEGORY, category),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_TARGET,
						new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_POS,
						new CommandSlotOptional.Impl(position, optionalGroup)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_VOLUME,
						new CommandSlotOptional.Impl(volume, optionalGroup)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_PITCH,
						new CommandSlotOptional.Impl(pitch, optionalGroup)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_MINVOLUME,
						new CommandSlotOptional.Impl(minVolume, optionalGroup)) };
	}

}
