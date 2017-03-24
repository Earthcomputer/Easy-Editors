package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.GuiCycleButton;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;

public class CommandSlotCycleButton extends GuiCommandSlotImpl {

	private GuiCycleButton cycleButton;

	public CommandSlotCycleButton(int width, int height, String... values) {
		this(width, height, values, values);
	}

	public CommandSlotCycleButton(int width, int height, String[] displayValues, String... actualValues) {
		super(width, height);
		cycleButton = new GuiCycleButton(-1, 0, 0, width, height, displayValues, actualValues);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		if (!cycleButton.isValidValue(args[index])) {
			throw new CommandSyntaxException();
		}
		cycleButton.setCurrentValue(args[index]);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		args.add(cycleButton.getCurrentValue());
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		cycleButton.xPosition = x;
		cycleButton.yPosition = y;
		cycleButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		return cycleButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	public int getCurrentIndex() {
		return cycleButton.getCurrentIndex();
	}

	public String getCurrentValue() {
		return cycleButton.getCurrentValue();
	}

	public void setCurrentIndex(int index) {
		cycleButton.setCurrentIndex(index);
	}

	public void setCurrentValue(String value) {
		cycleButton.setCurrentValue(value);
	}

	public static class Optional extends CommandSlotCycleButton implements IOptionalCommandSlot {
		private String defaultValue;

		public Optional(int width, int height, String defaultValue, String... values) {
			super(width, height, values);
			this.defaultValue = defaultValue;
		}

		public Optional(int width, int height, String defaultValue, String[] displayValues, String... actualValues) {
			super(width, height, displayValues, actualValues);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean isDefault() throws UIInvalidException {
			return getCurrentValue().equals(defaultValue);
		}

		@Override
		public void setToDefault() {
			setCurrentValue(defaultValue);
		}
	}

}
