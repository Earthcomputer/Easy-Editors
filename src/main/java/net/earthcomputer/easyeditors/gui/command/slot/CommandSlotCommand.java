package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiCommandSelector;
import net.earthcomputer.easyeditors.gui.command.ICommandEditorCallback;
import net.earthcomputer.easyeditors.gui.command.ICommandSyntax;
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
		if (commandSyntax != null) {
			IGuiCommandSlot[] syntaxChildren = commandSyntax.setupCommand();
			children = new IGuiCommandSlot[syntaxChildren.length + 1];
			children[0] = header;
			System.arraycopy(syntaxChildren, 0, children, 1, syntaxChildren.length);
		} else {
			children = new IGuiCommandSlot[] { buildHeader(commandName) };
		}
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
		}
		recalcSize();
		try {
			return super.readFromArgs(args, index + 1) + 1;
		} catch (CommandSyntaxException e) {
			IGuiCommandSlot[] syntaxChildren = commandSyntax.setupCommand();
			for (IGuiCommandSlot child : syntaxChildren) {
				child.addSizeChangeListener(this);
				child.setParent(this);
			}
			children = new IGuiCommandSlot[syntaxChildren.length + 1];
			children[0] = header;
			System.arraycopy(syntaxChildren, 0, children, 1, syntaxChildren.length);
			recalcSize();
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
		for (IGuiCommandSlot child : children) {
			child.addArgs(args);
		}
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		boolean r = false;
		for (IGuiCommandSlot child : children)
			if (child.onKeyTyped(typedChar, keyCode))
				r = true;
		return r;
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		for (IGuiCommandSlot child : children)
			child.onMouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (IGuiCommandSlot child : children)
			child.onMouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		for (IGuiCommandSlot child : children)
			child.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public String getCommand() {
		return commandName;
	}

	@Override
	public void setCommand(String rawCommand) {
		if (!rawCommand.equals(commandName)) {
			IGuiCommandSlot header = buildHeader(rawCommand);
			commandSyntax = ICommandSyntax.forCommandName(rawCommand);
			IGuiCommandSlot[] syntaxChildren = commandSyntax.setupCommand();
			children = new IGuiCommandSlot[syntaxChildren.length + 1];
			children[0] = header;
			System.arraycopy(syntaxChildren, 0, children, 1, syntaxChildren.length);
			for (IGuiCommandSlot child : children) {
				child.addSizeChangeListener(this);
				child.setParent(this);
			}
			recalcSize();
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
