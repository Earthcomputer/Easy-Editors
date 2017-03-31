package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class CommandSlotSelectFromList<T> extends CommandSlotHorizontalArrangement implements ICallback<T> {

	private T value = null;
	private CommandSlotLabel label;
	private String nullErrorMessage;

	public CommandSlotSelectFromList(String nullLabel, String nullErrorMessage) {
		this.nullErrorMessage = nullErrorMessage;

		addChild(label = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, nullLabel, 0xff0000));

		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						createGui(Minecraft.getMinecraft().currentScreen, CommandSlotSelectFromList.this));
			}
		});
	}

	@Override
	public T getCallbackValue() {
		return getValue();
	}

	@Override
	public void setCallbackValue(T value) {
		setValue(value);
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
		this.label.setColor(0);
		this.label.setText(getDisplayName(value));
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		T val = readArg(args[index]);
		if (val == null) {
			throw new CommandSyntaxException();
		}
		setValue(val);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		args.add(writeArg(value));
	}

	public void checkValid() throws UIInvalidException {
		if (value == null) {
			throw new UIInvalidException(nullErrorMessage);
		}
	}

	protected abstract GuiSelectFromList<T> createGui(GuiScreen currentScreen, ICallback<T> callback);

	protected abstract String getDisplayName(T val);

	protected abstract T readArg(String arg);

	protected abstract String writeArg(T arg);

}
