package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

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

	private CommandSlotNumberTextField xCoord;
	private CommandSlotNumberTextField yCoord;
	private CommandSlotNumberTextField zCoord;
	private CommandSlotCheckbox xRelative;
	private CommandSlotCheckbox yRelative;
	private CommandSlotCheckbox zRelative;

	public CommandSlotRelativeCoordinate() {
		xCoord = new CommandSlotNumberTextField(50, 100, -30000000, 30000000);
		yCoord = new CommandSlotNumberTextField(50, 100, -30000000, 30000000);
		zCoord = new CommandSlotNumberTextField(50, 100, -30000000, 30000000);
		xRelative = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_RELATIVECOORDINATE);
		yRelative = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_RELATIVECOORDINATE);
		zRelative = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_RELATIVECOORDINATE);
		addChild(CommandSlotLabel.createLabel("X:", xCoord, xRelative));
		addChild(CommandSlotLabel.createLabel("Y:", yCoord, yRelative));
		addChild(CommandSlotLabel.createLabel("Z:", zCoord, zRelative));
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index + 2 >= args.length) {
			throw new CommandSyntaxException();
		}
		parseArg(args[index], xCoord, xRelative);
		parseArg(args[index + 1], yCoord, yRelative);
		parseArg(args[index + 2], zCoord, zRelative);
		return 3;
	}

	private static void parseArg(String arg, CommandSlotNumberTextField coordField, CommandSlotCheckbox relative)
			throws CommandSyntaxException {
		boolean isRelative = arg.startsWith("~");
		relative.setChecked(isRelative);
		if (isRelative) {
			arg = arg.substring(1);
		}
		if (arg.isEmpty()) {
			coordField.setText("0");
		} else {
			double d;
			try {
				d = Double.parseDouble(arg);
			} catch (NumberFormatException e) {
				throw new CommandSyntaxException();
			}
			coordField.setText(GeneralUtils.doubleToString(d));
		}
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		writeArg(args, xCoord, xRelative);
		writeArg(args, yCoord, yRelative);
		writeArg(args, zCoord, zRelative);
	}

	private static void writeArg(List<String> args, CommandSlotNumberTextField coordField,
			CommandSlotCheckbox relative) {
		double dValue = coordField.getDoubleValue();
		if (dValue == 0 && relative.isChecked()) {
			args.add("~");
		} else {
			String arg = GeneralUtils.doubleToString(dValue);
			if (relative.isChecked()) {
				arg = "~" + arg;
			}
			args.add(arg);
		}
	}

	/**
	 * 
	 * @throws UIInvalidException
	 *             When any of the internal text fields do not hold a valid
	 *             double
	 */
	public void checkValid() throws UIInvalidException {
		xCoord.checkValid();
		yCoord.checkValid();
		zCoord.checkValid();
	}

}
