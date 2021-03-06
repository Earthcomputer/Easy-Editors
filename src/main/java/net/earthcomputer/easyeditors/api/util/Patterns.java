package net.earthcomputer.easyeditors.api.util;

import java.util.regex.Pattern;

/**
 * A class containing a number of {@link Pattern}s useful to Easy Editors.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class Patterns {

	public static final Pattern partialPlayerName = Pattern.compile("\\w{0,32}");

	public static final Pattern playerName = Pattern.compile("\\w{1,32}");

	public static final Pattern UUID = Pattern.compile("[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}");

	public static final Pattern partialUUID = Pattern
			.compile("[0-9a-fA-F]{0,8}-(?:[0-9a-fA-F]{0,4}-){3}[0-9a-fA-F]{0,12}");

	public static final Pattern playerSelector = Pattern.compile("@([pare])(?:\\[([\\w\\.=,!-]*)\\])?");

	public static final Pattern partialInteger = Pattern.compile("[+-]?[0-9]*");

	public static final Pattern integer = Pattern.compile("[+-]?[0-9]+");

	public static final Pattern partialDouble = Pattern
			.compile("[+-]?(?:0[Xx])?[0-9a-fA-F]*\\.?[0-9a-fA-F]*(?:[EePp][+-]?[0-9a-fA-F]*)?");

	public static final Pattern _double = Pattern.compile(
			// decimal
			"(?:[+-]?(?:(?:[0-9]+\\.?[0-9]*)|(?:\\.[0-9]+))(?:[Ee][+-]?[0-9]+)?)|" +
					// hexadecimal
					"(?:[+-]?0[Xx](?:(?:[0-9a-fA-F]+\\.?[0-9a-fA-F]*)|(?:\\.[0-9a-fA-F]+))(?:[Pp][+-]?[0-9a-fA-F]+)?)");

}
