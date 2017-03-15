package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.IEntitySelectorCallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiSelectStat extends GuiSelectFromList<StatBase> {

	private static List<StatBase> createStatsList(boolean includeAchievements) {
		List<StatBase> stats = Lists.newArrayListWithExpectedSize(
				StatList.BASIC_STATS.size() + (includeAchievements ? AchievementList.ACHIEVEMENTS.size() : 0));
		stats.addAll(StatList.BASIC_STATS);
		stats.add(new StatBase("stat.craftItem",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_CRAFTITEM)));
		stats.add(new StatBase("stat.mineBlock",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_MINEBLOCK)));
		stats.add(new StatBase("stat.useItem",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_USEITEM)));
		stats.add(new StatBase("stat.breakItem",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_BREAKITEM)));
		stats.add(new StatBase("stat.pickup",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_PICKUP)));
		stats.add(new StatBase("stat.drop",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_DROP)));
		stats.add(new StatBase("stat.killEntity",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_KILLENTITY)));
		stats.add(new StatBase("stat.entityKilledBy",
				new TextComponentTranslation(TranslateKeys.GUI_COMMANDEDITOR_SELECTSTAT_ENTITYKILLEDBY)));
		if (includeAchievements) {
			stats.addAll(AchievementList.ACHIEVEMENTS);
		}
		return stats;
	}

	private Block parameterBlock;
	private Item parameterItem;
	private ResourceLocation parameterEntity;
	private GuiButton selectParameterButton;

	public GuiSelectStat(GuiScreen prevScreen, ICallback<StatBase> callback, boolean includeAchievements) {
		super(prevScreen, callback, createStatsList(includeAchievements), Translate.GUI_COMMANDEDITOR_SELECTSTAT_TITLE);
		setFooterHeight(55);

		if (requiresParameterBlock()) {
			StatBase lookingFor = callback.getCallbackValue();
			for (Block block : ForgeRegistries.BLOCKS) {
				if (lookingFor.equals(StatList.getBlockStats(block))) {
					parameterBlock = block;
					break;
				}
			}
		} else if (requiresParameterItem()) {
			StatBase lookingFor = callback.getCallbackValue();
			String lookingForId = lookingFor.statId;
			if (lookingForId.startsWith("stat.craftItem.")) {
				for (Item item : ForgeRegistries.ITEMS) {
					if (lookingFor.equals(StatList.getCraftStats(item))) {
						parameterItem = item;
						break;
					}
				}
			} else if (lookingForId.startsWith("stat.useItem.")) {
				for (Item item : ForgeRegistries.ITEMS) {
					if (lookingFor.equals(StatList.getObjectUseStats(item))) {
						parameterItem = item;
						break;
					}
				}
			} else if (lookingForId.startsWith("stat.breakItem.")) {
				for (Item item : ForgeRegistries.ITEMS) {
					if (lookingFor.equals(StatList.getObjectBreakStats(item))) {
						parameterItem = item;
						break;
					}
				}
			} else if (lookingForId.startsWith("stat.pickup.")) {
				for (Item item : ForgeRegistries.ITEMS) {
					if (lookingFor.equals(StatList.getObjectsPickedUpStats(item))) {
						parameterItem = item;
						break;
					}
				}
			} else if (lookingForId.startsWith("stat.drop.")) {
				for (Item item : ForgeRegistries.ITEMS) {
					if (lookingFor.equals(StatList.getDroppedObjectStats(item))) {
						parameterItem = item;
						break;
					}
				}
			} else {
				throw new AssertionError();
			}
		} else if (requiresParameterEntity()) {
			String statId = callback.getCallbackValue().statId;
			String entityName;
			if (statId.startsWith("stat.killEntity.")) {
				entityName = statId.substring(16);
			} else {
				entityName = statId.substring(20);
			}
			ResourceLocation entityLocation = null;
			EntityEggInfo eggInfo = null;
			for (Map.Entry<ResourceLocation, EntityEggInfo> candidate : EntityList.ENTITY_EGGS.entrySet()) {
				if (entityName.equals(EntityList.getTranslationName(candidate.getKey()))) {
					entityLocation = candidate.getKey();
					eggInfo = candidate.getValue();
					break;
				}
			}
			if (entityLocation != null) {
				if (statId.startsWith("stat.killEntity.")) {
					if (eggInfo.killEntityStat != null) {
						parameterEntity = entityLocation;
					}
				} else {
					if (eggInfo.entityKilledByStat != null) {
						parameterEntity = entityLocation;
					}
				}
			}
		}
	}

	@Override
	protected List<String> getTooltip(StatBase value) {
		return Collections.emptyList();
	}

	@Override
	protected void drawSlot(int y, StatBase value) {
		String str = value.getStatName().getFormattedText();
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2, y + 2, 0xffffff);
		str = value.statId;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				y + 4 + fontRendererObj.FONT_HEIGHT, 0xc0c0c0);
	}

	@Override
	protected boolean doesSearchTextMatch(String searchText, StatBase value) {
		if (value.getStatName().getUnformattedText().toLowerCase().contains(searchText)) {
			return true;
		}
		if (value.statId.toLowerCase().contains(searchText)) {
			return true;
		}
		return false;
	}

	private boolean requiresParameterBlock() {
		return getSelectedValue().statId.equals("stat.mineBlock");
	}

	private static final Set<String> REQUIRE_PARAM_ITEM = ImmutableSet.of("stat.craftItem", "stat.useItem",
			"stat.breakItem", "stat.pickup", "stat.drop");

	private boolean requiresParameterItem() {
		return REQUIRE_PARAM_ITEM.contains(getSelectedValue().statId);
	}

	private boolean requiresParameterEntity() {
		String statId = getSelectedValue().statId;
		return "stat.killEntity".equals(statId) || "stat.entityKilledBy".equals(statId);
	}

	private boolean requiresParameter() {
		return requiresParameterBlock() || requiresParameterItem() || requiresParameterEntity();
	}

	@Override
	public void initGui() {
		super.initGui();
		selectParameterButton = addButton(new GuiButton(2, width - 30, height - 47, 20, 20, "..."));
		selectParameterButton.visible = requiresParameter();
	}

	@Override
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		super.drawForeground(mouseX, mouseY, partialTicks);
		if (requiresParameter()) {
			selectParameterButton.visible = true;
			String str;
			if (requiresParameterBlock()) {
				if (parameterBlock == null) {
					getDoneButton().enabled = false;
					str = Translate.GUI_COMMANDEDITOR_NOBLOCK;
				} else {
					getDoneButton().enabled = true;
					str = GuiSelectBlock.getDisplayName(parameterBlock.getDefaultState());
				}
			} else if (requiresParameterItem()) {
				if (parameterItem == null) {
					getDoneButton().enabled = false;
					str = Translate.GUI_COMMANDEDITOR_NOITEM;
				} else {
					getDoneButton().enabled = true;
					str = I18n.format(parameterItem.getUnlocalizedName() + ".name");
				}
			} else {
				if (parameterEntity == null) {
					getDoneButton().enabled = false;
					str = Translate.GUI_COMMANDEDITOR_NOENTITY;
				} else {
					getDoneButton().enabled = true;
					str = GuiSelectEntity.getEntityName(parameterEntity);
				}
			}
			fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2, height - 44, 0xffffff);
		} else {
			selectParameterButton.visible = false;
		}
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 2) {
			if (requiresParameterBlock()) {
				mc.displayGuiScreen(new GuiSelectBlock(this, new ICallback<IBlockState>() {
					@Override
					public IBlockState getCallbackValue() {
						return parameterBlock == null ? null : parameterBlock.getDefaultState();
					}

					@Override
					public void setCallbackValue(IBlockState value) {
						parameterBlock = value.getBlock();
					}
				}, false, new Predicate<IBlockState>() {
					@Override
					public boolean apply(IBlockState state) {
						Block block = state.getBlock();
						StatBase mineBlockStat = StatList.getBlockStats(block);
						if (mineBlockStat == null) {
							return false;
						}
						String expectedStatId = "stat.mineBlock."
								+ Item.getItemFromBlock(block).delegate.name().toString().replace(':', '.');
						String actualStatId = mineBlockStat.statId;
						return expectedStatId.equals(actualStatId);
					}
				}));
			} else if (requiresParameterItem()) {
				Predicate<ItemStack> predicate;
				String statId = getSelectedValue().statId;
				if ("stat.pickup".equals(statId) || "stat.drop".equals(statId)) {
					predicate = Predicates.alwaysTrue();
				} else if ("stat.useItem".equals(statId)) {
					predicate = new Predicate<ItemStack>() {
						@Override
						public boolean apply(ItemStack stack) {
							Item item = stack.getItem();
							StatBase useItemStat = StatList.getObjectUseStats(item);
							if (useItemStat == null) {
								return false;
							}
							String expectedStatId = "stat.useItem." + item.delegate.name().toString().replace(':', '.');
							String actualStatId = useItemStat.statId;
							return expectedStatId.equals(actualStatId);
						}
					};
				} else if ("stat.breakItem".equals(statId)) {
					predicate = new Predicate<ItemStack>() {
						@Override
						public boolean apply(ItemStack stack) {
							Item item = stack.getItem();
							StatBase breakItemStat = StatList.getObjectBreakStats(item);
							if (breakItemStat == null) {
								return false;
							}
							String expectedStatId = "stat.breakItem."
									+ item.delegate.name().toString().replace(':', '.');
							String actualStatId = breakItemStat.statId;
							return expectedStatId.equals(actualStatId);
						}
					};
				} else if ("stat.craftItem".equals(statId)) {
					predicate = new Predicate<ItemStack>() {
						@Override
						public boolean apply(ItemStack stack) {
							Item item = stack.getItem();
							StatBase craftStat = StatList.getCraftStats(item);
							if (craftStat == null) {
								return false;
							}
							String expectedStatId = "stat.craftItem."
									+ item.delegate.name().toString().replace(':', '.');
							String actualStatId = craftStat.statId;
							return expectedStatId.equals(actualStatId);
						}
					};
				} else {
					throw new AssertionError();
				}
				mc.displayGuiScreen(new GuiSelectItem(this, new ICallback<ItemStack>() {
					@Override
					public ItemStack getCallbackValue() {
						return parameterItem == null ? ItemStack.EMPTY : new ItemStack(parameterItem);
					}

					@Override
					public void setCallbackValue(ItemStack value) {
						parameterItem = value.getItem();
					}
				}, false, predicate));
			} else if (requiresParameterEntity()) {
				mc.displayGuiScreen(new GuiSelectEntity(this, new IEntitySelectorCallback() {
					@Override
					public ResourceLocation getEntity() {
						return parameterEntity;
					}

					@Override
					public void setEntity(ResourceLocation entityName) {
						parameterEntity = entityName;
					}
				}, false, false, Predicates.and(Predicates.in(EntityList.ENTITY_EGGS.keySet()),
						new Predicate<ResourceLocation>() {
							@Override
							public boolean apply(ResourceLocation input) {
								return EntityList.ENTITY_EGGS.get(input).killEntityStat != null;
							}
						})));
			}
		} else {
			super.actionPerformed(button);
		}
	}

	@Override
	protected StatBase getOverrideCallbackValue(StatBase originalValue) {
		if (requiresParameterBlock()) {
			return StatList.getBlockStats(parameterBlock);
		} else if (requiresParameterItem()) {
			String statId = originalValue.statId;
			if ("stat.pickup".equals(statId)) {
				return StatList.getObjectsPickedUpStats(parameterItem);
			} else if ("stat.drop".equals(statId)) {
				return StatList.getDroppedObjectStats(parameterItem);
			} else if ("stat.useItem".equals(statId)) {
				return StatList.getObjectUseStats(parameterItem);
			} else if ("stat.breakItem".equals(statId)) {
				return StatList.getObjectBreakStats(parameterItem);
			} else if ("stat.craftItem".equals(statId)) {
				return StatList.getCraftStats(parameterItem);
			} else {
				throw new AssertionError();
			}
		} else if (requiresParameterEntity()) {
			EntityEggInfo eggInfo = EntityList.ENTITY_EGGS.get(parameterEntity);
			if ("stat.killEntity".equals(originalValue.statId)) {
				return eggInfo.killEntityStat;
			} else {
				return eggInfo.entityKilledByStat;
			}
		} else {
			return originalValue;
		}
	}

	private static final Set<String> WILDCARDS = ImmutableSet.of("stat.mineBlock", "stat.craftItem", "stat.useItem",
			"stat.breakItem", "stat.pickup", "stat.drop", "stat.killEntity", "stat.entityKilledBy");

	@Override
	protected boolean areEqual(StatBase a, StatBase b) {
		String aid = a.statId, bid = b.statId;
		if (aid.equals(bid)) {
			return true;
		}
		if (WILDCARDS.contains(aid)) {
			if (bid.startsWith(aid + ".")) {
				return true;
			}
		}
		if (WILDCARDS.contains(bid)) {
			if (aid.startsWith(bid + ".")) {
				return true;
			}
		}
		return false;
	}

}
