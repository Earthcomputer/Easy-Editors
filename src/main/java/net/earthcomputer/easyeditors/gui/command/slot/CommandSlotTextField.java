package net.earthcomputer.easyeditors.gui.command.slot;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A command slot which represents an expandable text field
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotTextField extends GuiCommandSlotImpl implements ITextField<String> {

	private GuiTypeListenerTextField wrappedTextField;
	protected int x;
	protected int y;
	private int minWidth;
	private int maxWidth;
	private int wordsToConsume;

	/**
	 * Creates a text field with the given minimum width and the given maximum
	 * width, which consumes 1 argument
	 * 
	 * @param minWidth
	 * @param maxWidth
	 */
	public CommandSlotTextField(int minWidth, int maxWidth) {
		this(minWidth, maxWidth, 1);
	}

	/**
	 * Creates a text field with the given minimum width and the given maximum
	 * width, which consumes wordsToConsume arguments. wordsToConsume should be
	 * -1 if all the words up to the end of the command should be consumed
	 */
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
	public void addArgs(List<String> args) throws UIInvalidException {
		String[] argsToAdd = getText().trim().split(" ");
		int len = wordsToConsume < 0 ? argsToAdd.length : wordsToConsume;
		for (int i = 0; i < len; i++) {
			args.add(argsToAdd[i]);
		}
	}

	@Override
	public String getText() {
		return wrappedTextField.getText();
	}

	@Override
	public String getTextAsString() {
		return getText();
	}

	@Override
	public void setText(String text) {
		wrappedTextField.setText(text);
	}

	@Override
	public void setTextAsString(String text) {
		setText(text);
	}

	@Override
	public Predicate<String> getContentFilter() {
		return wrappedTextField.getContentFilter();
	}

	@Override
	public void setContentFilter(Predicate<String> contentFilter) {
		wrappedTextField.setValidator(contentFilter);
	}

	@Override
	public void setStringContentFilter(Predicate<String> contentFilter) {
		setContentFilter(contentFilter);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		if (x != this.x || y != this.y) {
			this.x = x;
			this.y = y;
			recreateInternalTextField();
		}
		wrappedTextField.drawTextBox();
	}

	@Override
	public void setMaxStringLength(int maxStringLength) {
		wrappedTextField.setMaxStringLength(maxStringLength);
	}

	@Override
	public int getMaxStringLength() {
		return wrappedTextField.getMaxStringLength();
	}

	@Override
	public boolean isFocused() {
		return wrappedTextField.isFocused();
	}

	@Override
	public void setFocused(boolean focused) {
		wrappedTextField.setFocused(focused);
	}

	/**
	 * Gets the start of the selection
	 * 
	 * @return
	 */
	public int getSelectionStart() {
		return Math.min(wrappedTextField.getCursorPosition(), wrappedTextField.getSelectionEnd());
	}

	/**
	 * Gets the end of the selection
	 * 
	 * @return
	 */
	public int getSelectionEnd() {
		return Math.max(wrappedTextField.getCursorPosition(), wrappedTextField.getSelectionEnd());
	}

	/**
	 * Gets the caret position
	 * 
	 * @return
	 */
	public int getCaretPos() {
		return wrappedTextField.getCursorPosition();
	}

	/**
	 * Sets the caret position, canceling the selection
	 * 
	 * @param caretPos
	 */
	public void setCaretPos(int caretPos) {
		wrappedTextField.setCursorPosition(caretPos);
	}

	/**
	 * Called when the text in the text field changes
	 */
	protected void onTextChanged() {
	}

	private void recreateInternalTextField() {
		GuiTypeListenerTextField oldTextField = wrappedTextField;
		GuiTypeListenerTextField newTextField = wrappedTextField = new GuiTypeListenerTextField(this, x + 1, y + 1,
				MathHelper.clamp(Minecraft.getMinecraft().fontRendererObj.getStringWidth(oldTextField.getText()) + 8,
						minWidth, maxWidth),
				20);
		newTextField.setMaxStringLengthDangerously(oldTextField.getMaxStringLength());
		newTextField.setTextDangerously(oldTextField.getText());
		newTextField.setSelectionPos(oldTextField.getLineScrollOffset());
		newTextField.setCursorPosition(oldTextField.getCursorPosition());
		newTextField.setFocused(oldTextField.isFocused());
		newTextField.setValidator(oldTextField.contentFilter);
		setWidth(newTextField.width + 2);
	}

	private void ensureInView() {
		onTextChanged();

		String strBefore = wrappedTextField.getText().substring(wrappedTextField.getLineScrollOffset(),
				wrappedTextField.getCursorPosition() - wrappedTextField.getLineScrollOffset());
		int x = wrappedTextField.getEnableBackgroundDrawing() ? wrappedTextField.xPosition + 4
				: wrappedTextField.xPosition;
		x += Minecraft.getMinecraft().fontRendererObj.getStringWidth(strBefore);
		if (getContext() != null)
			getContext().ensureXInView(x);
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		return wrappedTextField.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (getContext().isMouseInBounds(mouseX, mouseY) && mouseX >= wrappedTextField.xPosition
				&& mouseX < wrappedTextField.xPosition + wrappedTextField.width && mouseY >= wrappedTextField.yPosition
				&& mouseY < wrappedTextField.yPosition + wrappedTextField.height)
			wrappedTextField.mouseClicked(mouseX, mouseY, mouseButton);
		return false;
	}

	@Override
	public boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton) {
		if (mouseX < wrappedTextField.xPosition || mouseX >= wrappedTextField.xPosition + wrappedTextField.width
				|| mouseY < wrappedTextField.yPosition
				|| mouseY >= wrappedTextField.yPosition + wrappedTextField.height)
			wrappedTextField.mouseClicked(mouseX, mouseY, mouseButton);
		return false;
	}

	private static class GuiTypeListenerTextField extends GuiTextField {

		private static final Field lineScrollOffsetField = ReflectionHelper.findField(GuiTextField.class,
				"field_146225_q", "lineScrollOffset");

		private CommandSlotTextField listener;
		private Predicate<String> contentFilter = Predicates.alwaysTrue();

		public GuiTypeListenerTextField(CommandSlotTextField listener, int x, int y, int width, int height) {
			super(0, Minecraft.getMinecraft().fontRendererObj, x, y, width, height);
			this.listener = listener;
		}

		@Override
		public void setText(String text) {
			String oldText = getText();
			super.setText(text);
			text = getText();
			if (!oldText.equals(text)) {
				listener.recreateInternalTextField();
				listener.ensureInView();
			}
		}

		public void setTextDangerously(String text) {
			super.setText(text);
		}

		@Override
		public void writeText(String text) {
			String oldText = getText();
			super.writeText(text);
			text = getText();
			if (!oldText.equals(text)) {
				listener.recreateInternalTextField();
				listener.ensureInView();
			}
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
				listener.recreateInternalTextField();
				listener.ensureInView();
			}
		}

		@Override
		public void setMaxStringLength(int maxStringLength) {
			String oldText = getText();
			super.setMaxStringLength(maxStringLength);
			if (!oldText.equals(getText())) {
				listener.recreateInternalTextField();
				listener.ensureInView();
			}
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

		public Predicate<String> getContentFilter() {
			return contentFilter;
		}

		@Override
		public void setValidator(Predicate<String> contentFilter) {
			super.setValidator(contentFilter);
			this.contentFilter = contentFilter;
		}

	}

}
