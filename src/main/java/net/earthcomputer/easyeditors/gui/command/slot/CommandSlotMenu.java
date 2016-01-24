package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * A command slot which represents a drop-down menu of a limited number of
 * string options
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotMenu extends GuiCommandSlotImpl {

	private String[] values;
	private int currentValue = 0;

	private FontRenderer fontRenderer;

	/**
	 * Constructs a menu with the given valid values
	 * 
	 * @param values
	 */
	public CommandSlotMenu(String... values) {
		super(calcWidth(Minecraft.getMinecraft().fontRendererObj, values), 12);
		this.values = values;

		this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
	}

	private static int calcWidth(FontRenderer fontRenderer, String[] values) {
		int maxWidth = 0;
		for (String val : values) {
			int w = fontRenderer.getStringWidth(val);
			if (w > maxWidth)
				maxWidth = w;
		}
		return maxWidth + 16;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		boolean foundValue = false;
		for (int i = 0; i < values.length; i++) {
			if (args[index].equals(values[i])) {
				currentValue = i;
				foundValue = true;
				break;
			}
		}
		if (!foundValue)
			throw new CommandSyntaxException();
		return 1;
	}

	@Override
	public void addArgs(List<String> args) {
		args.add(values[currentValue]);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		Gui.drawRect(x, y, x + getWidth() - 12, y + 12, 0xff000000);
		Gui.drawRect(x + getWidth() - 12, y, x + getWidth(), y + 12, 0xff202020);
		drawString(fontRenderer, values[currentValue], x + 2, y + 2, 0xffffffff);
		fontRenderer.drawString("v", x + getWidth() - 9, y + 2, 0xffd0d0d0);
	}

}
