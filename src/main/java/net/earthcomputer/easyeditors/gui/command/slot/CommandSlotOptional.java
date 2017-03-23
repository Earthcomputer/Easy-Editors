package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;

public abstract class CommandSlotOptional extends CommandSlotBox {

	private List<CommandSlotOptional> optionalGroup;
	private int optionalGroupIndex;
	private boolean hasInitialized = false;

	public CommandSlotOptional(IGuiCommandSlot child, List<CommandSlotOptional> optionalGroup) {
		super(child);
		this.optionalGroup = optionalGroup;
		optionalGroupIndex = optionalGroup.size();
		optionalGroup.add(this);
	}

	protected abstract boolean isDefault() throws UIInvalidException;

	protected abstract void setToDefault();

	protected boolean isPresentInArgs(String[] args, int index) throws CommandSyntaxException {
		return args.length != index;
	}

	protected boolean shouldAddToArgs() throws UIInvalidException {
		for (int i = optionalGroupIndex, e = optionalGroup.size(); i < e; i++) {
			if (!optionalGroup.get(i).isDefault()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		hasInitialized = true;
		if (!isPresentInArgs(args, index)) {
			setToDefault();
			return 0;
		} else {
			return super.readFromArgs(args, index);
		}
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (shouldAddToArgs()) {
			super.addArgs(args);
		}
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		if (!hasInitialized) {
			setToDefault();
			hasInitialized = true;
		}
		super.draw(x, y, mouseX, mouseY, partialTicks);
	}

	public static class Impl extends CommandSlotOptional {

		private IOptionalCommandSlot optionalChild;

		public Impl(IOptionalCommandSlot child, List<CommandSlotOptional> optionalGroup) {
			super(convertToCommandSlot(child), optionalGroup);
			this.optionalChild = child;
		}

		private static IGuiCommandSlot convertToCommandSlot(IOptionalCommandSlot optionalCommandSlot) {
			if (!(optionalCommandSlot instanceof IGuiCommandSlot)) {
				throw new IllegalArgumentException("Not a command slot");
			}
			return (IGuiCommandSlot) optionalCommandSlot;
		}

		@Override
		protected boolean isDefault() throws UIInvalidException {
			return optionalChild.isDefault();
		}

		@Override
		protected void setToDefault() {
			optionalChild.setToDefault();
		}

	}

}
