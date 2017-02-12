package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;

public class CommandSlotConsumeRemainingArgs extends GuiCommandSlotImpl {

	public CommandSlotConsumeRemainingArgs() {
		super(0, 0);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		return args.length - index;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
	}

}
