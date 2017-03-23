package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.util.math.MathHelper;

public class CommandSlotNumberTextField extends CommandSlotTextField {

	private double minValue;
	private double maxValue;

	private String numberInvalidMessage;
	private String outOfBoundsMessage;

	public CommandSlotNumberTextField(int minWidth, int maxWidth) {
		this(minWidth, maxWidth, -Double.MAX_VALUE);
	}

	public CommandSlotNumberTextField(int minWidth, int maxWidth, double minValue) {
		this(minWidth, maxWidth, minValue, Double.MAX_VALUE);
	}

	public CommandSlotNumberTextField(int minWidth, int maxWidth, double minValue, double maxValue) {
		super(minWidth, maxWidth);
		setContentFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				if (!Patterns.partialDouble.matcher(input).matches())
					return false;
				else if (CommandSlotNumberTextField.this.minValue >= 0 && input.startsWith("-"))
					return false;
				else if (CommandSlotNumberTextField.this.maxValue < 0 && input.startsWith("+"))
					return false;
				else
					return true;
			}
		});
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public CommandSlotNumberTextField setNumberInvalidMessage(String numberInvalidMessage) {
		this.numberInvalidMessage = numberInvalidMessage;
		return this;
	}

	public CommandSlotNumberTextField setOutOfBoundsMessage(String outOfBoundsMessage) {
		this.outOfBoundsMessage = outOfBoundsMessage;
		return this;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		double d;
		try {
			d = Double.parseDouble(args[index]);
		} catch (NumberFormatException e) {
			throw new CommandSyntaxException();
		}
		if (d < minValue || d > maxValue)
			throw new CommandSyntaxException();
		setText(GeneralUtils.doubleToString(d));
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();

		args.add(GeneralUtils.doubleToString(getDoubleValue()));
	}

	/**
	 * 
	 * @return Whether the value inside this text field is a valid double
	 */
	public void checkValid() throws UIInvalidException {
		double doubleVal;
		try {
			doubleVal = Double.parseDouble(getText());
		} catch (NumberFormatException e) {
			throw new UIInvalidException(numberInvalidMessage == null ? TranslateKeys.GUI_COMMANDEDITOR_NUMBERINVALID
					: numberInvalidMessage);
		}
		if (doubleVal < minValue || doubleVal > maxValue) {
			throw new UIInvalidException(
					outOfBoundsMessage == null ? TranslateKeys.GUI_COMMANDEDITOR_NUMBEROUTOFBOUNDS : outOfBoundsMessage,
					minValue, maxValue);
		}
	}

	/**
	 * 
	 * @return The value of this text field, as a double
	 */
	public double getDoubleValue() {
		double doubleVal;
		try {
			doubleVal = Double.parseDouble(getText());
		} catch (NumberFormatException e) {
			doubleVal = 0;
		}
		return MathHelper.clamp(doubleVal, minValue, maxValue);
	}

	public static class Optional extends CommandSlotNumberTextField implements IOptionalCommandSlot {
		private double defaultValue;

		public Optional(int minWidth, int maxWidth, double minValue, double maxValue, double defaultValue) {
			super(minWidth, maxWidth, minValue, maxValue);
			this.defaultValue = defaultValue;
		}

		public Optional(int minWidth, int maxWidth, double minValue, double defaultValue) {
			super(minWidth, maxWidth, minValue);
			this.defaultValue = defaultValue;
		}

		public Optional(int minWidth, int maxWidth, double defaultValue) {
			super(minWidth, maxWidth);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean isDefault() throws UIInvalidException {
			checkValid();
			return getDoubleValue() == defaultValue;
		}

		@Override
		public void setToDefault() {
			setText(GeneralUtils.doubleToString(defaultValue));
		}
	}
}
