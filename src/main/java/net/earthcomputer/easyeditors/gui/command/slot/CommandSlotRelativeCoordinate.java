package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;

/**
 * A command slot for relative coordinates (accepts non-integer values), e.g. in
 * <code>/setblock</code>
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotRelativeCoordinate extends CommandSlotVerticalArrangement {

	private CoordinateArg x;
	private CoordinateArg y;
	private CoordinateArg z;

	public CommandSlotRelativeCoordinate() {
		this(Colors.label.color);
	}

	public CommandSlotRelativeCoordinate(int textColor) {
		class ChangeListeningCoordArg extends CoordinateArg {
			@Override
			protected void onChanged() {
				CommandSlotRelativeCoordinate.this.onChanged();
			}
		}
		x = new ChangeListeningCoordArg();
		y = new ChangeListeningCoordArg();
		y.getTextField().setMinValue(-4096);
		y.getTextField().setMaxValue(4096);
		z = new ChangeListeningCoordArg();
		addChild(CommandSlotLabel.createLabel("X:", textColor, x));
		addChild(CommandSlotLabel.createLabel("Y:", textColor, y));
		addChild(CommandSlotLabel.createLabel("Z:", textColor, z));
	}

	public CoordinateArg getXArg() {
		return x;
	}

	public CoordinateArg getYArg() {
		return y;
	}

	public CoordinateArg getZArg() {
		return z;
	}

	protected void onChanged() {
	}

	public static class CoordinateArg extends CommandSlotHorizontalArrangement {
		private CommandSlotNumberTextField textField;
		private CommandSlotCheckbox relative;

		public CoordinateArg() {
			textField = new CommandSlotNumberTextField(50, 100, -30000000, 30000000) {
				@Override
				protected void onTextChanged() {
					onChanged();
				}
			};
			textField.setText("0");
			relative = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_RELATIVECOORDINATE) {
				@Override
				protected void onChecked(boolean checked) {
					onChanged();
				}
			};
			relative.setChecked(true);
			addChild(textField);
			addChild(relative);
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (args.length == index) {
				throw new CommandSyntaxException();
			}
			String arg = args[index];
			boolean relative = arg.startsWith("~");
			if (relative) {
				arg = arg.substring(1);
				if (arg.isEmpty()) {
					arg = "0";
				}
			}
			textField.readFromArgs(new String[] { arg }, 0);
			this.relative.setChecked(relative);
			return 1;
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
			checkValid();
			double textFieldValue = textField.getDoubleValue();
			if (textFieldValue == 0) {
				if (relative.isChecked()) {
					args.add("~");
				} else {
					args.add("0");
				}
			} else {
				String arg = GeneralUtils.doubleToString(textFieldValue);
				if (relative.isChecked()) {
					arg = "~" + arg;
				}
				args.add(arg);
			}
		}

		/**
		 * 
		 * @throws UIInvalidException
		 *             When this internal text field has an invalid number
		 */
		public void checkValid() throws UIInvalidException {
			textField.checkValid();
		}

		/**
		 * Gets the internal text field
		 * 
		 * @return
		 */
		public CommandSlotNumberTextField getTextField() {
			return textField;
		}

		/**
		 * Gets the internal checkbox
		 * 
		 * @return
		 */
		public CommandSlotCheckbox getRelative() {
			return relative;
		}

		protected void onChanged() {
		}
	}

}
