package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiCommandSelector;
import net.earthcomputer.easyeditors.gui.command.ICommandEditorCallback;
import net.earthcomputer.easyeditors.gui.command.syntax.ICommandSyntax;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * A command slot which represents a command
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotCommand extends CommandSlotVerticalArrangement implements ICommandEditorCallback {

	private String commandName = "";
	ICommandSyntax commandSyntax;

	public CommandSlotCommand() {
		super(new IGuiCommandSlot[0]);
	}

	@Override
	public int readFromArgs(String[] args, int index) {
		commandName = args.length <= index ? null : args[index];
		IGuiCommandSlot header = buildHeader(commandName);
		String[] newArgs = new String[args.length - index - 1];
		System.arraycopy(args, index + 1, newArgs, 0, newArgs.length);
		commandSyntax = ICommandSyntax.forCommandName(commandName);
		clearChildren();
		addChild(header);
		if (commandSyntax != null) {
			addChildren(commandSyntax.setupCommand());
		}
		try {
			return super.readFromArgs(args, index + 1) + 1;
		} catch (CommandSyntaxException e) {
			clearChildren();
			addChild(header);
			addChildren(commandSyntax.setupCommand());
			return 1;
		}
	}

	private IGuiCommandSlot buildHeader(String commandName) {
		CommandSlotLabel label = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				commandName.isEmpty() ? I18n.format("gui.commandEditor.noCommand") : commandName,
				Colors.commandName.color);
		if (ICommandSyntax.forCommandName(commandName) == null)
			label.setColor(Colors.invalidCommandName.color);
		return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.commandLabel"), label,
				new CommandSlotButton(20, 20, "...") {
					@Override
					public void onPress() {
						Minecraft.getMinecraft().displayGuiScreen(new GuiCommandSelector(
								Minecraft.getMinecraft().currentScreen, CommandSlotCommand.this));
					}
				});
	}

	@Override
	public void addArgs(List<String> args) {
		args.add(commandName);
		super.addArgs(args);
	}

	@Override
	public String getCommand() {
		return commandName;
	}

	@Override
	public void setCommand(String rawCommand) {
		if (!rawCommand.equals(commandName)) {
			commandName = rawCommand;
			commandSyntax = ICommandSyntax.forCommandName(rawCommand);
			clearChildren();
			addChild(buildHeader(rawCommand));
			addChildren(commandSyntax.setupCommand());
		}
	}

	/**
	 * 
	 * @return Whether the command is valid
	 */
	public boolean isValid() {
		return commandSyntax == null ? false : commandSyntax.isValid();
	}

}
