package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.util.math.MathHelper;

/**
 * A text field which has an integer value
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotIntTextField extends CommandSlotTextField {

	private int minValue;
	private int maxValue;

	private String numberInvalidMessage;
	private String outOfBoundsMessage;

	public CommandSlotIntTextField(int minWidth, int maxWidth) {
		this(minWidth, maxWidth, Integer.MIN_VALUE);
	}

	public CommandSlotIntTextField(int minWidth, int maxWidth, int minValue) {
		this(minWidth, maxWidth, minValue, Integer.MAX_VALUE);
	}

	public CommandSlotIntTextField(int minWidth, int maxWidth, int minValue, int maxValue) {
		super(minWidth, maxWidth);
		setContentFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				if (!Patterns.partialInteger.matcher(input).matches())
					return false;
				else if (CommandSlotIntTextField.this.minValue >= 0 && input.startsWith("-"))
					return false;
				else if (CommandSlotIntTextField.this.maxValue < 0 && input.startsWith("+"))
					return false;
				else
					return true;
			}
		});
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public CommandSlotIntTextField setNumberInvalidMessage(String numberInvalidMessage) {
		this.numberInvalidMessage = numberInvalidMessage;
		return this;
	}

	public CommandSlotIntTextField setOutOfBoundsMessage(String outOfBoundsMessage) {
		this.outOfBoundsMessage = outOfBoundsMessage;
		return this;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		int i;
		try {
			i = Integer.parseInt(args[index]);
		} catch (NumberFormatException e) {
			throw new CommandSyntaxException();
		}
		if (i < minValue || i > maxValue)
			throw new CommandSyntaxException();
		setText(String.valueOf(i));
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();

		args.add(String.valueOf(getIntValue()));
	}

	/**
	 * 
	 * @return Whether the value inside this text field is a valid integer
	 */
	public void checkValid() throws UIInvalidException {
		int intVal;
		try {
			intVal = Integer.parseInt(getText());
		} catch (NumberFormatException e) {
			throw new UIInvalidException(numberInvalidMessage == null ? TranslateKeys.GUI_COMMANDEDITOR_NUMBERINVALID
					: numberInvalidMessage);
		}
		if (intVal < minValue || intVal > maxValue) {
			throw new UIInvalidException(
					outOfBoundsMessage == null ? TranslateKeys.GUI_COMMANDEDITOR_NUMBEROUTOFBOUNDS : outOfBoundsMessage,
					minValue, maxValue);
		}
	}

	/**
	 * 
	 * @return The value of this text field, as an integer
	 */
	public int getIntValue() {
		int intVal;
		try {
			intVal = Integer.parseInt(getText());
		} catch (NumberFormatException e) {
			intVal = 0;
		}
		return MathHelper.clamp(intVal, minValue, maxValue);
	}

	public static class Optional extends CommandSlotIntTextField implements IOptionalCommandSlot {
		private int defaultValue;

		public Optional(int minWidth, int maxWidth, int defaultValue) {
			super(minWidth, maxWidth);
			this.defaultValue = defaultValue;
		}

		public Optional(int minWidth, int maxWidth, int minValue, int defaultValue) {
			super(minWidth, maxWidth, minValue);
			this.defaultValue = defaultValue;
		}

		public Optional(int minWidth, int maxWidth, int minValue, int maxValue, int defaultValue) {
			super(minWidth, maxWidth, minValue, maxValue);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean isDefault() throws UIInvalidException {
			checkValid();
			return getIntValue() == defaultValue;
		}

		@Override
		public void setToDefault() {
			setText(String.valueOf(defaultValue));
		}
	}

}
