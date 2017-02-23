package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.BlockPropertyRegistry;
import net.earthcomputer.easyeditors.api.util.AnimatedBlockRenderer;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.api.util.NBTToJson;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiBlockSelector;
import net.earthcomputer.easyeditors.gui.command.IBlockSelectorCallback;
import net.earthcomputer.easyeditors.gui.command.NBTTagHandler;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.PropertyFloat;
import net.minecraftforge.fml.client.config.HoverChecker;

public class CommandSlotBlock extends CommandSlotVerticalArrangement implements IBlockSelectorCallback {

	public static final int COMPONENT_BLOCK = 1;
	public static final int COMPONENT_PROPERTIES = 2;
	public static final int COMPONENT_NBT = 4;

	private boolean isTest;
	private int[] argOrder;
	private int optionalStart;

	private Block block;
	private IBlockState displayedState;
	private Map<IProperty<?>, CommandSlotCheckbox> variantsIgnoring;
	private Map<IProperty<?>, IPropertyControl<?>> nonVariants;
	private List<NBTTagHandler> nbtHandlers;

	private CommandSlotModifiable<IGuiCommandSlot> properties;
	private CommandSlotModifiable<IGuiCommandSlot> nbt;

	public CommandSlotBlock(boolean isTest, int optionalStart, int... argOrder) {
		this.isTest = isTest;
		this.optionalStart = optionalStart;
		this.argOrder = argOrder;
		int displayComponents = 0;
		for (int component : argOrder) {
			displayComponents |= component;
		}
		final int finalDisplayComponents = displayComponents;

		addChild(new CommandSlotHorizontalArrangement(new CmdBlock(), new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiBlockSelector(Minecraft.getMinecraft().currentScreen,
						CommandSlotBlock.this, (finalDisplayComponents & (COMPONENT_PROPERTIES | COMPONENT_NBT)) != 0));
			}
		}));

		if ((displayComponents & COMPONENT_PROPERTIES) != 0) {
			addChild(properties = new CommandSlotModifiable<IGuiCommandSlot>(null));
		}

		if ((displayComponents & COMPONENT_NBT) != 0) {
			addChild(nbt = new CommandSlotModifiable<IGuiCommandSlot>(null));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index + optionalStart > args.length) {
			throw new CommandSyntaxException();
		}

		Block block = null;
		String propertiesArg = null;
		NBTTagCompound nbt = null;

		int argsConsumed = 0;
		for (int i = 0; i < argOrder.length && i < args.length - index; i++) {
			argsConsumed++;
			switch (argOrder[i]) {
			case COMPONENT_BLOCK:
				block = Block.getBlockFromName(args[index + i]);
				break;
			case COMPONENT_PROPERTIES:
				propertiesArg = args[index + i];
				break;
			case COMPONENT_NBT:
				if (i != argOrder.length - 1) {
					throw new IllegalStateException(
							"Invalid position of COMPONENT_NBT in argOrder. Must be last element");
				}
				Joiner joiner = Joiner.on(' ');
				String[] nbtArgs = new String[args.length - index - i];
				System.arraycopy(args, index + i, nbtArgs, 0, nbtArgs.length);
				try {
					nbt = JsonToNBT.getTagFromJson(joiner.join(nbtArgs));
				} catch (NBTException e) {
					throw new CommandSyntaxException();
				}
				break;
			default:
				throw new IllegalStateException("Invalid value in argOrder: " + argOrder[i]);
			}
		}

		if (block == null) {
			throw new CommandSyntaxException();
		}
		Map<IProperty<?>, Comparable<?>> properties = isTest ? Maps.<IProperty<?>, Comparable<?>> newHashMap()
				: Maps.newHashMap(block.getDefaultState().getProperties());
		if (propertiesArg != null) {
			if (!isTest || (!"-1".equals(propertiesArg) && !"*".equals(propertiesArg))) {
				try {
					int meta = Integer.parseInt(propertiesArg);
					if (meta < 0 || meta > 15) {
						throw new CommandSyntaxException();
					}
					properties = Maps.newHashMap(block.getStateFromMeta(meta).getProperties());
				} catch (NumberFormatException e) {
					if ("default".equals(propertiesArg)) {
						properties = Maps.newHashMap(block.getDefaultState().getProperties());
					} else {
						for (String property : propertiesArg.split(",")) {
							String[] keyAndValue = property.split("=");
							if (keyAndValue.length < 2) {
								throw new CommandSyntaxException();
							}
							IProperty<?> key = block.getBlockState().getProperty(keyAndValue[0]);
							if (key == null) {
								throw new CommandSyntaxException();
							}
							Comparable<?> value = key.parseValue(keyAndValue[1]).orNull();
							if (value == null) {
								throw new CommandSyntaxException();
							}
							properties.put(key, value);
						}
					}
				}
			}
		}

		IBlockState state = block.getDefaultState();
		for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
			state = putProperty(state, entry.getKey(), entry.getValue());
		}

		setBlock(state);
		if (isTest && this.properties != null) {
			for (Map.Entry<IProperty<?>, CommandSlotCheckbox> entry : variantsIgnoring.entrySet()) {
				if (!properties.containsKey(entry.getKey())) {
					entry.getValue().setChecked(true);
				}
			}
			for (Map.Entry<IProperty<?>, IPropertyControl<?>> entry : nonVariants.entrySet()) {
				if (!properties.containsKey(entry.getKey())) {
					entry.getValue().setIgnoringProperty();
				}
			}
		}

		if (nbt != null) {
			NBTTagHandler.readFromNBT(nbt, nbtHandlers);
		}

		if (nbt != null) {
			return args.length - index;
		}
		return argsConsumed;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> IBlockState putProperty(IBlockState state, IProperty<T> key,
			Comparable<?> value) {
		return state.withProperty(key, (T) value);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String getValueName(IProperty<T> key, Comparable<?> value) {
		return key.getName((T) value);
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();

		List<String> potentialArgs = Lists.newArrayList();
		int maxElementToCopy = 0;
		for (int i = 0; i < argOrder.length; i++) {
			switch (argOrder[i]) {
			case COMPONENT_BLOCK:
				ResourceLocation blockName = block.delegate.name();
				potentialArgs.add(GeneralUtils.resourceLocationToString(blockName));
				maxElementToCopy = i;
				break;
			case COMPONENT_PROPERTIES:
				Map<IProperty<?>, Comparable<?>> properties = Maps.newHashMap();
				if (isTest) {
					for (Map.Entry<IProperty<?>, CommandSlotCheckbox> variantIgnoring : variantsIgnoring.entrySet()) {
						if (!variantIgnoring.getValue().isChecked()) {
							properties.put(variantIgnoring.getKey(),
									this.displayedState.getValue(variantIgnoring.getKey()));
						}
					}
				} else {
					properties = Maps.newHashMap(this.displayedState.getProperties());
				}
				for (Map.Entry<IProperty<?>, IPropertyControl<?>> entry : nonVariants.entrySet()) {
					if (!entry.getValue().isIgnoringProperty()) {
						properties.put(entry.getKey(), entry.getValue().getSelectedValue());
					}
				}
				boolean _default;
				if (!isTest) {
					IBlockState state = block.getDefaultState();
					for (Map.Entry<IProperty<?>, Comparable<?>> prop : properties.entrySet()) {
						state = putProperty(state, prop.getKey(), prop.getValue());
					}
					_default = state.equals(block.getDefaultState());
					potentialArgs.add(String.valueOf(block.getMetaFromState(state)));
				} else {
					if (properties.isEmpty()) {
						potentialArgs.add("*");
						_default = true;
					} else if (properties.size() == this.displayedState.getProperties().size()) {
						IBlockState state = block.getDefaultState();
						for (Map.Entry<IProperty<?>, Comparable<?>> prop : properties.entrySet()) {
							state = putProperty(state, prop.getKey(), prop.getValue());
						}
						potentialArgs.add(String.valueOf(block.getMetaFromState(state)));
						_default = false;
					} else {
						Joiner joiner = Joiner.on(',');
						List<String> props = Lists.newArrayList();
						for (Map.Entry<IProperty<?>, Comparable<?>> prop : properties.entrySet()) {
							props.add(prop.getKey().getName() + "=" + getValueName(prop.getKey(), prop.getValue()));
						}
						potentialArgs.add(joiner.join(props));
						_default = false;
					}
				}
				if (i < optionalStart || !_default) {
					maxElementToCopy = i;
				}
				break;
			case COMPONENT_NBT:
				NBTTagCompound nbt = getNbt();
				if (i < optionalStart || nbt != null) {
					maxElementToCopy = i;
				}
				potentialArgs.add(nbt == null ? "{}" : NBTToJson.getJsonFromTag(nbt));
				break;
			}
		}
		potentialArgs = potentialArgs.subList(0, maxElementToCopy + 1);
		args.addAll(potentialArgs);
	}

	public void checkValid() throws UIInvalidException {
		if (block == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOBLOCKSELECTED);
		}
		for (IPropertyControl<?> property : nonVariants.values()) {
			property.checkValid();
		}
		if (nbtHandlers != null) {
			for (NBTTagHandler handler : nbtHandlers) {
				handler.checkValid();
			}
		}
	}

	@Override
	public IBlockState getBlock() {
		return displayedState;
	}

	@Override
	public void setBlock(IBlockState block) {
		boolean sameBlock = this.block == block.getBlock();
		this.block = block.getBlock();
		this.displayedState = block;

		if (!sameBlock && this.properties != null) {
			List<IProperty<?>> properties = Lists.newArrayList(block.getPropertyKeys());
			List<IProperty<? extends Comparable<?>>> variantProperties = BlockPropertyRegistry
					.getVariantProperties(block.getBlock());
			CommandSlotVerticalArrangement propertiesCommandSlot = new CommandSlotVerticalArrangement();
			if (isTest) {
				variantsIgnoring = Maps.newHashMap();
				for (IProperty<? extends Comparable<?>> variantProp : variantProperties) {
					CommandSlotCheckbox checkbox = new CommandSlotCheckbox(variantProp.getName());
					propertiesCommandSlot.addChild(checkbox);
					variantsIgnoring.put(variantProp, checkbox);
				}
			} else {
				variantsIgnoring = null;
			}
			properties.removeAll(variantProperties);
			nonVariants = Maps.newHashMap();
			for (IProperty<?> prop : properties) {
				IPropertyControl<?> control = createPropertyControl(prop, isTest);
				setControlSelectedValue(control, block.getValue(prop));
				propertiesCommandSlot.addChild(control.getCommandSlot());
				nonVariants.put(prop, control);
			}
			this.properties.setChild(propertiesCommandSlot);
		}

		if (this.nbt != null) {
			if (block.getBlock().hasTileEntity(block)) {
				TileEntity te = block.getBlock().createTileEntity(Minecraft.getMinecraft().world, block);
				nbtHandlers = te == null ? null : NBTTagHandler.constructTileEntityHandlers(te.getClass());
			} else {
				nbtHandlers = null;
			}
			if (nbtHandlers == null) {
				this.nbt.setChild(null);
			} else {
				this.nbt.setChild(NBTTagHandler.setupCommandSlot(nbtHandlers));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> void setControlSelectedValue(IPropertyControl<T> control,
			Comparable<?> value) {
		control.setSelectedValue((T) value);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> IPropertyControl<T> createPropertyControl(IProperty<T> prop,
			boolean isTest) {
		if (prop instanceof PropertyInteger) {
			int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
			for (int allowedVal : ((PropertyInteger) prop).getAllowedValues()) {
				if (allowedVal < min) {
					min = allowedVal;
				}
				if (allowedVal > max) {
					max = allowedVal;
				}
			}
			return (IPropertyControl<T>) new IntPropertyControl(prop.getName(), min, max, isTest);
		} else if (prop instanceof PropertyFloat) {
			final PropertyFloat propFloat = (PropertyFloat) prop;
			return (IPropertyControl<T>) new FloatPropertyControl(propFloat.getName(), new Predicate<Float>() {
				@Override
				public boolean apply(Float val) {
					return propFloat.isValid(val);
				}
			}, isTest);
		} else if (prop instanceof PropertyBool) {
			if (isTest) {
				return (IPropertyControl<T>) new TestBooleanPropertyControl(prop.getName());
			} else {
				return (IPropertyControl<T>) new BooleanPropertyControl(prop.getName());
			}
		} else {
			return new PropertyControl<T>(prop.getName(), prop, isTest);
		}
	}

	public NBTTagCompound getNbt() {
		if (this.nbt == null) {
			return null;
		}
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagHandler.writeToNBT(nbt, nbtHandlers);
		return nbt.hasNoTags() ? null : nbt;
	}

	private class CmdBlock extends GuiCommandSlotImpl {

		private HoverChecker hoverChecker;

		public CmdBlock() {
			super(18 + Minecraft.getMinecraft().fontRendererObj.getStringWidth(Translate.GUI_COMMANDEDITOR_NOBLOCK),
					Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT > 16
							? Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT : 16);
		}

		@Override
		public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			if (displayedState != null) {
				new AnimatedBlockRenderer(displayedState).render(x,
						fontRenderer.FONT_HEIGHT > 16 ? y + fontRenderer.FONT_HEIGHT / 2 - 8 : y);
			}

			fontRenderer.drawString(getDisplayText(), x + 18,
					fontRenderer.FONT_HEIGHT > 16 ? y : y + 8 - fontRenderer.FONT_HEIGHT / 2,
					displayedState == null ? Colors.invalidItemName.color : Colors.itemName.color);

			if (displayedState != null) {
				if (hoverChecker == null) {
					hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
				} else {
					hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());
				}

				if (!getContext().isMouseInBounds(mouseX, mouseY)) {
					hoverChecker.resetHoverTimer();
				} else if (hoverChecker.checkHover(mouseX, mouseY)) {
					drawTooltip(mouseX, mouseY, GuiBlockSelector.getTooltip(displayedState));
				}
			}
		}

		private String getDisplayText() {
			if (displayedState == null) {
				return Translate.GUI_COMMANDEDITOR_NOBLOCK;
			} else {
				return GuiBlockSelector.getDisplayName(displayedState);
			}
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			return 0;
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
		}

	}

	private static interface IPropertyControl<T extends Comparable<T>> {
		boolean isIgnoringProperty();

		void setIgnoringProperty();

		T getSelectedValue();

		void setSelectedValue(T value);

		void checkValid() throws UIInvalidException;

		IGuiCommandSlot getCommandSlot();
	}

	private static class IntPropertyControl implements IPropertyControl<Integer> {

		private String propertyName;
		private boolean isTest;
		private CommandSlotIntTextField value;

		public IntPropertyControl(String propertyName, int min, int max, boolean isTest) {
			this.propertyName = propertyName;
			this.isTest = isTest;
			this.value = new CommandSlotIntTextField(50, 100, min, max);
		}

		@Override
		public boolean isIgnoringProperty() {
			return isTest && value.getText().isEmpty();
		}

		@Override
		public void setIgnoringProperty() {
			value.setText("");
		}

		@Override
		public Integer getSelectedValue() {
			return value.getIntValue();
		}

		@Override
		public void setSelectedValue(Integer value) {
			this.value.setText(String.valueOf(value));
		}

		@Override
		public void checkValid() throws UIInvalidException {
			if (!isIgnoringProperty()) {
				value.checkValid();
			}
		}

		@Override
		public IGuiCommandSlot getCommandSlot() {
			return CommandSlotLabel.createLabel(propertyName, value);
		}

	}

	private static class FloatPropertyControl implements IPropertyControl<Float> {

		private String propertyName;
		private Predicate<Float> validator;
		private boolean isTest;
		private CommandSlotNumberTextField value;

		public FloatPropertyControl(String propertyName, Predicate<Float> validator, boolean isTest) {
			this.propertyName = propertyName;
			this.validator = validator;
			this.isTest = isTest;
			this.value = new CommandSlotNumberTextField(50, 100);
		}

		@Override
		public boolean isIgnoringProperty() {
			return isTest && value.getText().isEmpty();
		}

		@Override
		public void setIgnoringProperty() {
			value.setText("");
		}

		@Override
		public Float getSelectedValue() {
			return (float) value.getDoubleValue();
		}

		@Override
		public void setSelectedValue(Float value) {
			this.value.setText(GeneralUtils.doubleToString(value));
		}

		@Override
		public void checkValid() throws UIInvalidException {
			if (!isIgnoringProperty()) {
				value.checkValid();
				if (!validator.apply((float) value.getDoubleValue())) {
					throw new UIInvalidException(Translate.GUI_COMMANDEDITOR_NUMBERINVALID);
				}
			}
		}

		@Override
		public IGuiCommandSlot getCommandSlot() {
			return CommandSlotLabel.createLabel(propertyName, value);
		}

	}

	private static class TestBooleanPropertyControl implements IPropertyControl<Boolean> {

		private String propertyName;
		private CommandSlotMenu value;

		public TestBooleanPropertyControl(String propertyName) {
			this.propertyName = propertyName;
			value = new CommandSlotMenu("either", "false", "true");
		}

		@Override
		public boolean isIgnoringProperty() {
			return value.getCurrentIndex() == 0;
		}

		@Override
		public void setIgnoringProperty() {
			value.setCurrentIndex(0);
		}

		@Override
		public Boolean getSelectedValue() {
			return value.getCurrentIndex() == 2;
		}

		@Override
		public void setSelectedValue(Boolean value) {
			this.value.setCurrentIndex(value ? 2 : 1);
		}

		@Override
		public void checkValid() throws UIInvalidException {
		}

		@Override
		public IGuiCommandSlot getCommandSlot() {
			return CommandSlotLabel.createLabel(propertyName, value);
		}

	}

	private static class BooleanPropertyControl implements IPropertyControl<Boolean> {

		private CommandSlotCheckbox value;

		public BooleanPropertyControl(String propertyName) {
			value = new CommandSlotCheckbox(propertyName);
		}

		@Override
		public boolean isIgnoringProperty() {
			return false;
		}

		@Override
		public void setIgnoringProperty() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Boolean getSelectedValue() {
			return value.isChecked();
		}

		@Override
		public void setSelectedValue(Boolean value) {
			this.value.setChecked(value);
		}

		@Override
		public void checkValid() throws UIInvalidException {
		}

		@Override
		public IGuiCommandSlot getCommandSlot() {
			return value;
		}

	}

	private static class PropertyControl<T extends Comparable<T>> implements IPropertyControl<T> {

		private String propertyName;
		private List<T> values;
		private CommandSlotMenu value;
		private boolean isTest;

		public PropertyControl(String propertyName, IProperty<T> prop, boolean isTest) {
			this.propertyName = propertyName;
			List<T> values = Lists.newArrayList(prop.getAllowedValues());
			this.values = values;
			String[] names = new String[values.size()];
			for (int i = 0; i < names.length; i++) {
				names[i] = prop.getName(values.get(i));
			}
			if (isTest) {
				String[] newNames = new String[names.length + 1];
				newNames[0] = "any";
				System.arraycopy(names, 0, newNames, 1, names.length);
				names = newNames;
			}
			value = new CommandSlotMenu(names);
			this.isTest = isTest;
		}

		@Override
		public boolean isIgnoringProperty() {
			return isTest && value.getCurrentIndex() == 0;
		}

		@Override
		public void setIgnoringProperty() {
			value.setCurrentIndex(0);
		}

		@Override
		public T getSelectedValue() {
			int index = value.getCurrentIndex();
			if (isTest) {
				index--;
			}
			return values.get(index);
		}

		@Override
		public void setSelectedValue(T value) {
			int index = values.indexOf(value);
			if (isTest) {
				index++;
			}
			this.value.setCurrentIndex(index);
		}

		@Override
		public void checkValid() throws UIInvalidException {
		}

		@Override
		public IGuiCommandSlot getCommandSlot() {
			return CommandSlotLabel.createLabel(propertyName, value);
		}

	}

}