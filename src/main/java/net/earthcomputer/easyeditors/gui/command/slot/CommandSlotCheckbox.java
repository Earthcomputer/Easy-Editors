package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.HoverChecker;

public class CommandSlotCheckbox extends GuiCommandSlotImpl {

	private GuiCheckBox checkbox;
	private String hoverText;
	private HoverChecker hoverChecker;

	public CommandSlotCheckbox() {
		this(null);
	}

	public CommandSlotCheckbox(String text) {
		this(text, null);
	}

	public CommandSlotCheckbox(String text, String hoverText) {
		super(text == null || text.isEmpty() ? 11 : 13 + Minecraft.getMinecraft().fontRendererObj.getStringWidth(text),
				11);
		this.checkbox = new GuiCheckBox(0, 0, 0, text == null ? "" : text, false);
		if (hoverText != null)
			this.hoverChecker = new HoverChecker(checkbox, 1000);
		this.hoverText = hoverText;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		checkbox.setIsChecked(Boolean.parseBoolean(args[index]));
		return 1;
	}

	@Override
	public void addArgs(List<String> args) {
		args.add(String.valueOf(checkbox.isChecked()));
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		checkbox.xPosition = x;
		checkbox.yPosition = y;
		checkbox.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);

		if (hoverText != null && hoverChecker.checkHover(mouseX, mouseY))
			drawTooltip(mouseX, mouseY, hoverText);
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		return checkbox.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	public boolean isChecked() {
		return checkbox.isChecked();
	}

	public void setChecked(boolean checked) {
		checkbox.setIsChecked(checked);
	}

}
