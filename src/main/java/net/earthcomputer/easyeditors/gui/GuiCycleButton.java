package net.earthcomputer.easyeditors.gui;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiCycleButton extends GuiButton {

	private String[] displayValues;
	private String[] actualValues;
	private int currentValue;

	public GuiCycleButton(int id, int x, int y, int width, int height, String... values) {
		this(id, x, y, width, height, values, values);
	}

	public GuiCycleButton(int id, int x, int y, int width, int height, String[] displayValues, String... actualValues) {
		super(id, x, y, width, height, "");
		if (displayValues.length != actualValues.length) {
			throw new IllegalArgumentException("displayValues.length != actualValues.length");
		}
		if (displayValues.length == 0) {
			throw new IllegalArgumentException("No values to display");
		}
		this.displayValues = displayValues;
		this.actualValues = actualValues;
		this.displayString = displayValues[0];
		this.currentValue = 0;
	}

	public int getCurrentIndex() {
		return currentValue;
	}

	public String getCurrentValue() {
		return actualValues[currentValue];
	}

	public void setCurrentIndex(int index) {
		currentValue = index;
		displayString = displayValues[index];
	}

	public void setCurrentValue(String value) {
		setCurrentIndex(ArrayUtils.indexOf(actualValues, value));
	}

	public boolean isValidValue(String value) {
		return ArrayUtils.contains(actualValues, value);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			if (GuiScreen.isShiftKeyDown()) {
				currentValue--;
				if (currentValue == -1) {
					currentValue += actualValues.length;
				}
			} else {
				currentValue++;
				if (currentValue == actualValues.length) {
					currentValue = 0;
				}
			}
			displayString = displayValues[currentValue];
			return true;
		}
		return false;
	}

}
