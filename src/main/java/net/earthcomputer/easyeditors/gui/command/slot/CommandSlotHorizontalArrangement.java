package net.earthcomputer.easyeditors.gui.command.slot;

/**
 * A command slot which contains multiple child command slots, arranged
 * horizontally
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotHorizontalArrangement extends CommandSlotCollection {

	public CommandSlotHorizontalArrangement(IGuiCommandSlot... children) {
		super(children);
	}

	@Override
	protected void recalcWidth() {
		int width = 0;
		for (int i = 0; i < size(); i++) {
			int w = getChildAt(i).getWidth();
			if (w > 0) {
				width += 2 + w;
			}
		}
		if (width > 0)
			width -= 2;
		setWidth(width);
	}

	@Override
	protected void recalcHeight() {
		int height = 0;
		for (int i = 0; i < size(); i++) {
			int h = getChildAt(i).getHeight();
			if (h > height)
				height = h;
		}
		setHeight(height);
	}

	@Override
	protected int[] getXPosChildren() {
		int[] xs = new int[size()];
		int width = 0;
		for (int i = 0; i < xs.length; i++) {
			IGuiCommandSlot child = getChildAt(i);
			xs[i] = width;
			if (child.getWidth() > 0) {
				width += child.getWidth() + 2;
			}
		}
		return xs;
	}

	@Override
	protected int[] getYPosChildren() {
		int[] ys = new int[size()];
		for (int i = 0; i < ys.length; i++) {
			ys[i] = getHeight() / 2 - getChildAt(i).getHeight() / 2;
		}
		return ys;
	}

}
