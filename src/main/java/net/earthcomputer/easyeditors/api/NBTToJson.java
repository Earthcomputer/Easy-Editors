package net.earthcomputer.easyeditors.api;

import java.util.Iterator;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * This class is here to fix two problems:<br/>
 * 1: NBTTagLists add unnecessary indexes in their toString() methods<br/>
 * 2: NEI has a coremod which adds spaces after commas in various toString()
 * methods<br/>
 * This class basically does the opposite of {@link net.minecraft.nbt.JsonToNBT
 * JsonToNBT}. <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class NBTToJson {

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

	private static StringBuilder other(StringBuilder sb, NBTBase other) {
		return sb.append(other.toString());
	}

}
