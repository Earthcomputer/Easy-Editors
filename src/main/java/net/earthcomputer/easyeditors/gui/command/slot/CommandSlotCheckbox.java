package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot which represents a checkbox
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotCheckbox extends GuiCommandSlotImpl {

	private GuiCheckBox checkbox;
	private String hoverText;
	private HoverChecker hoverChecker;

	/**
	 * Constructs an empty checkbox with no text and no hover text
	 */
	public CommandSlotCheckbox() {
		this(null);
	}

	/**
	 * Constructs a checkbox with the specified text and no hover text
	 * 
	 * @param text
	 */
	public CommandSlotCheckbox(String text) {
		this(text, null);
	}

	/**
	 * Constructs a checkbox with the specified text and the specified hover
	 * text. If you don't want text but want hover text, pass null into the
	 * first argument
	 * 
	 * @param text
	 * @param hoverText
	 */
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
	public void addArgs(List<String> args) throws UIInvalidException {
		args.add(String.valueOf(checkbox.isChecked()));
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		checkbox.xPosition = x;
		checkbox.yPosition = y;
		checkbox.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);

		if (hoverText != null) {
			if (!getContext().isMouseInBounds(mouseX, mouseY))
				hoverChecker.resetHoverTimer();
			else if (hoverChecker.checkHover(mouseX, mouseY))
				drawTooltip(mouseX, mouseY, hoverText);
		}
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean checkedBefore = isChecked();
		boolean r = getContext().isMouseInBounds(mouseX, mouseY)
				&& checkbox.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
		if (isChecked() != checkedBefore)
			onChecked(isChecked());
		return r;
	}

	/**
	 * 
	 * @return Whether this checkbox is checked
	 */
	public boolean isChecked() {
		return checkbox.isChecked();
	}

	/**
	 * Sets whether this checkbox is checked
	 * 
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		if (checked != isChecked()) {
			checkbox.setIsChecked(checked);
			onChecked(checked);
		}
	}

	/**
	 * Called when this checkbox is checked or unchecked
	 * 
	 * @param checked
	 */
	protected void onChecked(boolean checked) {
	}

}
