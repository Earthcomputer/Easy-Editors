package net.earthcomputer.easyeditors.gui.command.slot;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CommandSlotTextField extends GuiCommandSlotImpl {

	private GuiTypeListenerTextField wrappedTextField;
	private int x;
	private int y;
	private int minWidth;
	private int maxWidth;
	private int wordsToConsume;

	public CommandSlotTextField(int minWidth, int maxWidth) {
		this(minWidth, maxWidth, 1);
	}

	public CommandSlotTextField(int minWidth, int maxWidth, int wordsToConsume) {
		super(minWidth + 2, 22);
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		wrappedTextField = new GuiTypeListenerTextField(this, 0, 0, minWidth, 20);
		this.wordsToConsume = wordsToConsume;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (wordsToConsume == 0)
			return 0;
		int len;
		if (wordsToConsume < 0) {
			if (args.length <= index)
				throw new CommandSyntaxException();
			len = args.length - index;
		} else {
			if (args.length < index + wordsToConsume)
				throw new CommandSyntaxException();
			len = wordsToConsume;
		}
		String text = args[index];
		int dest = index + len;
		for (int i = index + 1; i < dest; i++) {
			text += " " + args[index];
		}
		setText(text);
		return len;
	}

	@Override
	public void addArgs(List<String> args) {
		String[] argsToAdd = getText().trim().split(" ");
		int len = wordsToConsume < 0 ? argsToAdd.length : wordsToConsume;
		for (int i = 0; i < len; i++) {
			args.add(argsToAdd[i]);
		}
	}

	public String getText() {
		return wrappedTextField.getText();
	}

	public void setText(String text) {
		wrappedTextField.setText(text);
	}

	public void setContentFilter(Predicate<String> contentFilter) {
		wrappedTextField.func_175205_a(contentFilter);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		if (x != this.x || y != this.y) {
			this.x = x;
			this.y = y;
			onTextChanged();
		}
		wrappedTextField.drawTextBox();
	}

	public void setMaxStringLength(int maxStringLength) {
		wrappedTextField.setMaxStringLength(maxStringLength);
	}

	protected void onTextChanged() {
		GuiTypeListenerTextField oldTextField = wrappedTextField;
		GuiTypeListenerTextField newTextField = wrappedTextField = new GuiTypeListenerTextField(this, x + 1, y + 1,
				MathHelper.clamp_int(
						Minecraft.getMinecraft().fontRendererObj.getStringWidth(oldTextField.getText()) + 8, minWidth,
						maxWidth),
				20);
		newTextField.setMaxStringLengthDangerously(oldTextField.getMaxStringLength());
		newTextField.setTextDangerously(oldTextField.getText());
		newTextField.setSelectionPos(oldTextField.getLineScrollOffset());
		newTextField.setCursorPosition(oldTextField.getCursorPosition());
		newTextField.setFocused(oldTextField.isFocused());
		newTextField.func_175205_a(oldTextField.contentFilter);
		setWidth(newTextField.width + 2);
	}

	@Override
	public void onKeyTyped(char typedChar, int keyCode) {
		wrappedTextField.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		wrappedTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private static class GuiTypeListenerTextField extends GuiTextField {

		private static final Field lineScrollOffsetField = ReflectionHelper.findField(GuiTextField.class,
				"field_146225_q", "lineScrollOffset");

		private CommandSlotTextField listener;
		private Predicate contentFilter = Predicates.alwaysTrue();

		public GuiTypeListenerTextField(CommandSlotTextField listener, int x, int y, int width, int height) {
			super(0, Minecraft.getMinecraft().fontRendererObj, x, y, width, height);
			this.listener = listener;
		}

		@Override
		public void setText(String text) {
			String oldText = getText();
			super.setText(text);
			text = getText();
			if (!oldText.equals(text))
				listener.onTextChanged();
		}

		public void setTextDangerously(String text) {
			super.setText(text);
		}

		@Override
		public void writeText(String text) {
			String oldText = getText();
			super.writeText(text);
			text = getText();
			if (!oldText.equals(text))
				listener.onTextChanged();
		}

		@Override
		public void deleteFromCursor(int i) {
			String oldText = getText();
			int oldCursorPosition = getCursorPosition();
			int oldSelectionEnd = getSelectionEnd();
			int oldLineScrollOffset = getLineScrollOffset();
			super.deleteFromCursor(i);
			String newText = getText();
			if (!contentFilter.apply(newText)) {
				setText(oldText);
				setCursorPosition(oldCursorPosition);
				setSelectionPos(oldSelectionEnd);
				setLineScrollOffset(oldLineScrollOffset);
			} else if (!oldText.equals(newText)) {
				listener.onTextChanged();
			}
		}

		@Override
		public void setMaxStringLength(int maxStringLength) {
			String oldText = getText();
			super.setMaxStringLength(maxStringLength);
			if (!oldText.equals(getText()))
				listener.onTextChanged();
		}

		public void setMaxStringLengthDangerously(int maxStringLength) {
			super.setMaxStringLength(maxStringLength);
		}

		public int getLineScrollOffset() {
			try {
				return lineScrollOffsetField.getInt(this);
			} catch (Exception e) {
				throw new ReportedException(CrashReport.makeCrashReport(e, "Doing reflection"));
			}
		}

		public void setLineScrollOffset(int lineScrollOffset) {
			try {
				lineScrollOffsetField.set(this, lineScrollOffset);
			} catch (Exception e) {
				throw new ReportedException(CrashReport.makeCrashReport(e, "Doing reflection"));
			}
		}

		@Override
		public void func_175205_a(Predicate contentFilter) {
			super.func_175205_a(contentFilter);
			this.contentFilter = contentFilter;
		}

	}

}
