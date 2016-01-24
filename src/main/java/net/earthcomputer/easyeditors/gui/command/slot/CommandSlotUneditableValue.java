package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
	}

}
