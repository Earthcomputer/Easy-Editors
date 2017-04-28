package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.BlockPropertyRegistry;
import net.earthcomputer.easyeditors.api.util.AnimatedBlockRenderer;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.api.util.NBTToJson;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectBlock;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.HoverChecker;

public class CommandSlotBlock extends CommandSlotVerticalArrangement implements ICallback<IBlockState> {

	public static final int COMPONENT_BLOCK = 1;
	public static final int COMPONENT_PROPERTIES = 2;
	public static final int COMPONENT_NBT = 4;

	private boolean isTest;
	private int[] argOrder;
	private int optionalStart;
	private Predicate<IBlockState> allowedBlockPredicate = Predicates.alwaysTrue();

	private IBlockState state;
	private Map<IProperty<?>, CommandSlotCheckbox> variantsIgnoring;
	private Map<IProperty<?>, IPropertyControl<?>> nonVariants;
	private List<NBTTagHandler> nbtHandlers;

	private CommandSlotModifiable properties;
	private CommandSlotModifiable nbt;

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
				Minecraft.getMinecraft().displayGuiScreen(new GuiSelectBlock(Minecraft.getMinecraft().currentScreen,
						CommandSlotBlock.this, (finalDisplayComponents & (COMPONENT_PROPERTIES | COMPONENT_NBT)) != 0,
						allowedBlockPredicate));
			}
		}));

		if ((displayComponents & COMPONENT_PROPERTIES) != 0) {
			addChild(properties = new CommandSlotModifiable());
		}

		if ((displayComponents & COMPONENT_NBT) != 0) {
			addChild(nbt = new CommandSlotModifiable());
		}
	}

	public CommandSlotBlock setAllowedBlockPredicate(Predicate<IBlockState> allowedBlockPredicate) {
		this.allowedBlockPredicate = allowedBlockPredicate;
		return this;
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
		Map<IProperty<?>, Comparable<?>> properties = isTest ? Maps.<IProperty<?>, Comparable<?>>newHashMap()
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
		if (!allowedBlockPredicate.apply(state)) {
			throw new CommandSyntaxException();
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
				ResourceLocation blockName = state.getBlock().delegate.name();
				potentialArgs.add(GeneralUtils.resourceLocationToString(blockName));
				maxElementToCopy = i;
				break;
			case COMPONENT_PROPERTIES:
				boolean _default;
				if (!isTest) {
					_default = state.equals(state.getBlock().getDefaultState());
					potentialArgs.add(String.valueOf(state.getBlock().getMetaFromState(state)));
				} else {
					Map<IProperty<?>, Comparable<?>> properties = Maps.newHashMap();
					if (isTest) {
						for (Map.Entry<IProperty<?>, CommandSlotCheckbox> variantIgnoring : variantsIgnoring
								.entrySet()) {
							if (!variantIgnoring.getValue().isChecked()) {
								properties.put(variantIgnoring.getKey(), state.getValue(variantIgnoring.getKey()));
							}
						}
					} else {
						properties = Maps.newHashMap(state.getProperties());
					}
					for (Map.Entry<IProperty<?>, IPropertyControl<?>> entry : nonVariants.entrySet()) {
						if (!entry.getValue().isIgnoringProperty()) {
							properties.put(entry.getKey(), entry.getValue().getSelectedValue());
						}
					}
					if (properties.isEmpty()) {
						potentialArgs.add("*");
						_default = true;
					} else if (properties.size() == state.getProperties().size()) {
						IBlockState state = this.state.getBlock().getDefaultState();
						for (Map.Entry<IProperty<?>, Comparable<?>> prop : properties.entrySet()) {
							state = putProperty(state, prop.getKey(), prop.getValue());
						}
						potentialArgs.add(String.valueOf(state.getBlock().getMetaFromState(state)));
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
		if (canSkipOptionals()) {
			potentialArgs = potentialArgs.subList(0, maxElementToCopy + 1);
		}
		args.addAll(potentialArgs);
	}

	public void checkValid() throws UIInvalidException {
		if (state == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOBLOCKSELECTED);
		}
		if (properties != null) {
			for (IPropertyControl<?> property : nonVariants.values()) {
				property.checkValid();
			}
		}
		if (nbtHandlers != null) {
			for (NBTTagHandler handler : nbtHandlers) {
				handler.checkValid();
			}
		}
	}

	protected boolean canSkipOptionals() throws UIInvalidException {
		return true;
	}

	public IBlockState getBlock() {
		return state;
	}

	public void setBlock(IBlockState block) {
		boolean sameBlock;
		if (state == null) {
			sameBlock = block == null;
		} else if (block == null) {
			sameBlock = false;
		} else {
			sameBlock = state.getBlock() == block.getBlock();
		}
		this.state = block;

		if (!sameBlock && this.properties != null) {
			if (block == null) {
				properties.setChild(null);
			} else {
				List<IProperty<?>> properties = Lists.newArrayList(block.getPropertyKeys());
				List<IProperty<?>> variantProperties = BlockPropertyRegistry.getVariantProperties(block.getBlock());
				CommandSlotVerticalArrangement propertiesCommandSlot = new CommandSlotVerticalArrangement();
				if (isTest) {
					variantsIgnoring = Maps.newHashMap();
					CommandSlotVerticalArrangement variantsIgnoringSlot = new CommandSlotVerticalArrangement();
					variantsIgnoringSlot.addChild(CommandSlotLabel
							.createLabel(Translate.GUI_COMMANDEDITOR_BLOCK_VARIANTSIGNORING, Colors.itemLabel.color));
					for (IProperty<?> variantProp : variantProperties) {
						CommandSlotCheckbox checkbox = new CommandSlotCheckbox(
								GuiSelectBlock.getPropertyName(variantProp));
						variantsIgnoringSlot.addChild(checkbox);
						variantsIgnoring.put(variantProp, checkbox);
					}
					if (!variantsIgnoring.isEmpty()) {
						propertiesCommandSlot
								.addChild(new CommandSlotRectangle(variantsIgnoringSlot, Colors.itemBox.color));
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
		}

		if (this.nbt != null) {
			if (block != null && block.getBlock().hasTileEntity(block)) {
				TileEntity te = block.getBlock().createTileEntity(Minecraft.getMinecraft().world, block);
				nbtHandlers = te == null ? null
						: NBTTagHandler.constructTileEntityHandlers(te.getClass(), getContext());
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

	@Override
	public IBlockState getCallbackValue() {
		return getBlock();
	}

	@Override
	public void setCallbackValue(IBlockState value) {
		setBlock(value);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> void setControlSelectedValue(IPropertyControl<T> control,
			Comparable<?> value) {
		control.setSelectedValue((T) value);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> IPropertyControl<T> createPropertyControl(IProperty<T> prop,
			boolean isTest) {
		if (canUseGenericPropertyControl(prop)) {
			return new PropertyControl<T>(prop, isTest);
		} else if (prop instanceof PropertyInteger) {
			int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
			for (int allowedVal : ((PropertyInteger) prop).getAllowedValues()) {
				if (allowedVal < min) {
					min = allowedVal;
				}
				if (allowedVal > max) {
					max = allowedVal;
				}
			}
			return (IPropertyControl<T>) new IntPropertyControl((IProperty<Integer>) prop, min, max, isTest);
		} else if (prop instanceof PropertyBool) {
			if (isTest) {
				return (IPropertyControl<T>) new TestBooleanPropertyControl((IProperty<Boolean>) prop);
			} else {
				return (IPropertyControl<T>) new BooleanPropertyControl((IProperty<Boolean>) prop);
			}
		} else {
			return new PropertyControl<T>(prop, isTest);
		}
	}

	private static <T extends Comparable<T>> boolean canUseGenericPropertyControl(IProperty<T> prop) {
		for (T value : prop.getAllowedValues()) {
			if (BlockPropertyRegistry.getValueUnlocalizedName(prop, value) == null) {
				return false;
			}
		}
		return true;
	}

	public NBTTagCompound getNbt() {
		if (this.nbt == null || this.nbtHandlers == null) {
			return null;
		}
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagHandler.writeToNBT(nbt, nbtHandlers);
		return nbt.hasNoTags() ? null : nbt;
	}

	public void setNbt(NBTTagCompound nbt) {
		if (this.nbt == null || this.nbtHandlers == null) {
			return;
		}
		NBTTagHandler.readFromNBT(nbt, nbtHandlers);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.draw(x, y, mouseX, mouseY, partialTicks);

		if (state != null && properties != null) {
			IBlockState defaultState = state.getBlock().getDefaultState();
			if (isTest) {
				for (Map.Entry<IProperty<?>, CommandSlotCheckbox> variant : variantsIgnoring.entrySet()) {
					if (variant.getValue().isChecked()) {
						state = putProperty(state, variant.getKey(), defaultState.getValue(variant.getKey()));
					}
				}
			}
			for (Map.Entry<IProperty<?>, IPropertyControl<?>> prop : nonVariants.entrySet()) {
				if (!prop.getValue().isIgnoringProperty()) {
					state = putProperty(state, prop.getKey(), prop.getValue().getSelectedValue());
				} else {
					state = putProperty(state, prop.getKey(), defaultState.getValue(prop.getKey()));
				}
			}
		}
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
			if (state != null) {
				new AnimatedBlockRenderer(state).render(x,
						fontRenderer.FONT_HEIGHT > 16 ? y + fontRenderer.FONT_HEIGHT / 2 - 8 : y);
			}

			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			String str = getDisplayText();
			fontRenderer.drawString(str, x + 18,
					fontRenderer.FONT_HEIGHT > 16 ? y : y + 8 - fontRenderer.FONT_HEIGHT / 2,
					state == null ? Colors.invalidItemName.color : Colors.itemName.color);
			setWidth(18 + fontRenderer.getStringWidth(str));

			if (state != null) {
				if (hoverChecker == null) {
					hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
				} else {
					hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());
				}

				if (!getContext().isMouseInBounds(mouseX, mouseY)) {
					hoverChecker.resetHoverTimer();
				} else if (hoverChecker.checkHover(mouseX, mouseY)) {
					drawTooltip(mouseX, mouseY, GuiSelectBlock.getBlockStateTooltip(state));
				}
			}
		}

		private String getDisplayText() {
			if (state == null) {
				return Translate.GUI_COMMANDEDITOR_NOBLOCK;
			} else {
				return GuiSelectBlock.getDisplayName(state);
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

		public IntPropertyControl(IProperty<Integer> property, int min, int max, boolean isTest) {
			this.propertyName = GuiSelectBlock.getPropertyName(property);
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
			return CommandSlotLabel.createLabel(propertyName, Colors.itemLabel.color, value);
		}

	}

	private static class TestBooleanPropertyControl implements IPropertyControl<Boolean> {

		private String propertyName;
		private CommandSlotMenu value;

		public TestBooleanPropertyControl(IProperty<Boolean> property) {
			this.propertyName = GuiSelectBlock.getPropertyName(property);
			value = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_BLOCK_PROPCONTROL_EITHER,
					Translate.PROPERTY_FALSE, Translate.PROPERTY_TRUE }, "either", "false", "true");
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
			return CommandSlotLabel.createLabel(propertyName, Colors.itemLabel.color, value);
		}

	}

	private static class BooleanPropertyControl implements IPropertyControl<Boolean> {

		private CommandSlotCheckbox value;

		public BooleanPropertyControl(IProperty<Boolean> property) {
			value = new CommandSlotCheckbox(GuiSelectBlock.getPropertyName(property));
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
		private CommandSlotMenu menuValue;
		private CommandSlotCycleButton cycleButtonValue;
		private boolean isTest;

		public PropertyControl(IProperty<T> prop, boolean isTest) {
			this.propertyName = GuiSelectBlock.getPropertyName(prop);
			List<T> values = Lists.newArrayList(prop.getAllowedValues());
			this.values = values;
			String[] names = new String[values.size()];
			for (int i = 0; i < names.length; i++) {
				names[i] = GuiSelectBlock.getPropertyValueName(prop, values.get(i));
			}
			if (isTest) {
				String[] newNames = new String[names.length + 1];
				newNames[0] = Translate.GUI_COMMANDEDITOR_BLOCK_PROPCONTROL_ANY;
				System.arraycopy(names, 0, newNames, 1, names.length);
				names = newNames;
			}
			if (values.size() <= 7) {
				menuValue = new CommandSlotMenu(names);
			} else {
				cycleButtonValue = new CommandSlotCycleButton(100, 20, names);
			}
			this.isTest = isTest;
		}

		@Override
		public boolean isIgnoringProperty() {
			if (!isTest) {
				return false;
			}
			if (menuValue == null) {
				return cycleButtonValue.getCurrentIndex() == 0;
			} else {
				return menuValue.getCurrentIndex() == 0;
			}
		}

		@Override
		public void setIgnoringProperty() {
			if (menuValue == null) {
				cycleButtonValue.setCurrentIndex(0);
			} else {
				menuValue.setCurrentIndex(0);
			}
		}

		@Override
		public T getSelectedValue() {
			int index;
			if (menuValue == null) {
				index = cycleButtonValue.getCurrentIndex();
			} else {
				index = menuValue.getCurrentIndex();
			}
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
			if (this.menuValue == null) {
				this.cycleButtonValue.setCurrentIndex(index);
			} else {
				this.menuValue.setCurrentIndex(index);
			}
		}

		@Override
		public void checkValid() throws UIInvalidException {
		}

		@Override
		public IGuiCommandSlot getCommandSlot() {
			return CommandSlotLabel.createLabel(propertyName, Colors.itemLabel.color,
					menuValue == null ? cycleButtonValue : menuValue);
		}

	}

}
