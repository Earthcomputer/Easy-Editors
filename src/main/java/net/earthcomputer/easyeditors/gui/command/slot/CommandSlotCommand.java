package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectCommand;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;

/**
 * A command slot which represents a command
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotCommand extends CommandSlotVerticalArrangement implements ICallback<String> {

	private String commandName = "";
	private CommandSyntax commandSyntax;

	@Override
	public int readFromArgs(String[] args, int index) {
		commandName = args.length <= index ? null : args[index];
		IGuiCommandSlot header = buildHeader(commandName);
		String[] newArgs = new String[args.length - index - 1];
		System.arraycopy(args, index + 1, newArgs, 0, newArgs.length);
		commandSyntax = CommandSyntax.forCommandName(commandName, getContext());
		clearChildren();
		addChild(header);
		if (commandSyntax != null) {
			addChildren(commandSyntax.setupCommand());
		}
		try {
			return super.readFromArgs(args, index + 1) + 1;
		} catch (CommandSyntaxException e) {
			GeneralUtils.logStackTrace(EasyEditorsApi.logger, e);
			getContext().commandSyntaxError();
			clearChildren();
			addChild(header);
			addChildren(commandSyntax.setupCommand());
			return 1;
		}
	}

	private IGuiCommandSlot buildHeader(String commandName) {
		CommandSlotLabel label = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				commandName.isEmpty() ? Translate.GUI_COMMANDEDITOR_NOCOMMAND : commandName, Colors.commandName.color);
		if (CommandSyntax.forCommandName(commandName, getContext()) == null) {
			label.setColor(Colors.invalidCommandName.color);
			getContext().commandSyntaxError();
		}
		return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_COMMANDLABEL, label,
				new CommandSlotButton(20, 20, "...") {
					@Override
					public void onPress() {
						Minecraft.getMinecraft().displayGuiScreen(new GuiSelectCommand(
								Minecraft.getMinecraft().currentScreen, CommandSlotCommand.this, getContext()));
					}
				});
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (commandSyntax == null)
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOCOMMANDCHOSEN);
		commandSyntax.checkValid();
		args.add(commandName);
		super.addArgs(args);
	}

	public String getCommand() {
		return commandName;
	}

	public void setCommand(String rawCommand) {
		if (!rawCommand.equals(commandName)) {
			commandName = rawCommand;
			commandSyntax = CommandSyntax.forCommandName(rawCommand, getContext());
			clearChildren();
			addChild(buildHeader(rawCommand));
			addChildren(commandSyntax.setupCommand());
		}
	}

	@Override
	public String getCallbackValue() {
		return getCommand();
	}

	@Override
	public void setCallbackValue(String value) {
		setCommand(value);
	}

}
