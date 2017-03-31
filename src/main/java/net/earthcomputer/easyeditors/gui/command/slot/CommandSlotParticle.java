package net.earthcomputer.easyeditors.gui.command.slot;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.FakeWorld;
import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectParticle;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CommandSlotParticle extends CommandSlotVerticalArrangement {

	private static final Field particleManagerParticleTypesField = ReflectionHelper.findField(ParticleManager.class,
			"field_178932_g", "particleTypes");

	private static final World FAKE_WORLD = new FakeWorld();
	private static final Random RANDOM = new Random();
	private static final int MAX_SIMULATIONS = 100000;

	private ParticleType type;
	private CommandSlotRelativeCoordinate spawnCoord;
	// so-called "dx"
	private CommandSlotNumberTextField paramX;
	// so-called "dy"
	private CommandSlotNumberTextField paramY;
	// so-called "dz"
	private CommandSlotNumberTextField paramZ;
	// so-called "speed"
	private CommandSlotNumberTextField paramSpeed;
	private CommandSlotIntTextField.Optional count;
	private CommandSlotCheckbox.Optional force;
	private CommandSlotPlayerSelector players;
	private CommandSlotModifiable args;

	private CommandSlotLabel graphsTitles;
	private Graph xGraph;
	private Graph yGraph;
	private Graph zGraph;
	private Graph xSpeedGraph;
	private Graph ySpeedGraph;
	private Graph zSpeedGraph;
	private Graph redGraph;
	private Graph greenGraph;
	private Graph blueGraph;

	private ArrayList<ParticleSpawnInfo> simulations;

	public CommandSlotParticle() {
		simulations = Lists.newArrayList();

		type = new ParticleType();
		spawnCoord = new CommandSlotRelativeCoordinate() {
			@Override
			protected void onChanged() {
				simulations.clear();
			}
		};
		class ChangeListeningNumberTextField extends CommandSlotNumberTextField {
			public ChangeListeningNumberTextField(int minWidth, int maxWidth) {
				super(minWidth, maxWidth);
			}

			@Override
			protected void onTextChanged() {
				simulations.clear();
			}
		}
		paramX = new ChangeListeningNumberTextField(50, 100);
		paramY = new ChangeListeningNumberTextField(50, 100);
		paramZ = new ChangeListeningNumberTextField(50, 100);
		paramSpeed = new ChangeListeningNumberTextField(50, 100);

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();
		count = new CommandSlotIntTextField.Optional(50, 100, 0, 0) {
			@Override
			protected void onTextChanged() {
				simulations.clear();
			}
		};
		force = new CommandSlotCheckbox.Optional(Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE,
				Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE_TOOLTIP, false) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				setChecked(args[index].equals("force"));
				return 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				args.add(isChecked() ? "force" : "");
			}
		};
		players = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY);
		args = new CommandSlotModifiable();

		xGraph = new Graph(simulations, 0);
		yGraph = new Graph(simulations, 1);
		zGraph = new Graph(simulations, 2);
		xSpeedGraph = new Graph(simulations, 3);
		ySpeedGraph = new Graph(simulations, 4);
		zSpeedGraph = new Graph(simulations, 5);
		redGraph = new Graph(simulations, 6);
		greenGraph = new Graph(simulations, 7);
		blueGraph = new Graph(simulations, 8);

		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_TYPE,
				Translate.GUI_COMMANDEDITOR_PARTICLE_TYPE_TOOLTIP, type));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_COORD,
				Translate.GUI_COMMANDEDITOR_PARTICLE_COORD_TOOLTIP, spawnCoord));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMX,
				Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMX_TOOLTIP, paramX));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMY,
				Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMY_TOOLTIP, paramY));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMZ,
				Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMZ_TOOLTIP, paramZ));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMSPEED,
				Translate.GUI_COMMANDEDITOR_PARTICLE_PARAMSPEED_TOOLTIP, paramSpeed));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_COUNT,
				Translate.GUI_COMMANDEDITOR_PARTICLE_COUNT_TOOLTIP,
				new CommandSlotOptional.Impl(count, optionalGroup)));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE,
				Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE_TOOLTIP,
				new CommandSlotOptional.Impl(force, optionalGroup)));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_PLAYERS,
				Translate.GUI_COMMANDEDITOR_PARTICLE_PLAYERS_TOOLTIP,
				new CommandSlotRectangle(new CommandSlotOptional(players, optionalGroup) {
					@Override
					protected boolean isDefault() throws UIInvalidException {
						List<String> args = Lists.newArrayList();
						getChild().addArgs(args);
						return args.get(0).equals("@a");
					}

					@Override
					protected void setToDefault() {
						try {
							getChild().readFromArgs(new String[] { "@a" }, 0);
						} catch (CommandSyntaxException e) {
							throw new Error(e);
						}
					}
				}, Colors.playerSelectorBox.color)));
		addChild(args);
		new CommandSlotOptional(args, optionalGroup) {
			@Override
			protected boolean isDefault() throws UIInvalidException {
				return args.getChild() == null;
			}

			@Override
			protected void setToDefault() {
				args.setChild(null);
			}
		};

		graphsTitles = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format(TranslateKeys.GUI_COMMANDEDITOR_PARTICLE_GRAPH, 0));
		addChild(graphsTitles);
		CommandSlotVerticalArrangement posGraphs = new CommandSlotVerticalArrangement();
		posGraphs.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHX, xGraph));
		posGraphs.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHY, yGraph));
		posGraphs.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHZ, zGraph));
		CommandSlotVerticalArrangement speedGraphs = new CommandSlotVerticalArrangement();
		speedGraphs
				.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHXSPEED, xSpeedGraph));
		speedGraphs
				.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHYSPEED, ySpeedGraph));
		speedGraphs
				.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHZSPEED, zSpeedGraph));
		CommandSlotVerticalArrangement colorGraphs = new CommandSlotVerticalArrangement();
		colorGraphs.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHRED, redGraph));
		colorGraphs.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHGREEN, greenGraph));
		colorGraphs.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPHBLUE, blueGraph));
		addChild(new CommandSlotHorizontalArrangement(posGraphs, speedGraphs, colorGraphs));
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		if (simulations.size() < MAX_SIMULATIONS) {
			try {
				checkValid();
				double posX = spawnCoord.getXArg().getTextField().getDoubleValue();
				double posY = spawnCoord.getYArg().getTextField().getDoubleValue();
				double posZ = spawnCoord.getZArg().getTextField().getDoubleValue();
				double paramX = this.paramX.getDoubleValue();
				double paramY = this.paramY.getDoubleValue();
				double paramZ = this.paramZ.getDoubleValue();
				double paramSpeed = this.paramSpeed.getDoubleValue();
				int count = this.count.getIntValue();
				List<String> args = Lists.newArrayList();
				this.args.addArgs(args);
				int[] params = new int[args.size()];
				for (int i = 0; i < params.length; i++) {
					params[i] = Integer.parseInt(args.get(i));
				}

				int particleId = type.getValue().getParticleID();
				IParticleFactory particleFactory = (IParticleFactory) ((Map<?, ?>) particleManagerParticleTypesField
						.get(Minecraft.getMinecraft().effectRenderer)).get(particleId);

				simulations.ensureCapacity(simulations.size() + 100);
				for (int i = 0; i < 100; i++) {
					Particle particle;
					if (count == 0) {
						double xSpeed = paramX * paramSpeed;
						double ySpeed = paramY * paramSpeed;
						double zSpeed = paramZ * paramSpeed;
						particle = particleFactory.createParticle(particleId, FAKE_WORLD, posX, posY, posZ, xSpeed,
								ySpeed, zSpeed, params);
					} else {
						double xOffset = RANDOM.nextGaussian() * paramX;
						double yOffset = RANDOM.nextGaussian() * paramY;
						double zOffset = RANDOM.nextGaussian() * paramZ;
						double xSpeed = RANDOM.nextGaussian() * paramSpeed;
						double ySpeed = RANDOM.nextGaussian() * paramSpeed;
						double zSpeed = RANDOM.nextGaussian() * paramSpeed;
						particle = particleFactory.createParticle(particleId, FAKE_WORLD, posX + xOffset,
								posY + yOffset, posZ + zOffset, xSpeed, ySpeed, zSpeed, params);
					}
					simulations.add(new ParticleSpawnInfo(particle));
				}
				graphsTitles.setText(I18n.format(TranslateKeys.GUI_COMMANDEDITOR_PARTICLE_GRAPH, simulations.size()));
				graphsTitles.setColor(0x000000);
			} catch (UIInvalidException e) {
				// ignore
			} catch (Throwable e) {
				graphsTitles.setText(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPH_CANTSIMULATE);
				graphsTitles.setColor(0xff0000);
			}
		}
		super.draw(x, y, mouseX, mouseY, partialTicks);
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		super.addArgs(args);
	}

	private void checkValid() throws UIInvalidException {
		type.checkValid();
		spawnCoord.getXArg().checkValid();
		spawnCoord.getYArg().checkValid();
		spawnCoord.getZArg().checkValid();
		paramX.checkValid();
		paramY.checkValid();
		paramZ.checkValid();
		paramSpeed.checkValid();
		count.checkValid();
		players.checkValid();
		args.addArgs(Lists.<String>newArrayList());
	}

	private void onChangeParticleTo(EnumParticleTypes newType) {
		switch (newType) {
		case ITEM_CRACK:
			args.setChild(new CommandSlotRectangle(new CommandSlotItemStack(1, CommandSlotItemStack.COMPONENT_ITEM,
					CommandSlotItemStack.COMPONENT_DAMAGE) {
				@Override
				public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
					if (args.length == index) {
						throw new CommandSyntaxException();
					}
					Item item;
					try {
						item = Item.getItemById(Integer.parseInt(args[index]));
					} catch (NumberFormatException e) {
						throw new CommandSyntaxException();
					}
					if (item == null) {
						throw new CommandSyntaxException();
					}
					index++;
					boolean hasMetaArgument = args.length != index;
					int meta;
					if (hasMetaArgument) {
						try {
							meta = Integer.parseInt(args[index]);
						} catch (NumberFormatException e) {
							throw new CommandSyntaxException();
						}
					} else {
						meta = 0;
					}
					setItem(new ItemStack(item, 1, meta));
					return hasMetaArgument ? 2 : 1;
				}

				@Override
				public void addArgs(List<String> args) throws UIInvalidException {
					checkValid();

					ItemStack stack = getItem();
					Item item = stack.getItem();
					int meta = stack.getMetadata();

					args.add(String.valueOf(Item.getIdFromItem(item)));
					if (meta != 0) {
						args.add(String.valueOf(meta));
					}
				}
			}, Colors.itemBox.color));
			break;
		case BLOCK_CRACK:
		case BLOCK_DUST:
		case FALLING_DUST:
			args.setChild(new CommandSlotRectangle(new CommandSlotBlock(false, 1, CommandSlotBlock.COMPONENT_BLOCK,
					CommandSlotBlock.COMPONENT_PROPERTIES) {
				@Override
				public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
					if (args.length == index) {
						throw new CommandSyntaxException();
					}
					int blockStateId;
					try {
						blockStateId = Integer.parseInt(args[index]);
					} catch (NumberFormatException e) {
						throw new CommandSyntaxException();
					}
					setBlock(Block.getStateById(blockStateId));
					return 1;
				}

				@Override
				public void addArgs(List<String> args) throws UIInvalidException {
					checkValid();
					int blockStateId = Block.getStateId(getBlock());
					args.add(String.valueOf(blockStateId));
				}
			}, Colors.itemBox.color));
			break;
		default:
			args.setChild(null);
			break;
		}
	}

	private class ParticleType extends CommandSlotSelectFromList<EnumParticleTypes> {
		public ParticleType() {
			super(Translate.GUI_COMMANDEDITOR_NOPARTICLE, TranslateKeys.GUI_COMMANDEDITOR_NOPARTICLESELECTED);
		}

		@Override
		protected GuiSelectFromList<EnumParticleTypes> createGui(GuiScreen currentScreen,
				ICallback<EnumParticleTypes> callback) {
			return new GuiSelectParticle(currentScreen, callback);
		}

		@Override
		protected String getDisplayName(EnumParticleTypes val) {
			return I18n.format("gui.commandEditor.particle." + val.getParticleName() + ".name");
		}

		@Override
		protected EnumParticleTypes readArg(String arg) {
			return EnumParticleTypes.getByName(arg);
		}

		@Override
		protected String writeArg(EnumParticleTypes arg) {
			return arg.getParticleName();
		}

		@Override
		public void setValue(EnumParticleTypes value) {
			super.setValue(value);
			onChangeParticleTo(value);
		}
	}

	private static class Graph extends GuiCommandSlotImpl {

		private List<ParticleSpawnInfo> simulations;
		private int fieldNumber;

		public Graph(List<ParticleSpawnInfo> simulations, int fieldNumber) {
			super(110, 150);
			this.simulations = simulations;
			this.fieldNumber = fieldNumber;
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			return 0;
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
		}

		@Override
		public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
			// draw the axes
			Gui.drawRect(x, y, x + 1, y + 100, 0xff000000);
			Gui.drawRect(x, y + 99, x + 100, y + 100, 0xff000000);

			// exit if there have been no simulations (the axes are all we need
			// to draw)
			if (simulations.isEmpty()) {
				return;
			}

			// get all the field values from the simulations
			ArrayList<Double> fieldValues = Lists.newArrayListWithCapacity(simulations.size());
			for (ParticleSpawnInfo sim : simulations) {
				fieldValues.add(sim.getField(fieldNumber));
			}

			// get the minimum, maximum and halfway between values
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (double field : fieldValues) {
				if (field < min) {
					min = field;
				}
				if (field > max) {
					max = field;
				}
			}
			double middle = min + (max - min) / 2;

			// draw the numbers on the x-axis
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 100 / 2, y + 101, 0);
			GlStateManager.rotate(90, 0, 0, 1);
			FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
			DecimalFormat format = new DecimalFormat("0.###");
			font.drawString(format.format(max), 0, -(100 / 2 + 4), 0x000000);
			font.drawString(format.format(middle), 0, -4, 0x000000);
			font.drawString(format.format(min), 0, 100 / 2 - 4, 0x000000);
			GlStateManager.popMatrix();

			// divide the numbers into groups and count the numbers in each
			// group so they can be drawn as a frequency polygon
			final int GROUP_COUNT = 20;
			double groupWidth = (max - min) / GROUP_COUNT;
			int[] numberInEachGroup = new int[GROUP_COUNT];
			for (double field : fieldValues) {
				int groupNumber = (int) ((field - min) / groupWidth);
				if (groupNumber == GROUP_COUNT) {
					groupNumber--;
				}
				numberInEachGroup[groupNumber]++;
			}
			int maxNumberInGroup = 0;
			for (int numInGroup : numberInEachGroup) {
				if (numInGroup > maxNumberInGroup) {
					maxNumberInGroup = numInGroup;
				}
			}
			int[] heights = new int[GROUP_COUNT];
			for (int i = 0; i < GROUP_COUNT; i++) {
				heights[i] = numberInEachGroup[i] * 100 / maxNumberInGroup;
			}

			// draw the polygon
			double groupWidthPixels = 100.0 / (GROUP_COUNT + 1);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0; i < GROUP_COUNT; i++) {
				buffer.pos(x + groupWidthPixels / 2 + groupWidthPixels * i, y + 100 - heights[i], 0).color(0, 0, 0, 255)
						.endVertex();
			}
			GlStateManager.disableTexture2D();
			tessellator.draw();
			GlStateManager.enableTexture2D();
		}

	}

	private static class ParticleSpawnInfo {
		private static final Field particlePosX = ReflectionHelper.findField(Particle.class, "field_187126_f", "posX");
		private static final Field particlePosY = ReflectionHelper.findField(Particle.class, "field_187127_g", "posY");
		private static final Field particlePosZ = ReflectionHelper.findField(Particle.class, "field_187128_h", "posZ");
		private static final Field particleMotionX = ReflectionHelper.findField(Particle.class, "field_187129_i",
				"motionX");
		private static final Field particleMotionY = ReflectionHelper.findField(Particle.class, "field_187130_j",
				"motionY");
		private static final Field particleMotionZ = ReflectionHelper.findField(Particle.class, "field_187131_k",
				"motionZ");

		private double x;
		private double y;
		private double z;
		private double xSpeed;
		private double ySpeed;
		private double zSpeed;
		private float red;
		private float green;
		private float blue;

		public ParticleSpawnInfo(Particle particle) {
			try {
				x = particlePosX.getDouble(particle);
				y = particlePosY.getDouble(particle);
				z = particlePosZ.getDouble(particle);
				xSpeed = particleMotionX.getDouble(particle);
				ySpeed = particleMotionY.getDouble(particle);
				zSpeed = particleMotionZ.getDouble(particle);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
			red = particle.getRedColorF();
			green = particle.getGreenColorF();
			blue = particle.getBlueColorF();
		}

		public double getField(int id) {
			switch (id) {
			case 0:
				return x;
			case 1:
				return y;
			case 2:
				return z;
			case 3:
				return xSpeed;
			case 4:
				return ySpeed;
			case 5:
				return zSpeed;
			case 6:
				return red;
			case 7:
				return green;
			case 8:
				return blue;
			default:
				throw new IndexOutOfBoundsException(String.valueOf(id));
			}
		}
	}

}
