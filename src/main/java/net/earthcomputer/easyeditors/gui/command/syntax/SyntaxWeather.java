package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;

public class SyntaxWeather extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		CommandSlotMenu weatherType = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_WEATHER_CLEAR,
				Translate.GUI_COMMANDEDITOR_WEATHER_RAIN, Translate.GUI_COMMANDEDITOR_WEATHER_THUNDER }, "clear",
				"rain", "thunder");
		CommandSlotIntTextField customDuration = new CommandSlotIntTextField(100, 100, 0, 1000000)
				.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_WEATHER_DURATION_CUSTOM_INVALID)
				.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_WEATHER_DURATION_CUSTOM_OUTOFBOUNDS);
		CommandSlotRadioList duration = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_WEATHER_DURATION_NATURAL,
						Translate.GUI_COMMANDEDITOR_WEATHER_DURATION_NATURAL_TOOLTIP),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_WEATHER_DURATION_CUSTOM,
						Translate.GUI_COMMANDEDITOR_WEATHER_DURATION_CUSTOM_TOOLTIP, customDuration)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				return args.length == index ? 0 : 1;
			}

			@Override
			protected boolean shouldCheckIndexOutOfBounds() {
				return false;
			}
		};
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_WEATHER_TYPE,
						Translate.GUI_COMMANDEDITOR_WEATHER_TYPE_TOOLTIP, weatherType),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_WEATHER_DURATION,
						Translate.GUI_COMMANDEDITOR_WEATHER_DURATION_TOOLTIP, duration) };
	}

}
