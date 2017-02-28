package net.earthcomputer.easyeditors.api.util;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.util.text.TextFormatting;

/**
 * A formatted String which can represent all types of text formatting found in
 * Minecraft. Contains convenience methods for working on these Strings.<br/>
 * <br/>
 * A <code>FormattedText</code> is split into the unformatted String and the
 * format, which, internally, is stored as an array of {@link CharFormats}.
 * 
 * <p>
 * <b>This class is a member of the Easy Editors API</b>
 * </p>
 * 
 * @author Earthcomputer
 * @see TextFormatting
 * @see CharFormat
 * @see net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField
 *      CommandSlotFormattedTextField
 */
public final class FormattedText {

	/**
	 * The formatted text with length zero
	 */
	public static final FormattedText EMPTY = new FormattedText("");

	private String unformattedText;
	private CharFormat[] charFormats;

	/**
	 * Creates a <code>FormattedText</code> with the given unformatted text and
	 * with every character's style set to {@link CharFormat#NO_STYLE}
	 * 
	 * @param unformattedText
	 */
	public FormattedText(String unformattedText) {
		this.unformattedText = unformattedText;
		charFormats = new CharFormat[unformattedText.length()];
		Arrays.fill(charFormats, CharFormat.NO_STYLE);
	}

	private FormattedText(String unformattedText, CharFormat[] charFormats) {
		if (unformattedText.length() != charFormats.length) {
			throw new IllegalArgumentException(String.format("unformattedText.length() (%d) != charFormats.length (%d)",
					unformattedText.length(), charFormats.length));
		}
		this.unformattedText = unformattedText;
		this.charFormats = charFormats;
	}

	////// CONVERSION METHODS //////

	/**
	 * Creates a <code>FormattedText</code> from a String with formatting codes
	 * which a {@link net.minecraft.client.gui.FontRenderer FontRenderer} would
	 * use.
	 * 
	 * <p>
	 * <code>FontRenderer</code>s scan through the String, setting internal
	 * flags as they meet certain <i>formatting codes</i>. All formatting codes
	 * consist of the section sign (§, U+00A7), followed by a single character,
	 * depending on which formatting code it is. This character is case
	 * insensitive. Every formatting code has a corresponding constant in
	 * {@link TextFormatting}.
	 * </p>
	 * <p>
	 * The formatting codes with characters 0 through 9 and A through F
	 * represent the 16 dye colors in Minecraft. When the
	 * <code>FontRenderer</code> reaches one of these formatting codes, it sets
	 * an internal field to the corresponding <code>TextFormatting</code>, and
	 * text will be rendered in this color from that point forth until either
	 * the color is changed again or the formatting is reset using the reset
	 * formatting code (see below). As well as changing the color, a color
	 * formatting code also resets all the other flags to false, so they will
	 * need to be respecified after changing the color.
	 * </p>
	 * <p>
	 * Here are the other formatting codes and what they do:
	 * <ul>
	 * <li>Bold (§L) - sets the flag representing whether the text will be bold
	 * to true. Text will then be rendered bold until the reset formatting code
	 * is reached, or the color is changed</li>
	 * <li>Italic (§O) - sets the flag representing whether the text will be
	 * italic to true. Text will then be rendered italic until the reset
	 * formatting code is reached, or the color is changed</li>
	 * <li>Underline (§N) - sets the flag representing whether the text will be
	 * underlined to true. Text will then be rendered underlined until the reset
	 * formatting code is reached, or the color is changed</li>
	 * <li>Strikethrough (§M) - sets the flag representing whether the text will
	 * have a strikethrough to true. Text will then be rendered with a
	 * strikethrough until the reset formatting code is reached, or the color is
	 * changed</li>
	 * <li>Obfuscated (§K) - sets the flag representing whether the text will be
	 * obfuscated to true. Every character in the text, until the reset
	 * formatting code is reached or the color is changed, will then be randomly
	 * replaced by another character of the same width every frame</li>
	 * <li>Reset (§R) - Sets every flag back to their defaults, including the
	 * color. The default color is the color that was specified to the
	 * <code>FontRenderer</code> before the text was drawn</li>
	 * </ul>
	 * </p>
	 * 
	 * @param vanillaText
	 * @return
	 * @see net.minecraft.client.gui.FontRenderer FontRenderer
	 * @see TextFormatting
	 * @see #toVanillaText()
	 */
	public static FormattedText compile(String vanillaText) {
		boolean bold = false;
		boolean italic = false;
		boolean underlined = false;
		boolean strikethrough = false;
		boolean obfuscated = false;
		TextFormatting color = null;

		String unformattedText = TextFormatting.getTextWithoutFormattingCodes(vanillaText);
		CharFormat[] charFormats = new CharFormat[unformattedText.length()];

		int finalIdx = 0;
		for (int i = 0, e = vanillaText.length(); i < e; i++) {
			if (vanillaText.charAt(i) == '\u00a7' && i + 1 < e) {
				char formattingCode = Character.toLowerCase(vanillaText.charAt(i + 1));
				if ((formattingCode >= '0' && formattingCode <= '9')
						|| (formattingCode >= 'a' && formattingCode <= 'f')) {
					bold = false;
					italic = false;
					underlined = false;
					strikethrough = false;
					obfuscated = false;
					color = TextFormatting.fromColorIndex("0123456789abcdef".indexOf(formattingCode));
				} else if (formattingCode == 'k') {
					obfuscated = true;
				} else if (formattingCode == 'l') {
					bold = true;
				} else if (formattingCode == 'm') {
					strikethrough = true;
				} else if (formattingCode == 'n') {
					underlined = true;
				} else if (formattingCode == 'o') {
					italic = true;
				} else if (formattingCode == 'r') {
					bold = false;
					italic = false;
					underlined = false;
					strikethrough = false;
					obfuscated = false;
					color = null;
				}
				i++;
			} else {
				charFormats[finalIdx] = new CharFormat(bold, italic, underlined, strikethrough, obfuscated, color);
				finalIdx++;
			}
		}
		return new FormattedText(unformattedText, charFormats);
	}

	/**
	 * Returns the shortest String representation of this formatted text with
	 * formatting codes recognized by a <code>FontRenderer</code>. See
	 * {@link #compile(String)} for an explanation of how these formatting codes
	 * work.
	 * 
	 * <p>
	 * Note that this method may append a reset formatting code to the end of
	 * the returned String. This is a workaround for a vanilla bug where the
	 * <code>FontRenderer</code> does not automatically reset its internal flags
	 * when drawing a string with a shadow, in between drawing the shadow and
	 * drawing the main string.
	 * </p>
	 * 
	 * @return
	 */
	public String toVanillaText() {
		boolean bold = false;
		boolean italic = false;
		boolean underlined = false;
		boolean strikethrough = false;
		boolean obfuscated = false;
		TextFormatting color = null;

		StringBuilder vanillaText = new StringBuilder();

		for (int i = 0, e = unformattedText.length(); i < e; i++) {
			CharFormat charFormat = charFormats[i];
			boolean needsResetOrColorChange = false;
			boolean definitelyNeedsReset = false;
			if (bold && !charFormat.isBold()) {
				needsResetOrColorChange = true;
			} else if (italic && !charFormat.isItalic()) {
				needsResetOrColorChange = true;
			} else if (underlined && !charFormat.isUnderlined()) {
				needsResetOrColorChange = true;
			} else if (strikethrough && !charFormat.isStrikethrough()) {
				needsResetOrColorChange = true;
			} else if (obfuscated && !charFormat.isObfuscated()) {
				needsResetOrColorChange = true;
			} else if (color != null && charFormat.getColor() == null) {
				definitelyNeedsReset = true;
			}
			if ((needsResetOrColorChange && color == charFormat.getColor()) || definitelyNeedsReset) {
				vanillaText.append(TextFormatting.RESET);
				bold = false;
				italic = false;
				underlined = false;
				strikethrough = false;
				obfuscated = false;
				color = null;
			}
			if (color != charFormat.getColor()) {
				vanillaText.append(charFormat.getColor());
				bold = false;
				italic = false;
				underlined = false;
				strikethrough = false;
				obfuscated = false;
				color = charFormat.getColor();
			}
			if (!bold && charFormat.isBold()) {
				vanillaText.append(TextFormatting.BOLD);
				bold = true;
			}
			if (!italic && charFormat.isItalic()) {
				vanillaText.append(TextFormatting.ITALIC);
				italic = true;
			}
			if (!underlined && charFormat.isUnderlined()) {
				vanillaText.append(TextFormatting.UNDERLINE);
				underlined = true;
			}
			if (!strikethrough && charFormat.isStrikethrough()) {
				vanillaText.append(TextFormatting.STRIKETHROUGH);
				strikethrough = true;
			}
			if (!obfuscated && charFormat.isObfuscated()) {
				vanillaText.append(TextFormatting.OBFUSCATED);
				obfuscated = true;
			}
			vanillaText.append(unformattedText.charAt(i));
		}
		if (!isEmpty() && !charFormats[charFormats.length - 1].equals(CharFormat.NO_STYLE)) {
			vanillaText.append(TextFormatting.RESET);
		}
		return vanillaText.toString();
	}

	////// STYLE METHODS //////

	/**
	 * Returns the <code>CharFormat</code> at the given index
	 * 
	 * @param index
	 * @return
	 * @throws StringIndexOutOfBoundsException
	 *             When the index is less than 0 or past the end of the string
	 */
	public CharFormat formatAt(int index) {
		if (index < 0 || index >= charFormats.length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return charFormats[index];
	}

	/**
	 * Returns a copy of this <code>FormattedText</code> with the given
	 * <code>CharFormat</code> at the given index
	 * 
	 * @param index
	 * @param format
	 * @return
	 * @throws StringIndexOutOfBoundsException
	 *             When the index is less than 0 or past the end of the string
	 */
	public FormattedText withFormatAt(int index, CharFormat format) {
		if (index < 0 || index >= charFormats.length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		CharFormat[] newFormats = charFormats.clone();
		newFormats[index] = format;
		return new FormattedText(unformattedText, newFormats);
	}

	/**
	 * Returns true if all the <code>CharFormat</code>s between
	 * <code>selectStart</code> (inclusive) and <code>selectEnd</code>
	 * (exclusive) have the given formatting flag set to true (or are of the
	 * given color in the case of a color)
	 * 
	 * @param selectStart
	 * @param selectEnd
	 * @param style
	 * @return
	 * @throws IllegalArgumentException
	 *             If <code>selectEnd < selectStart</code>
	 * @throws StringIndexOutOfBoundsException
	 *             If <code>selectStart < 0</code> or <code>selectEnd >
	 *             length()</code>
	 */
	public boolean isSelectionOfStyle(int selectStart, int selectEnd, TextFormatting style) {
		if (selectEnd < selectStart) {
			throw new IllegalArgumentException("selectEnd < selectStart");
		}
		if (selectStart < 0 || selectEnd > unformattedText.length()) {
			throw new StringIndexOutOfBoundsException(
					"Selection from " + selectStart + " to " + selectEnd + " is not contained by this string");
		}
		if (style.isColor()) {
			for (int i = selectStart; i < selectEnd; i++) {
				if (charFormats[i].getColor() != style) {
					return false;
				}
			}
			return true;
		}
		if (style == TextFormatting.BOLD) {
			for (int i = selectStart; i < selectEnd; i++) {
				if (!charFormats[i].isBold()) {
					return false;
				}
			}
			return true;
		}
		if (style == TextFormatting.ITALIC) {
			for (int i = selectStart; i < selectEnd; i++) {
				if (!charFormats[i].isItalic()) {
					return false;
				}
			}
			return true;
		}
		if (style == TextFormatting.UNDERLINE) {
			for (int i = selectStart; i < selectEnd; i++) {
				if (!charFormats[i].isUnderlined()) {
					return false;
				}
			}
			return true;
		}
		if (style == TextFormatting.STRIKETHROUGH) {
			for (int i = selectStart; i < selectEnd; i++) {
				if (!charFormats[i].isStrikethrough()) {
					return false;
				}
			}
			return true;
		}
		if (style == TextFormatting.OBFUSCATED) {
			for (int i = selectStart; i < selectEnd; i++) {
				if (!charFormats[i].isObfuscated()) {
					return false;
				}
			}
			return true;
		}
		throw new IllegalArgumentException("Illegal style: " + style);
	}

	/**
	 * Operates on all the styles between <code>selectStart</code> (inclusive)
	 * and <code>selectEnd</code> (exclusive).
	 * <ul>
	 * <li>If the corresponding flag of all the <code>CharFormat</code>s within
	 * this range are set to true (as per
	 * {@link #isSelectionOfStyle(int, int, TextFormatting)}), then this method
	 * will set the flags of all those <code>CharFormat</code> to false</li>
	 * <li>Otherwise, the corresponding flag of all those
	 * <code>CharFormat</code>s to true</li>
	 * </ul>
	 * 
	 * @param selectStart
	 * @param selectEnd
	 * @param style
	 * @return A copy of this <code>FormattedText</code> with the style toggled
	 * @throws IllegalArgumentException
	 *             If <code>selectEnd < selectStart</code>
	 * @throws StringIndexOutOfBoundsException
	 *             If <code>selectStart < 0</code> or
	 *             <code>selectEnd > length()</code>
	 */
	public FormattedText toggleStyle(int selectStart, int selectEnd, TextFormatting style) {
		if (selectEnd < selectStart) {
			throw new IllegalArgumentException("selectEnd < selectStart");
		}
		if (selectStart < 0 || selectEnd > unformattedText.length()) {
			throw new StringIndexOutOfBoundsException(
					"Selection from " + selectStart + " to " + selectEnd + " is not contained by this string");
		}
		CharFormat.Builder[] charFormatBuilders = new CharFormat.Builder[charFormats.length];
		for (int i = 0; i < charFormatBuilders.length; i++) {
			charFormatBuilders[i] = new CharFormat.Builder(charFormats[i]);
		}
		if (style.isColor()) {
			if (isSelectionOfStyle(selectStart, selectEnd, style)) {
				style = null;
			}
			for (int i = selectStart; i < selectEnd; i++) {
				charFormatBuilders[i].setColor(style);
			}
		} else if (style == TextFormatting.BOLD) {
			boolean bold = !isSelectionOfStyle(selectStart, selectEnd, TextFormatting.BOLD);
			for (int i = selectStart; i < selectEnd; i++) {
				charFormatBuilders[i].setBold(bold);
			}
		} else if (style == TextFormatting.ITALIC) {
			boolean italic = !isSelectionOfStyle(selectStart, selectEnd, TextFormatting.ITALIC);
			for (int i = selectStart; i < selectEnd; i++) {
				charFormatBuilders[i].setItalic(italic);
			}
		} else if (style == TextFormatting.UNDERLINE) {
			boolean underline = !isSelectionOfStyle(selectStart, selectEnd, TextFormatting.UNDERLINE);
			for (int i = selectStart; i < selectEnd; i++) {
				charFormatBuilders[i].setUnderlined(underline);
			}
		} else if (style == TextFormatting.STRIKETHROUGH) {
			boolean strikethrough = !isSelectionOfStyle(selectStart, selectEnd, TextFormatting.STRIKETHROUGH);
			for (int i = selectStart; i < selectEnd; i++) {
				charFormatBuilders[i].setStrikethrough(strikethrough);
			}
		} else if (style == TextFormatting.OBFUSCATED) {
			boolean obfuscated = !isSelectionOfStyle(selectStart, selectEnd, TextFormatting.OBFUSCATED);
			for (int i = selectStart; i < selectEnd; i++) {
				charFormatBuilders[i].setObfuscated(obfuscated);
			}
		} else {
			throw new IllegalArgumentException("Invalid style: " + style);
		}
		CharFormat[] newFormats = new CharFormat[charFormatBuilders.length];
		for (int i = 0; i < newFormats.length; i++) {
			newFormats[i] = charFormatBuilders[i].build();
		}
		return new FormattedText(unformattedText, newFormats);
	}

	////// STRING METHODS //////

	/**
	 * Returns the number of characters in this <code>FormattedText</code>
	 * 
	 * @return
	 * @see String#length()
	 */
	public int length() {
		return unformattedText.length();
	}

	/**
	 * Returns true if <code>length() == 0</code>
	 * 
	 * @return
	 * @see String#isEmpty()
	 */
	public boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * Returns the character at the given index
	 * 
	 * @param index
	 * @return
	 * @throws StringIndexOutOfBoundsException
	 *             If the given index is out of bounds
	 * @see String#charAt(int)
	 */
	public char charAt(int index) {
		return unformattedText.charAt(index);
	}

	/**
	 * Returns the first index that the specified character could be found, or
	 * -1 if not found
	 * 
	 * @param c
	 * @return
	 * @see String#indexOf(int)
	 */
	public int indexOf(int c) {
		return unformattedText.indexOf(c);
	}

	/**
	 * Returns the first index that the specified String could be found, or -1
	 * if not found
	 * 
	 * @param text
	 * @return
	 * @see String#indexOf(String)
	 */
	public int indexOf(String text) {
		return unformattedText.indexOf(text);
	}

	/**
	 * Returns the first index that the given <code>FormattedText</code> could
	 * be found, or -1 if not found. Unlike other similar methods, this method
	 * also attempts to match the format, as well as the unformatted text.
	 * 
	 * @param text
	 * @return
	 * @see String#indexOf(String)
	 */
	public int indexOf(FormattedText text) {
		return indexOf(text, 0);
	}

	/**
	 * Returns the first index that the specified character could be found,
	 * greater than or equal to <code>start</code>, or -1 if not found.
	 * 
	 * @param c
	 * @param start
	 * @return
	 * @see String#indexOf(int, int)
	 */
	public int indexOf(int c, int start) {
		return unformattedText.indexOf(c, start);
	}

	/**
	 * Returns the first index that the specified text could be found, greater
	 * than or equal to <code>start</code>, or -1 if not found.
	 * 
	 * @param text
	 * @param start
	 * @return
	 * @see String#indexOf(String, int)
	 */
	public int indexOf(String text, int start) {
		return unformattedText.indexOf(text, start);
	}

	/**
	 * Returns the first index that the specified <code>FormattedText</code>
	 * could be found, greater than or equal to <code>start</code>, or -1 if not
	 * found. Unlike other similar methods, this method also attempts to match
	 * the format, as well as the unformatted text.
	 * 
	 * @param text
	 * @param start
	 * @return
	 * @see String#indexOf(String, int)
	 */
	public int indexOf(FormattedText text, int start) {
		while (true) {
			start = unformattedText.indexOf(text.unformattedText, start);
			if (start == -1) {
				return -1;
			}
			if (substring(start, start + text.length()).equals(text)) {
				return start;
			}
			start++;
			if (start == text.length()) {
				return -1;
			}
		}
	}

	/**
	 * <code>a.concat(b)</code>, where <code>a</code> is a
	 * <code>FormattedText</code> and <code>b</code> is a normal String, is the
	 * equivalent of <code>a.concat(new FormattedText(b))</code>.
	 * 
	 * @param other
	 * @return <code>this + other</code>
	 * @see #concat(FormattedText)
	 * @see String#concat(String)
	 */
	public FormattedText concat(String other) {
		return concat(new FormattedText(other));
	}

	/**
	 * Concatenates the other string onto this string. Returns the equivalent of
	 * using the <code>+</code> operator on two normal Strings in Java.
	 * 
	 * @param other
	 * @return
	 * @see String#concat(String)
	 */
	public FormattedText concat(FormattedText other) {
		return new FormattedText(unformattedText + other.unformattedText,
				ArrayUtils.addAll(charFormats, other.charFormats));
	}

	/**
	 * Returns a <code>FormattedText</code> containing all the characters and
	 * <code>CharFormat</code>s bewteen <code>start</code> (inclusive) and
	 * <code>end</code> (exclusive)
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws IllegalArgumentException
	 *             If <code>end < start</code>
	 * @see String#substring(int, int)
	 */
	public FormattedText substring(int start, int end) {
		if (end < start) {
			throw new IllegalArgumentException(String.format("end (%d) < start (%d)", end, start));
		}
		CharFormat[] newCharFormats = new CharFormat[end - start];
		System.arraycopy(charFormats, start, newCharFormats, 0, newCharFormats.length);
		return new FormattedText(unformattedText.substring(start, end), newCharFormats);
	}

	/**
	 * <code>a.substring(b)</code> is the equivalent of
	 * <code>a.substring(b, a.length())</code>
	 * 
	 * @param start
	 * @return
	 * @see #substring(int, int)
	 * @see String#substring(int)
	 */
	public FormattedText substring(int start) {
		return substring(start, length());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof FormattedText)) {
			return false;
		} else {
			FormattedText other = (FormattedText) obj;
			return unformattedText.equals(other.unformattedText) && Arrays.equals(charFormats, other.charFormats);
		}
	}

	@Override
	public int hashCode() {
		return unformattedText.hashCode() + 31 * Arrays.hashCode(charFormats);
	}

	@Override
	public String toString() {
		return toVanillaText();
	}

}
