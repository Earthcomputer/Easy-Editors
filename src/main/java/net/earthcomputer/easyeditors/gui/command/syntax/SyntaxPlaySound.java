package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotNumberTextField;
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
	private CommandSlotRelativeCoordinate.WithDefault position;
	private CommandSlotNumberTextField volume;
	private CommandSlotNumberTextField pitch;
	private CommandSlotNumberTextField minVolume;

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

		position = new CommandSlotRelativeCoordinate.WithDefault() {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				int i = index;
				if (args.length == i) {
					getXArg().getTextField().setText("0");
					getXArg().getRelative().setChecked(true);
				} else {
					getXArg().readFromArgs(args, i);
					i++;
				}
				if (args.length == i) {
					getYArg().getTextField().setText("0");
					getYArg().getRelative().setChecked(true);
				} else {
					getYArg().readFromArgs(args, i);
					i++;
				}
				if (args.length == i) {
					getZArg().getTextField().setText("0");
					getZArg().getRelative().setChecked(true);
				} else {
					getZArg().readFromArgs(args, i);
					i++;
				}
				return i - index;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (!isArgRedundant()) {
					getXArg().addArgs(args);
					getYArg().addArgs(args);
					getZArg().addArgs(args);
				} else {
					boolean xDefault = getXArg().getTextField().getDoubleValue() == 0
							&& getXArg().getRelative().isChecked();
					boolean yDefault = getYArg().getTextField().getDoubleValue() == 0
							&& getYArg().getRelative().isChecked();
					boolean zDefault = getZArg().getTextField().getDoubleValue() == 0
							&& getZArg().getRelative().isChecked();
					if (!xDefault || !yDefault || !zDefault) {
						getXArg().addArgs(args);
						if (!yDefault || !zDefault) {
							getYArg().addArgs(args);
							if (!zDefault) {
								getZArg().addArgs(args);
							}
						}
					}
				}
			}

			@Override
			protected boolean isArgRedundant() throws UIInvalidException {
				return volume.getDoubleValue() == 1 && pitch.getDoubleValue() == 1 && minVolume.getDoubleValue() == 0;
			}
		};

		// TODO: convert max to fraction
		volume = new CommandSlotNumberTextField(50, 50, 0, 3.4028234663852886E38D) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setText("1");
					return 0;
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (getDoubleValue() == 1 && pitch.getDoubleValue() == 1 && minVolume.getDoubleValue() == 0) {
					return;
				}
				super.addArgs(args);
			}
		};
		volume.setText("1");

		pitch = new CommandSlotNumberTextField(50, 50, 0, 2) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setText("1");
					return 0;
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (getDoubleValue() == 1 && minVolume.getDoubleValue() == 0) {
					return;
				}
				super.addArgs(args);
			}
		};
		pitch.setText("1");

		minVolume = new CommandSlotNumberTextField(50, 50, 0, 1) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setText("0");
					return 0;
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				checkValid();
				if (getDoubleValue() == 0) {
					return;
				}
				super.addArgs(args);
			}
		};
		minVolume.setText("0");

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_SOUND, sound),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_CATEGORY, category),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_TARGET,
						new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_POS, position),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_VOLUME, volume),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_PITCH, pitch),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYSOUND_MINVOLUME, minVolume) };
	}

}
