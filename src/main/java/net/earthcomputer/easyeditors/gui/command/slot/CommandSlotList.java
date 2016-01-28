package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.Instantiator;
import net.minecraftforge.fml.client.config.GuiUtils;

public class CommandSlotList<E extends IGuiCommandSlot> extends CommandSlotVerticalArrangement {

	private Instantiator<E> instantiator;
	private List<E> entries = Lists.newArrayList();

	public CommandSlotList(Instantiator<E> instantiator, E... children) {
		super();
		this.instantiator = instantiator;
		for (E child : children) {
			addEntry(child);
		}
		addChild(new CommandSlotButton(20, 20, "+") {
			{
				setTextColor(GuiUtils.getColorCode('2', true));
			}

			@Override
			public void onPress() {
				addEntry(CommandSlotList.this.instantiator.newInstance());
			}
		});
	}

	public int entryCount() {
		return entries.size();
	}

	public void clearEntries() {
		entries.clear();
		for (int i = size() - 1; i >= 0; i--) {
			IGuiCommandSlot child = getChildAt(i);
			if (child instanceof CommandSlotList.Entry)
				removeChildAt(i);
		}
	}

	public void addEntry(E entry) {
		int index = entries.size();
		entries.add(entry);
		addChild(index, new Entry(index));
	}

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

	public E getEntry(int index) {
		return entries.get(index);
	}

	private class Entry extends CommandSlotHorizontalArrangement {

		private int ind;

		public Entry(int ind) {
			this.ind = ind;
			addChild(new CommandSlotButton(20, 20, "+") {
				{
					setTextColor(GuiUtils.getColorCode('2', true));
				}

				@Override
				public void onPress() {
					addEntry(Entry.this.ind, instantiator.newInstance());
				}
			});
			addChild(new CommandSlotButton(20, 20, "x") {
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

	}

}
