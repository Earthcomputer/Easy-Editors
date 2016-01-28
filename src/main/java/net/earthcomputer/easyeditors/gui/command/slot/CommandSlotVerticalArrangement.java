package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.Arrays;

/**
 * A command slot which contains multiple child command slots, arranged
 * vertically
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotVerticalArrangement extends CommandSlotCollection {

	public CommandSlotVerticalArrangement(IGuiCommandSlot... children) {
		super(children);
	}

	@Override
	protected void recalcWidth() {
		int width = 0;
		for (int i = 0; i < size(); i++) {
			int w = getChildAt(i).getWidth();
			if (w > width)
				width = w;
		}
		setWidth(width);
	}

	@Override
	protected void recalcHeight() {
		int height = 0;
		for (int i = 0; i < size(); i++) {
			int h = getChildAt(i).getHeight();
			if (h > 0) {
				height += 2 + h;
			}
		}
		if (height > 0)
			height -= 2;
		setHeight(height);
	}
	
	@Override
	protected int[] getXPosChildren() {
		int[] xs = new int[size()];
		Arrays.fill(xs, 0);
		return xs;
	}
	
	@Override
	protected int[] getYPosChildren() {
		int[] ys = new int[size()];
		int height = 0;
		for (int i = 0; i < ys.length; i++) {
			IGuiCommandSlot child = getChildAt(i);
			ys[i] = height;
			if (child.getHeight() > 0) {
				height += child.getHeight() + 2;
			}
		}
		return ys;
	}

}
