package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

/**
 * A command slot which is not visible or editable to the user, but adds a value
 * to the arguments
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotUneditableValue extends GuiCommandSlotImpl {

	private String value;

	public CommandSlotUneditableValue(String defaultValue) {
		super(0, 0);
		this.value = defaultValue;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		value = args[index];
		return 1;
	}

	@Override
	public void addArgs(List<String> args) {
		args.add(value);
	}

	/**
	 * 
	 * @return The value to add to the arguments
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value to add to the arguments
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
	}

}
