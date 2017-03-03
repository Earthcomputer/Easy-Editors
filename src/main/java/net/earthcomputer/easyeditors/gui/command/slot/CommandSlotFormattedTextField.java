package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.earthcomputer.easyeditors.api.util.CharFormat;
import net.earthcomputer.easyeditors.api.util.FormattedText;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot which represents a text field which contains formatted text
 * 
 * @author Earthcomputer
 * @see FormattedText
 */
public class CommandSlotFormattedTextField extends GuiCommandSlotImpl implements ITextField<FormattedText> {

	private int x, y;

	private HoverChecker hoverChecker;
	private boolean isShowingHint = false;
	private boolean isShowingColorPalette = false;

	private FormattedText text = FormattedText.EMPTY;
	private boolean focused;
	private int caretPos;
	private int selectionPos;
	private int scrollPos;
	private int maxStringLength = 32;
	private Predicate<FormattedText> contentFilter = Predicates.alwaysTrue();
	private CharFormat zeroLengthFormat = null;

	public CommandSlotFormattedTextField(int width) {
		super(width + 2, 22);
	}

	/**
	 * Returns the selected text, may be empty
	 * 
	 * @return
	 */
	public FormattedText getSelectedText() {
		return text.substring(Math.min(caretPos, selectionPos), Math.max(caretPos, selectionPos));
	}

	private void writeText(FormattedText text) {
		FormattedText newText = FormattedText.EMPTY;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c < 32 || c == 127) {
				text = text.substring(0, i).concat(text.substring(i + 1));
				i--;
			}
		}
		int selectionStart = Math.min(caretPos, selectionPos);
		int selectionEnd = Math.max(caretPos, selectionPos);
		int maxWriteLength = maxStringLength - this.text.length() - (selectionStart - selectionEnd);

		if (!this.text.isEmpty()) {
			newText = newText.concat(this.text.substring(0, selectionStart));
		}

		int lengthToWrite;

		if (maxWriteLength < text.length()) {
			newText = newText.concat(text.substring(0, maxWriteLength));
			lengthToWrite = maxWriteLength;
		} else {
			newText = newText.concat(text);
			lengthToWrite = text.length();
		}

		if (!this.text.isEmpty() && selectionEnd < this.text.length()) {
			newText = newText.concat(this.text.substring(selectionEnd));
		}

		if (contentFilter.apply(newText)) {
			this.text = newText;
			setCaretPos(caretPos + selectionStart - selectionPos + lengthToWrite);
			setSelectionPos(caretPos);
		}
	}

	private void deleteWords(int count) {
		if (text.isEmpty()) {
			return;
		}
		if (selectionPos != caretPos) {
			writeText(FormattedText.EMPTY);
		} else {
			deleteFromCursor(getNthWordFromPos(count, caretPos) - caretPos);
		}
	}

	private void deleteFromCursor(int count) {
		if (text.isEmpty()) {
			return;
		}
		if (selectionPos != caretPos) {
			this.writeText(FormattedText.EMPTY);
		} else {
			boolean deletingBackwards = count < 0;
			int deleteStart = deletingBackwards ? caretPos + count : caretPos;
			int deleteEnd = deletingBackwards ? caretPos : caretPos + count;
			FormattedText s = FormattedText.EMPTY;

			if (deleteStart >= 0) {
				s = this.text.substring(0, deleteStart);
			}

			if (deleteEnd < this.text.length()) {
				s = s.concat(this.text.substring(deleteEnd));
			}

			if (contentFilter.apply(s)) {
				this.text = s;

				if (deletingBackwards) {
					setCaretPos(caretPos + count);
					setSelectionPos(caretPos);
				}
			}

		}
	}

	private int getNthWordFromPos(int n, int pos) {
		int nthWordPos = pos;
		boolean lookingBackwards = n < 0;
		int distanceToLook = Math.abs(n);

		for (int i = 0; i < distanceToLook; i++) {
			if (!lookingBackwards) {
				int len = text.length();
				nthWordPos = text.indexOf(' ', nthWordPos);

				if (nthWordPos == -1) {
					nthWordPos = len;
				}
			} else {
				while (nthWordPos > 0 && text.charAt(nthWordPos - 1) != ' ') {
					nthWordPos--;
				}
			}
		}

		return nthWordPos;
	}

	/**
	 * Sets the end of the selection (the other end from the caret), scrolling
	 * if necessary
	 * 
	 * @param pos
	 */
	public void setSelectionPos(int pos) {
		selectionPos = MathHelper.clamp(pos, 0, text.length());

		if (selectionPos < scrollPos) {
			scrollPos = selectionPos;
		} else {
			String shownText = text.substring(scrollPos).toVanillaText();
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			shownText = fontRenderer.trimStringToWidth(shownText, getWidth() - 4 - 8);
			shownText = TextFormatting.getTextWithoutFormattingCodes(shownText);

			int endIdx = scrollPos + shownText.length();
			if (selectionPos > endIdx) {
				scrollPos += selectionPos - endIdx;
			}

			// always needed in case the text length has changed
			scrollPos = MathHelper.clamp(scrollPos, 0, text.length());
		}
	}

	/**
	 * Sets the position of the caret, <b>without setting the selection end</b>
	 * or the scroll position. If you need to set the selection end as well, use
	 * {@link #setSelectionPos(int)} in addition to this method
	 * 
	 * @param pos
	 */
	public void setCaretPos(int pos) {
		caretPos = MathHelper.clamp(pos, 0, text.length());
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		if (!focused) {
			isShowingHint = false;
			isShowingColorPalette = false;
			return false;
		}

		if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			setCaretPos(text.length());
			setSelectionPos(0);
			zeroLengthFormat = null;
			return true;
		}
		if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText().toVanillaText());
			return true;
		}
		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			writeText(FormattedText.compile(GuiScreen.getClipboardString()));
			zeroLengthFormat = null;
			return true;
		}
		if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText().toVanillaText());
			writeText(FormattedText.EMPTY);
			zeroLengthFormat = null;
			return true;
		}
		if (keyCode == Keyboard.KEY_SPACE && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown()
				&& !GuiScreen.isAltKeyDown()) {
			isShowingHint = true;
			isShowingColorPalette = false;
			return true;
		} else if (keyCode == Keyboard.KEY_F && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown()
				&& !GuiScreen.isAltKeyDown()) {
			isShowingHint = false;
			isShowingColorPalette = true;
			return true;
		} else {
			isShowingHint = false;
			isShowingColorPalette = false;
			if (hoverChecker != null) {
				hoverChecker.resetHoverTimer();
			}
			if (GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown()) {
				if (keyCode == Keyboard.KEY_B) {
					changeFormatting(TextFormatting.BOLD);
					return true;
				} else if (keyCode == Keyboard.KEY_I) {
					changeFormatting(TextFormatting.ITALIC);
					return true;
				} else if (keyCode == Keyboard.KEY_U) {
					changeFormatting(TextFormatting.UNDERLINE);
					return true;
				} else if (keyCode == Keyboard.KEY_O) {
					changeFormatting(TextFormatting.OBFUSCATED);
					return true;
				} else if (keyCode == Keyboard.KEY_S) {
					changeFormatting(TextFormatting.STRIKETHROUGH);
					return true;
				}
			}
		}

		switch (keyCode) {
		case Keyboard.KEY_BACK: {
			if (GuiScreen.isCtrlKeyDown()) {
				deleteWords(-1);
			} else {
				deleteFromCursor(-1);
			}
			zeroLengthFormat = null;
			return true;
		}
		case Keyboard.KEY_HOME: {
			setSelectionPos(0);
			if (!GuiScreen.isShiftKeyDown()) {
				setCaretPos(0);
			}
			zeroLengthFormat = null;
			return true;
		}
		case Keyboard.KEY_LEFT: {
			if (GuiScreen.isShiftKeyDown()) {
				if (GuiScreen.isCtrlKeyDown()) {
					setSelectionPos(getNthWordFromPos(-1, selectionPos));
				} else {
					setSelectionPos(selectionPos - 1);
				}
			} else {
				if (GuiScreen.isCtrlKeyDown()) {
					setSelectionPos(getNthWordFromPos(-1, caretPos));
				} else {
					setSelectionPos(selectionPos - 1);
				}
				setCaretPos(selectionPos);
			}
			zeroLengthFormat = null;
			return true;
		}
		case Keyboard.KEY_END: {
			setSelectionPos(text.length());
			if (!GuiScreen.isShiftKeyDown()) {
				setCaretPos(selectionPos);
			}
			zeroLengthFormat = null;
			return true;
		}
		case Keyboard.KEY_RIGHT: {
			if (GuiScreen.isShiftKeyDown()) {
				if (GuiScreen.isCtrlKeyDown()) {
					setSelectionPos(getNthWordFromPos(1, selectionPos));
				} else {
					setSelectionPos(selectionPos + 1);
				}
			} else {
				if (GuiScreen.isShiftKeyDown()) {
					setSelectionPos(getNthWordFromPos(1, caretPos));
				} else {
					setSelectionPos(selectionPos + 1);
				}
				setCaretPos(selectionPos);
			}
			zeroLengthFormat = null;
			return true;
		}
		case Keyboard.KEY_DELETE: {
			if (GuiScreen.isCtrlKeyDown()) {
				deleteWords(1);
			} else {
				deleteFromCursor(1);
			}
			return true;
		}
		default: {
			if (!ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
				return false;
			}
			writeText(new FormattedText(String.valueOf(typedChar)));
			CharFormat format;
			if (zeroLengthFormat != null) {
				format = zeroLengthFormat;
				zeroLengthFormat = null;
			} else if (text.length() > 1) {
				if (caretPos >= 2) {
					format = text.formatAt(caretPos - 2);
				} else {
					format = text.formatAt(caretPos);
				}
			} else {
				format = CharFormat.NO_STYLE;
			}
			FormattedText newText = text.withFormatAt(caretPos - 1, format);
			if (contentFilter.apply(newText)) {
				text = newText;
			}
			return true;
		}
		}
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		// Render base text box (see GuiTextField.drawTextBox)

		this.x = x;
		this.y = y;

		// background
		drawRect(x, y, x + getWidth(), y + getHeight(), 0xffa0a0a0);
		drawRect(x + 1, y + 1, x + getWidth() - 1, y + getHeight() - 1, 0xff303030);

		// initialize
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		final int TEXT_COLOR = 0xe0e0e0;

		int relativeCaretPos = caretPos - scrollPos;
		int relativeSelectionPos = selectionPos - scrollPos;

		FormattedText displayedText = FormattedText.compile(
				fontRenderer.trimStringToWidth(this.text.substring(scrollPos).toVanillaText(), getWidth() - 4 - 8));

		boolean caretInView = relativeCaretPos >= 0 && relativeCaretPos <= displayedText.length();
		boolean caretVisible = focused;

		int startX = x + 6;
		int textY = y + (getHeight() - fontRenderer.FONT_HEIGHT - 1) / 2 + 2;
		int workingX = startX;

		if (relativeSelectionPos > displayedText.length()) {
			relativeSelectionPos = displayedText.length();
		}

		// draw obfuscated text more clearly
		int obfuscatedWorkingX = workingX;
		for (int i = 0, e = displayedText.length(); i < e; i++) {
			CharFormat format = displayedText.formatAt(i);
			int charWidth = fontRenderer.getCharWidth(displayedText.charAt(i));
			if (format.isBold()) {
				charWidth++;
			}
			if (format.isObfuscated()) {
				Gui.drawRect(obfuscatedWorkingX, textY, obfuscatedWorkingX + charWidth,
						textY + fontRenderer.FONT_HEIGHT, 0xc0c00000);
				displayedText = displayedText.withFormatAt(i,
						new CharFormat.Builder(format).setObfuscated(false).build());
			}
			obfuscatedWorkingX += charWidth;
		}

		// draw text before caret,kju \hm[

		if (!displayedText.isEmpty()) {
			FormattedText textBeforeCaret = caretInView ? displayedText.substring(0, relativeCaretPos) : displayedText;
			workingX = fontRenderer.drawStringWithShadow(textBeforeCaret.toVanillaText(), startX, textY, TEXT_COLOR);
		}

		// figure out how to draw caret
		boolean useLineForCaret = caretPos < this.text.length() || this.text.length() >= maxStringLength;
		int caretX = workingX;

		if (!caretInView) {
			caretX = relativeCaretPos > 0 ? startX + getWidth() - 4 : startX;
		} else if (useLineForCaret) {
			caretX = workingX - 1;
			workingX--;
		}

		// draw text after caret
		if (!displayedText.isEmpty() && caretInView && relativeCaretPos < displayedText.length()) {
			FormattedText str = displayedText.substring(relativeCaretPos);
			workingX = fontRenderer.drawStringWithShadow(str.toVanillaText(), workingX, textY, TEXT_COLOR);
		}

		// draw caret, on top of text
		if (caretVisible) {
			if (useLineForCaret) {
				Gui.drawRect(caretX, textY - 1, caretX + 1, textY + 1 + fontRenderer.FONT_HEIGHT, 0xffd0d0d0);
			} else {
				fontRenderer.drawStringWithShadow("_", caretX, textY, TEXT_COLOR);
			}
		}

		// draw selection box
		if (relativeSelectionPos != relativeCaretPos) {
			int endSelectionX = startX
					+ fontRenderer.getStringWidth(displayedText.substring(0, relativeSelectionPos).toVanillaText());
			this.drawSelectionBox(x, y, caretX, textY - 1, endSelectionX - 1, textY + 1 + fontRenderer.FONT_HEIGHT);
		}

		// Custom behavior specific to a formatted text field

		// logic
		if (!focused) {
			isShowingHint = false;
			if (hoverChecker != null) {
				hoverChecker.resetHoverTimer();
			}
		}

		if (hoverChecker == null) {
			hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
		} else {
			hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());
		}
		if (hoverChecker.checkHover(mouseX, mouseY) && !isShowingHint && !isShowingColorPalette) {
			drawTooltip(x - 8, y + getHeight() + 16, Translate.GUI_COMMANDEDITOR_TEXTFIELD_INITIALHINT, getWidth());

		}

		// draw various tooltips
		if (isShowingHint) {
			drawTooltip(x - 8, y + getHeight() + 16, Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_LINE1,
					Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_BOLD, Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_ITALIC,
					Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_UNDERLINE,
					Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_STRIKETHROUGH,
					Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_OBFUSCATED,
					Translate.GUI_COMMANDEDITOR_TEXTFIELD_HINT_COLOR);
		}
	}

	private void drawSelectionBox(int x, int y, int startX, int startY, int endX, int endY) {
		if (startX < endX) {
			int tmp = startX;
			startX = endX;
			endX = tmp;
		}

		if (startY < endY) {
			int tmp = startY;
			startY = endY;
			endY = tmp;
		}

		if (endX > x + getWidth()) {
			endX = x + getWidth();
		}

		if (startX > x + getWidth()) {
			startX = x + getWidth();
		}

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		GlStateManager.color(0, 0, 255, 255);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		buffer.pos(startX, endY, 0).endVertex();
		buffer.pos(endX, endY, 0).endVertex();
		buffer.pos(endX, startY, 0).endVertex();
		buffer.pos(startX, startY, 0).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	@Override
	public void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.drawForeground(x, y, mouseX, mouseY, partialTicks);
		if (isShowingColorPalette) {
			drawRect(x, y - 36, x + 36, y, 0x80ffffff);
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					char formattingCode = TextFormatting.fromColorIndex(i + j * 4).toString().charAt(1);
					int color = Minecraft.getMinecraft().fontRendererObj.getColorCode(formattingCode);
					color |= 0xff000000;
					drawRect(x + 2 + 8 * i, y - 36 + 2 + 8 * j, x + 2 + 8 * (i + 1), y - 36 + 2 + 8 * (j + 1), color);
				}
			}
		}
	}

	@Override
	public boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton) {
		if (isShowingColorPalette) {
			if (mouseButton == 0 && mouseX >= x + 2 && mouseY >= y - 36 + 2 && mouseX < x + 36 - 2 && mouseY < y - 2) {
				int i = (mouseX - x - 2) / 8;
				int j = (mouseY - y + 36 - 2) / 8;
				changeFormatting(TextFormatting.fromColorIndex(i + j * 4));
				isShowingColorPalette = false;
				return true;
			}
		}
		return super.onMouseClickedForeground(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		focused = mouseX >= x && mouseX < y + getWidth() && mouseY >= y && mouseY < y + getHeight();

		if (focused && mouseButton == 0) {
			int relativeX = mouseX - x;

			relativeX -= 6;

			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			String displayedText = fontRenderer.trimStringToWidth(text.substring(scrollPos).toVanillaText(),
					getWidth() - 4 - 8);
			setCaretPos(FormattedText.compile(fontRenderer.trimStringToWidth(displayedText, relativeX)).length()
					+ scrollPos);
			setSelectionPos(caretPos);
			zeroLengthFormat = null;
		}

		if (!focused) {
			isShowingHint = false;
			isShowingColorPalette = false;
		}

		return focused;
	}

	protected void changeFormatting(TextFormatting formatting) {
		if (caretPos == selectionPos) {
			CharFormat existingFormat;
			if (zeroLengthFormat != null) {
				existingFormat = zeroLengthFormat;
			} else if (text.isEmpty()) {
				existingFormat = CharFormat.NO_STYLE;
			} else if (caretPos == 0) {
				existingFormat = text.formatAt(0);
			} else {
				existingFormat = text.formatAt(caretPos - 1);
			}
			if (formatting.isColor()) {
				zeroLengthFormat = new CharFormat.Builder(existingFormat)
						.setColor(existingFormat.getColor() == formatting ? null : formatting).build();
			} else if (formatting == TextFormatting.BOLD) {
				zeroLengthFormat = new CharFormat.Builder(existingFormat).setBold(!existingFormat.isBold()).build();
			} else if (formatting == TextFormatting.ITALIC) {
				zeroLengthFormat = new CharFormat.Builder(existingFormat).setItalic(!existingFormat.isItalic()).build();
			} else if (formatting == TextFormatting.UNDERLINE) {
				zeroLengthFormat = new CharFormat.Builder(existingFormat).setUnderlined(!existingFormat.isUnderlined())
						.build();
			} else if (formatting == TextFormatting.STRIKETHROUGH) {
				zeroLengthFormat = new CharFormat.Builder(existingFormat)
						.setStrikethrough(!existingFormat.isUnderlined()).build();
			} else if (formatting == TextFormatting.OBFUSCATED) {
				zeroLengthFormat = new CharFormat.Builder(existingFormat).setObfuscated(!existingFormat.isObfuscated())
						.build();
			} else {
				throw new IllegalArgumentException("Invalid style: " + formatting);
			}
		} else {
			FormattedText newText = text.toggleStyle(Math.min(caretPos, selectionPos), Math.max(caretPos, selectionPos),
					formatting);
			if (contentFilter.apply(newText)) {
				text = newText;
			}
		}
		return;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		String text = "";
		for (int i = index; i < args.length; i++) {
			if (i != index) {
				text += " ";
			}
			text += args[i];
		}

		this.text = FormattedText.compile(text);

		return args.length - index;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		for (String part : text.toVanillaText().split(" ")) {
			args.add(part);
		}
	}

	@Override
	public FormattedText getText() {
		return text;
	}

	@Override
	public String getTextAsString() {
		return text.toVanillaText();
	}

	@Override
	public void setText(FormattedText text) {
		if (text.length() > maxStringLength) {
			text = text.substring(0, maxStringLength);
		}
		if (contentFilter.apply(text)) {
			this.text = text;
			setCaretPos(text.length());
			setSelectionPos(caretPos);
		}
	}

	@Override
	public void setTextAsString(String text) {
		setText(FormattedText.compile(text));
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public int getMaxStringLength() {
		return maxStringLength;
	}

	@Override
	public void setMaxStringLength(int maxStringLength) {
		this.maxStringLength = maxStringLength;
		if (text.length() > maxStringLength) {
			setText(text.substring(0, maxStringLength));
		}
	}

	/**
	 * Gets the position of the caret
	 * 
	 * @return
	 */
	public int getCaretPos() {
		return caretPos;
	}

	/**
	 * Gets the selection end, i.e. the other end of the selection from the
	 * caret
	 * 
	 * @return
	 */
	public int getSelectionPos() {
		return selectionPos;
	}

	@Override
	public void setStringContentFilter(final Predicate<String> filter) {
		setContentFilter(new Predicate<FormattedText>() {
			@Override
			public boolean apply(FormattedText text) {
				return filter.apply(text.getUnformattedText());
			}
		});
	}

	@Override
	public void setContentFilter(Predicate<FormattedText> filter) {
		this.contentFilter = filter;
	}

	@Override
	public Predicate<FormattedText> getContentFilter() {
		return contentFilter;
	}

}
