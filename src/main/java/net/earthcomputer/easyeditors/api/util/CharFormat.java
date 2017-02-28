package net.earthcomputer.easyeditors.api.util;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import net.minecraft.util.text.TextFormatting;

/**
 * Represents the format of a single character, represents the possible
 * combinations of {@link TextFormatting}s.
 * 
 * <b>This class is a member of the Easy Editors API</code>
 * 
 * @author Earthcomputer
 * @see TextFormatting
 * @see FormattedText
 */
public class CharFormat {

	/**
	 * Contains the <code>CharFormat</code> with all the style flags set to
	 * false
	 */
	public static final CharFormat NO_STYLE = new CharFormat(false, false, false, false, false, null);

	private boolean bold;
	private boolean italic;
	private boolean underlined;
	private boolean strikethrough;
	private boolean obfuscated;
	private TextFormatting color;

	public CharFormat(boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated,
			@Nullable TextFormatting color) {
		this.bold = bold;
		this.italic = italic;
		this.underlined = underlined;
		this.strikethrough = strikethrough;
		this.obfuscated = obfuscated;
		this.color = color;
	}

	/**
	 * Gets whether this <code>CharFormat</code> is bold
	 * 
	 * @return
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * Gets whether this <code>CharFormat</code> is italic
	 * 
	 * @return
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * Gets whether this <code>CharFormat</code> is underlined
	 * 
	 * @return
	 */
	public boolean isUnderlined() {
		return underlined;
	}

	/**
	 * Gets whether this <code>CharFormat</code> has a strikethrough
	 * 
	 * @return
	 */
	public boolean isStrikethrough() {
		return strikethrough;
	}

	/**
	 * Gets whether this <code>CharFormat</code> has the obfuscated format
	 * 
	 * @return
	 */
	public boolean isObfuscated() {
		return obfuscated;
	}

	/**
	 * Gets the color of this <code>CharFormat</code>, or <code>null</code> for
	 * the default color
	 * 
	 * @return
	 */
	@Nullable
	public TextFormatting getColor() {
		return color;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof CharFormat)) {
			return false;
		} else {
			CharFormat other = (CharFormat) obj;
			return bold == other.bold && italic == other.italic && underlined == other.underlined
					&& strikethrough == other.strikethrough && obfuscated == other.obfuscated
					&& Objects.equal(color, other.color);
		}
	}

	@Override
	public int hashCode() {
		int hash = (bold ? 1 : 0) | (italic ? 2 : 0) | (underlined ? 4 : 0) | (strikethrough ? 8 : 0)
				| (obfuscated ? 16 : 0);
		hash = (color == null ? 0 : color.hashCode()) + 31 * hash;
		return hash;
	}

	/**
	 * A utility class for changing only a few flags in a
	 * <code>CharFormat</code>
	 * 
	 * @author Earthcomputer
	 */
	public static class Builder {
		private boolean bold;
		private boolean italic;
		private boolean underlined;
		private boolean strikethrough;
		private boolean obfuscated;
		private TextFormatting color;

		/**
		 * Initializes a builder as a copy of the given <code>CharFormat</code>
		 * 
		 * @param other
		 */
		public Builder(CharFormat other) {
			this.bold = other.bold;
			this.italic = other.italic;
			this.underlined = other.underlined;
			this.strikethrough = other.strikethrough;
			this.obfuscated = other.obfuscated;
			this.color = other.color;
		}

		/**
		 * Sets whether the copy should be bold
		 * 
		 * @param bold
		 * @return
		 */
		public Builder setBold(boolean bold) {
			this.bold = bold;
			return this;
		}

		/**
		 * Sets whether the copy should be italic
		 * 
		 * @param italic
		 * @return
		 */
		public Builder setItalic(boolean italic) {
			this.italic = italic;
			return this;
		}

		/**
		 * Sets whether the copy should be underlined
		 * 
		 * @param underlined
		 * @return
		 */
		public Builder setUnderlined(boolean underlined) {
			this.underlined = underlined;
			return this;
		}

		/**
		 * Sets whether the copy should have a strikethrough
		 * 
		 * @param strikethrough
		 * @return
		 */
		public Builder setStrikethrough(boolean strikethrough) {
			this.strikethrough = strikethrough;
			return this;
		}

		/**
		 * Sets whether the copy should be obfuscated
		 * 
		 * @param obfuscated
		 * @return
		 */
		public Builder setObfuscated(boolean obfuscated) {
			this.obfuscated = obfuscated;
			return this;
		}

		/**
		 * Sets the new color of the copy. Use <code>null</code> for the default
		 * color
		 * 
		 * @param color
		 * @return
		 */
		public Builder setColor(@Nullable TextFormatting color) {
			this.color = color;
			return this;
		}

		/**
		 * Creates the immutable <code>CharFormat</code>
		 * 
		 * @return
		 */
		public CharFormat build() {
			return new CharFormat(bold, italic, underlined, strikethrough, obfuscated, color);
		}
	}

}
