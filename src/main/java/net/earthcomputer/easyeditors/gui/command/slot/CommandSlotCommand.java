package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiCommandSelector;
import net.earthcomputer.easyeditors.gui.command.ICommandEditorCallback;
import net.earthcomputer.easyeditors.gui.command.ICommandSyntax;
import net.earthcomputer.easyeditors.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public class CommandSlotCommand extends CommandSlotVerticalArrangement implements ICommandEditorCallback {

	private String commandName = "";
	ICommandSyntax commandSyntax;

	public CommandSlotCommand() {
		super(new IGuiCommandSlot[0]);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		commandName = args.length <= index ? null : args[index];
		IGuiCommandSlot header = buildHeader(commandName);
		String[] newArgs = new String[args.length - index - 1];
		System.arraycopy(args, index + 1, newArgs, 0, newArgs.length);
		commandSyntax = ICommandSyntax.forCommandName(commandName);
		if (commandSyntax != null) {
			IGuiCommandSlot[] syntaxChildren = commandSyntax.setupCommand();
			children = new IGuiCommandSlot[syntaxChildren.length + 1];
			children[0] = buildHeader(commandName);
			System.arraycopy(syntaxChildren, 0, children, 1, syntaxChildren.length);
		} else {
			children = new IGuiCommandSlot[] { buildHeader(commandName) };
		}
		for (IGuiCommandSlot child : children)
			child.addSizeChangeListener(this);
		recalcSize();
		return super.readFromArgs(args, index + 1) + 1;
	}

	private IGuiCommandSlot buildHeader(String commandName) {
		CommandSlotLabel label = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				commandName.isEmpty() ? I18n.format("gui.commandEditor.noCommand") : commandName,
				Colors.commandName.color);
		if (ICommandSyntax.forCommandName(commandName) == null)
			label.setColor(Minecraft.getMinecraft().fontRendererObj
					.getColorCode(EnumChatFormatting.DARK_RED.toString().charAt(1)));
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
	public void onKeyTyped(char typedChar, int keyCode) {
		for (IGuiCommandSlot child : children)
			child.onKeyTyped(typedChar, keyCode);
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
			for (IGuiCommandSlot child : children)
				child.addSizeChangeListener(this);
			recalcSize();
		}
	}

	public boolean isValid() {
		return commandSyntax == null ? false : commandSyntax.isValid();
	}

}
