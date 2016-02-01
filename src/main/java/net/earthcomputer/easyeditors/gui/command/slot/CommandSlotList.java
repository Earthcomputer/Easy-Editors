package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Instantiator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * A command slot which represents a variable-sized list of child command slots.
 * The difference is that the user can change the size of the list and insert
 * components themselves. Instead of refering to children as children, client
 * code should refer to them as entries, and thus use the {@link #getEntry(int)}
 * instead of {@link #getChildAt(int)}, {@link #entryCount()} instead of
 * {@link #size()}, etc. Attempting to manipulate children using these methods
 * may yield unexpected results
 * 
 * @author Earthcomputer
 *
 * @param <E>
 */
public class CommandSlotList<E extends IGuiCommandSlot> extends CommandSlotVerticalArrangement {

	private Instantiator<E> instantiator;
	private List<E> entries = Lists.newArrayList();

	private String insertHoverText = I18n.format("gui.commandEditor.list.insert");
	private String removeHoverText = I18n.format("gui.commandEditor.list.remove");

	private CommandSlotButton appendButton;

	/**
	 * Constructs a list
	 * 
	 * @param instantiator
	 *            - how to create a new entry, when the user presses a + button
	 * @param children
	 *            - pre-existing entries
	 */
	public CommandSlotList(Instantiator<E> instantiator, E... children) {
		super();
		this.instantiator = instantiator;
		for (E child : children) {
			addEntry(child);
		}
		addChild(appendButton = new CommandSlotButton(20, 20, "+", I18n.format("gui.commandEditor.list.append")) {
			{
				setTextColor(GuiUtils.getColorCode('2', true));
			}

			@Override
			public void onPress() {
				addEntry(CommandSlotList.this.instantiator.newInstance());
			}
		});
	}

	/**
	 * Sets the text shown when the append button is hovered over (the
	 * bottommost + button)
	 * 
	 * @param appendHoverText
	 * @return
	 */
	public CommandSlotList<E> setAppendHoverText(String appendHoverText) {
		appendButton.setHoverText(appendHoverText);
		return this;
	}

	/**
	 * Sets the text shown when the insert button is hovered over (all the +
	 * buttons except the bottommost one)
	 * 
	 * @param insertHoverText
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CommandSlotList<E> setInsertHoverText(String insertHoverText) {
		this.insertHoverText = insertHoverText;
		for (int i = 0; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry)
				((Entry) child).setInsertHoverText(insertHoverText);
		}
		return this;
	}

	/**
	 * Sets the text shown when the remove button is hovered over (the x
	 * buttons)
	 * 
	 * @param removeHoverText
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CommandSlotList<E> setRemoveHoverText(String removeHoverText) {
		this.removeHoverText = removeHoverText;
		for (int i = 0; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry)
				((Entry) child).setRemoveHoverText(removeHoverText);
		}
		return this;
	}

	/**
	 * 
	 * @return The number of entries
	 */
	public int entryCount() {
		return entries.size();
	}

	/**
	 * Clears all entries
	 */
	public void clearEntries() {
		entries.clear();
		for (int i = size() - 1; i >= 0; i--) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry)
				removeChildAt(i);
		}
	}

	/**
	 * Adds a new entry at the end of the list of entries
	 * 
	 * @param entry
	 */
	public void addEntry(E entry) {
		int index = entries.size();
		entries.add(entry);
		addChild(index, new Entry(index));
	}

	/**
	 * Inserts an entry before the given index
	 * 
	 * @param index
	 * @param entry
	 */
	public void addEntry(int index, E entry) {
		entries.add(index, entry);
		addChild(index, new Entry(index));
		for (int i = index + 1; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry) {
				@SuppressWarnings("unchecked")
				Entry childEntry = (Entry) getChildAt(i);
				childEntry.setIndex(childEntry.getIndex() + 1);
			}
		}
	}

	/**
	 * Removes the given entry
	 * 
	 * @param entry
	 */
	public void removeEntry(E entry) {
		int index = entries.indexOf(entry);
		entries.remove(entry);
		removeChildAt(index);
		for (int i = index; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry) {
				@SuppressWarnings("unchecked")
				Entry childEntry = (Entry) getChildAt(i);
				childEntry.setIndex(childEntry.getIndex() - 1);
			}
		}
	}

	/**
	 * Removes the entry at the given index
	 * 
	 * @param index
	 */
	public void removeEntry(int index) {
		entries.remove(index);
		removeChildAt(index);
		for (int i = index; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry) {
				@SuppressWarnings("unchecked")
				Entry childEntry = (Entry) getChildAt(i);
				childEntry.setIndex(childEntry.getIndex() - 1);
			}
		}
	}

	/**
	 * 
	 * @param index
	 * @return The entry at the given index
	 */
	public E getEntry(int index) {
		return entries.get(index);
	}

	private class Entry extends CommandSlotHorizontalArrangement {

		private int ind;

		private CommandSlotButton insertButton;
		private CommandSlotButton removeButton;

		public Entry(int ind) {
			this.ind = ind;
			addChild(insertButton = new CommandSlotButton(20, 20, "+", insertHoverText) {
				{
					setTextColor(GuiUtils.getColorCode('2', true));
				}

				@Override
				public void onPress() {
					addEntry(Entry.this.ind, instantiator.newInstance());
				}
			});
			addChild(removeButton = new CommandSlotButton(20, 20, "x", removeHoverText) {
				{
					setTextColor(GuiUtils.getColorCode('c', true));
				}

				@Override
				public void onPress() {
					removeEntry(Entry.this.ind);
				}

			});
			addChild(entries.get(ind));
		}

		public int getIndex() {
			return ind;
		}

		public void setIndex(int ind) {
			this.ind = ind;
		}

		public void setInsertHoverText(String insertHoverText) {
			insertButton.setHoverText(insertHoverText);
		}

		public void setRemoveHoverText(String removeHoverText) {
			removeButton.setHoverText(removeHoverText);
		}

	}

}
