package net.earthcomputer.easyeditors.gui.command.slot;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.FakeWorld;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectParticle;
import net.earthcomputer.easyeditors.gui.command.IParticleSelectorCallback;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CommandSlotParticle extends CommandSlotVerticalArrangement {

	private static final Field particleManagerParticleTypesField = ReflectionHelper.findField(ParticleManager.class,
			"field_178932_g", "particleTypes");

	private static final World FAKE_WORLD = new FakeWorld();
	private static final Random RANDOM = new Random();

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
	private CommandSlotIntTextField count;
	private CommandSlotCheckbox force;
	private CommandSlotPlayerSelector players;
	private CommandSlotModifiable<IGuiCommandSlot> args;
	private CommandSlotVerticalArrangement EMPTY_ARGS;

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

		type = new ParticleType(simulations);
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
		count = new CommandSlotIntTextField(50, 100, 0) {
			@Override
			protected void onTextChanged() {
				simulations.clear();
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setText("0");
					return 0;
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				checkValid();
				if (getIntValue() == 0 && !force.isChecked()) {
					List<String> tmpArgs = Lists.newArrayListWithCapacity(1);
					players.addArgs(tmpArgs);
					if (tmpArgs.isEmpty()) {
						return;
					}
				}
				super.addArgs(args);
			}
		};
		force = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE,
				Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE_TOOLTIP) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setChecked(false);
					return 0;
				} else {
					setChecked(args[index].equals("force"));
					return 1;
				}
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (!isChecked()) {
					List<String> tmpArgs = Lists.newArrayListWithCapacity(1);
					players.addArgs(tmpArgs);
					if (tmpArgs.isEmpty()) {
						return;
					}
				}
				args.add(isChecked() ? "force" : "");
			}
		};
		players = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY) {
			{
				try {
					super.readFromArgs(new String[] { "@a" }, 0);
				} catch (CommandSyntaxException e) {
					throw Throwables.propagate(e);
				}
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				super.addArgs(args);
				if (args.get(args.size() - 1).equals("@a") && CommandSlotParticle.this.args.getChild() == EMPTY_ARGS) {
					args.remove(args.size() - 1);
				}
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					super.readFromArgs(new String[] { "@a" }, 0);
					return 0;
				}
				return super.readFromArgs(args, index);
			}
		};
		EMPTY_ARGS = new CommandSlotVerticalArrangement();
		args = new CommandSlotModifiable<IGuiCommandSlot>(EMPTY_ARGS);

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
				Translate.GUI_COMMANDEDITOR_PARTICLE_COUNT_TOOLTIP, count));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE,
				Translate.GUI_COMMANDEDITOR_PARTICLE_FORCE_TOOLTIP, force));
		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_PLAYERS,
				Translate.GUI_COMMANDEDITOR_PARTICLE_PLAYERS_TOOLTIP, players));
		addChild(args);

		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PARTICLE_GRAPH));
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
		if (simulations.size() < 10000) {
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
				int[] params = new int[0]; // TODO: fix

				int particleId = type.getParticle().getParticleID();
				IParticleFactory particleFactory;
				try {
					particleFactory = (IParticleFactory) ((Map<?, ?>) particleManagerParticleTypesField
							.get(Minecraft.getMinecraft().effectRenderer)).get(particleId);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}

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
			} catch (UIInvalidException e) {
				// ignore
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
		// TODO: check args valid
	}

	private static class ParticleType extends CommandSlotHorizontalArrangement implements IParticleSelectorCallback {
		private EnumParticleTypes particleType;
		private CommandSlotLabel particleName;

		private List<ParticleSpawnInfo> simulations;

		public ParticleType(List<ParticleSpawnInfo> simulations) {
			this.simulations = simulations;
			particleName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					Translate.GUI_COMMANDEDITOR_NOPARTICLE, Colors.invalidItemName.color);
			addChild(particleName);
			addChild(new CommandSlotButton(20, 20, "...") {
				@Override
				public void onPress() {
					Minecraft.getMinecraft().displayGuiScreen(
							new GuiSelectParticle(Minecraft.getMinecraft().currentScreen, ParticleType.this));
				}
			});
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (args.length == index) {
				throw new CommandSyntaxException();
			}
			EnumParticleTypes particle = EnumParticleTypes.getByName(args[index]);
			if (particle == null) {
				throw new CommandSyntaxException();
			}
			setParticle(particle);
			return 1;
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
			checkValid();
			args.add(particleType.getParticleName());
		}

		@Override
		public EnumParticleTypes getParticle() {
			return particleType;
		}

		@Override
		public void setParticle(EnumParticleTypes particle) {
			this.particleType = particle;
			particleName.setText(I18n.format("gui.commandEditor.particle." + particle.getParticleName() + ".name"));
			particleName.setColor(Colors.itemName.color);
			simulations.clear();
		}

		public void checkValid() throws UIInvalidException {
			if (particleType == null) {
				throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOPARTICLESELECTED);
			}
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
			Gui.drawRect(x, y, x + 1, y + 100, 0xff000000);
			Gui.drawRect(x, y + 99, x + 100, y + 100, 0xff000000);
			if (simulations.isEmpty()) {
				return;
			}

			ArrayList<Double> fieldValues = Lists.newArrayListWithCapacity(simulations.size());
			for (ParticleSpawnInfo sim : simulations) {
				fieldValues.add(sim.getField(fieldNumber));
			}
			// TODO: not use the median and therefore not have to sort the
			// numbers?
			Collections.sort(fieldValues);
			double min = fieldValues.get(0);
			double max = fieldValues.get(fieldValues.size() - 1);
			double median = fieldValues.get(fieldValues.size() / 2);

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 100 / 2, y + 101, 0);
			GlStateManager.rotate(90, 0, 0, 1);
			FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
			MathContext mthCtx = new MathContext(3);
			font.drawString(BigDecimal.valueOf(max).round(mthCtx).toString(), 0, -(100 / 2 + 4), 0x000000);
			font.drawString(BigDecimal.valueOf(median).round(mthCtx).toString(), 0, -4, 0x000000);
			font.drawString(BigDecimal.valueOf(min).round(mthCtx).toString(), 0, 100 / 2 - 4, 0x000000);
			GlStateManager.popMatrix();

			final int GROUP_SIZE = 20;
			int groupCount = fieldValues.size() / GROUP_SIZE;
			double groupWidth = (max - min) / groupCount;
			int[] numberInEachGroup = new int[groupCount];
			for (double field : fieldValues) {
				int groupNumber = (int) ((field - min) / groupWidth);
				if (groupNumber == groupCount) {
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
			int[] heights = new int[groupCount];
			for (int i = 0; i < groupCount; i++) {
				heights[i] = numberInEachGroup[i] * 100 / maxNumberInGroup;
			}

			double groupWidthPixels = 100.0 / (groupCount + 1);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			for (int i = 0; i < groupCount; i++) {
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