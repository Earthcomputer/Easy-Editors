package net.earthcomputer.easyeditors.api;

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

}
