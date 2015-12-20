package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class CommandSlotLabel extends GuiCommandSlotImpl {
	
	private FontRenderer fontRenderer;
	private String text;
	private int color;

	public CommandSlotLabel(FontRenderer fontRenderer, String text) {
		this(fontRenderer, text, Colors.label.color);
	}

	public CommandSlotLabel(FontRenderer fontRenderer, String text, int color) {
		super(fontRenderer.getStringWidth(text), fontRenderer.FONT_HEIGHT);
		this.fontRenderer = fontRenderer;
		this.text = text;
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int readFromArgs(String[] args, int index) {
		return 0;
	}

	@Override
	public void addArgs(List<String> args) {
	}

	public String getText() {
		return text;
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		fontRenderer.drawString(text, x, y, color);
	}

	public static IGuiCommandSlot createLabel(String text, IGuiCommandSlot... describing) {
		return createLabel(text, Colors.label.color, describing);
	}
	
	public static IGuiCommandSlot createLabel(String text, int color, IGuiCommandSlot... describing) {
		IGuiCommandSlot[] slots = new IGuiCommandSlot[describing.length + 1];
		slots[0] = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, text, color);
		System.arraycopy(describing, 0, slots, 1, describing.length);
		return new CommandSlotHorizontalArrangement(slots);
	}

}
