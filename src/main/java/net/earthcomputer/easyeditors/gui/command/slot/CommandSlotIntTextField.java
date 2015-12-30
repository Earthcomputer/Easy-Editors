package net.earthcomputer.easyeditors.gui.command.slot;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.api.Patterns;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

/**
 * A text field which has an integer value
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotIntTextField extends CommandSlotTextField {

	private int minValue;
	private int maxValue;

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
				int i;
				try {
					i = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					return true;
				}
				return i >= CommandSlotIntTextField.this.minValue && i <= CommandSlotIntTextField.this.maxValue;
			}
		});
		this.minValue = minValue;
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
		setText(args[index]);
		return 1;
	}

	/**
	 * 
	 * @return Whether the value inside this text field is a valid integer
	 */
	public boolean isValid() {
		try {
			Integer.parseInt(getText());
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @return The value of this text field, as an integer
	 */
	public int getIntValue() {
		return getText().isEmpty() ? 0 : Integer.parseInt(getText());
	}

}
