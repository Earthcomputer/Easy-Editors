package net.earthcomputer.easyeditors.api.util;

import java.util.Iterator;
import java.util.regex.Pattern;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

/**
 * This class is here to fix two problems:<br/>
 * 1: NBTTagLists add unnecessary indexes in their toString() methods<br/>
 * 2: NBTTagStrings' speech marks are sometimes unnecessary<br/>
 * 3: NBTTagDouble and NBTTagFloat sometimes convert to an unnecessarily long
 * string<br/>
 * 4: NEI has a coremod which adds spaces after commas in various toString()
 * methods<br/>
 * This class basically does the opposite of {@link net.minecraft.nbt.JsonToNBT
 * JsonToNBT}.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class NBTToJson {

	private static final Pattern numberPattern = Pattern
			.compile("[+-]?(?:[0-9]+[bBlLsS]?)|(?:[0-9]*\\.?[0-9]+[DdFf]?)");

	private NBTToJson() {
	}

	/**
	 * Converts an NBTTagCompound to a Json string
	 * 
	 * @param tag
	 * @return
	 */
	public static String getJsonFromTag(NBTTagCompound tag) {
		return compound(new StringBuilder(), tag).toString();
	}

	private static StringBuilder any(StringBuilder sb, NBTBase nbt) {
		switch (nbt.getId()) {
		case Constants.NBT.TAG_COMPOUND:
			return compound(sb, (NBTTagCompound) nbt);
		case Constants.NBT.TAG_LIST:
			return list(sb, (NBTTagList) nbt);
		case Constants.NBT.TAG_INT_ARRAY:
			return intArray(sb, (NBTTagIntArray) nbt);
		case Constants.NBT.TAG_STRING:
			return string(sb, (NBTTagString) nbt);
		case Constants.NBT.TAG_DOUBLE:
			return _double(sb, (NBTTagDouble) nbt);
		case Constants.NBT.TAG_FLOAT:
			return _float(sb, (NBTTagFloat) nbt);
		default:
			return other(sb, nbt);
		}
	}

	private static StringBuilder compound(StringBuilder sb, NBTTagCompound compound) {
		sb.append('{');
		Iterator<String> iterator = compound.getKeySet().iterator();
		if (iterator.hasNext()) {
			String key = iterator.next();
			sb.append(key).append(':');
			any(sb, compound.getTag(key));
		}
		while (iterator.hasNext()) {
			String key = iterator.next();
			sb.append(',').append(key).append(':');
			any(sb, compound.getTag(key));
		}
		return sb.append('}');
	}

	private static StringBuilder list(StringBuilder sb, NBTTagList list) {
		sb.append('[');
		if (!list.hasNoTags()) {
			any(sb, list.get(0));
		}
		for (int i = 1; i < list.tagCount(); i++) {
			sb.append(',');
			any(sb, list.get(i));
		}
		return sb.append(']');
	}

	private static StringBuilder intArray(StringBuilder sb, NBTTagIntArray arr) {
		sb.append('[');
		int[] is = arr.getIntArray();
		if (is.length != 0) {
			sb.append(is[0]);
		}
		for (int i = 1; i < is.length; i++) {
			sb.append(',').append(is[i]);
		}
		return sb.append(']');
	}

	private static StringBuilder string(StringBuilder sb, NBTTagString str) {
		String theStr = str.getString();
		boolean requireQuotes = false;
		if (theStr.startsWith("{") || theStr.startsWith("["))
			requireQuotes = true;
		else if (numberPattern.matcher(theStr).matches() || theStr.equalsIgnoreCase("false")
				|| theStr.equalsIgnoreCase("true"))
			requireQuotes = true;
		else if (theStr.contains(",") || theStr.contains("}") || theStr.contains("]"))
			requireQuotes = true;
		else if (theStr.startsWith("\""))
			requireQuotes = true;
		if (requireQuotes)
			theStr = "\"" + theStr.replace("\"", "\\\"") + "\"";

		return sb.append(theStr);
	}

	private static StringBuilder _double(StringBuilder sb, NBTTagDouble _double) {
		double d = _double.getDouble();
		String str;
		if (d == 0)
			str = "0d";
		else if (d < 1 && d > -1)
			str = String.valueOf(d).replace("0.", ".");
		else if (d % 1 == 0)
			str = String.valueOf(d).replace(".0", "d");
		else
			str = String.valueOf(d);
		return sb.append(str);
	}

	private static StringBuilder _float(StringBuilder sb, NBTTagFloat _float) {
		float f = _float.getFloat();
		String str;
		if (f == 0)
			str = "0f";
		else if (f < 1 && f > -1)
			str = String.valueOf(f).replace("0.", ".") + "f";
		else if (f % 1 == 0)
			str = String.valueOf(f).replace(".0", "f");
		else
			str = String.valueOf(f) + "f";
		return sb.append(str);
	}

	private static StringBuilder other(StringBuilder sb, NBTBase other) {
		return sb.append(other.toString());
	}

}
