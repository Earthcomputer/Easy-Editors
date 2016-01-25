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
	private boolean expanded = false;

	private int x;
	private int y;
	private boolean expandUpwards;

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
		this.x = x;
		this.y = y;
		Gui.drawRect(x, y, x + getWidth() - 12, y + 12, 0xff000000);
		Gui.drawRect(x + getWidth() - 12, y, x + getWidth(), y + 12, 0xff202020);
		drawString(fontRenderer, values[currentValue], x + 2, y + 2, 0xffffff);
		fontRenderer.drawString("v", x + getWidth() - 9, y + 2, 0xd0d0d0);
	}

	@Override
	public void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.drawForeground(x, y, mouseX, mouseY, partialTicks);

		if (expanded) {
			expandUpwards = y + 12 + values.length * 12 >= Minecraft.getMinecraft().currentScreen.height;
			int top = expandUpwards ? y - values.length * 12 : y + 12;
			for (int i = 0; i < values.length; i++) {
				Gui.drawRect(x, top + i * 12, x + getWidth() - 12, top + i * 12 + 12,
						i % 2 == 0 ? 0xe0808080 : 0xe0606060);
				drawString(fontRenderer, values[i], x + 2, top + i * 12 + 2, 0xffffff);
			}
		}
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (expanded) {
				expanded = false;
				int top = expandUpwards ? y - values.length * 12 : y + 12;
				if (mouseX >= x && mouseX < x + getWidth() && mouseY >= top && mouseY < top + values.length * 12) {
					currentValue = (mouseY - top) / 12;
					return true;
				}
			} else {
				if (mouseX >= x + getWidth() - 12 && mouseX < x + getWidth() && mouseY >= y && mouseY < y + 12) {
					expanded = true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp) {
		return expanded;
	}

}
