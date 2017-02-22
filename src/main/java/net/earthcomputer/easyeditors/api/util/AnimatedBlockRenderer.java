package net.earthcomputer.easyeditors.api.util;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * A helper class which renders blocks. Client code should create an instance of
 * this class for each IBlockState they wish to render, then call the
 * {@link #render(int, int)} method.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class AnimatedBlockRenderer {

	private static final int HORIZONTAL_CYCLE_TIME = 8000;
	private static final int VERTICAL_CYCLE_TIME = 4000;

	private IBlockState block;

	/**
	 * Constructs an AnimatedBlockRenderer for the given IBlockState
	 * 
	 * @param block
	 */
	public AnimatedBlockRenderer(IBlockState block) {
		this.block = block;
	}

	/**
	 * Renders this IBlockState at the given coordinates on the screen
	 * 
	 * @param x
	 * @param y
	 */
	@SuppressWarnings("deprecation")
	public void render(int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Block block = this.block.getBlock();
		IBlockAccess world;

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();

		switch (block.getRenderType(this.block)) {
		case LIQUID:
			String fluid = block.getMaterial(this.block) == Material.LAVA ? "lava" : "water";
			String state;
			if (block == BlockLiquid.getStaticBlock(block.getMaterial(this.block)))
				state = "still";
			else if (block == BlockLiquid.getFlowingBlock(block.getMaterial(this.block)))
				state = "flowing";
			else {
				EasyEditorsApi.logger.error("Invalid block with render type 1: " + block.delegate.name());
				break;
			}
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
					.getAtlasSprite(String.format("minecraft:blocks/%s_%s", fluid, state));

			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			worldRenderer.pos(x, y + 16, 50).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
			worldRenderer.pos(x + 16, y + 16, 50).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
			worldRenderer.pos(x + 16, y, 50).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
			worldRenderer.pos(x, y, 50).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
			tessellator.draw();
			break;
		case ENTITYBLOCK_ANIMATED:
			Item item = Item.getItemFromBlock(block);
			if (item != null) {
				ItemStack stack = new ItemStack(item);
				RenderHelper.disableStandardItemLighting();
				RenderHelper.enableGUIStandardItemLighting();
				RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
				renderItem.zLevel = 50;
				renderItem.renderItemAndEffectIntoGUI(stack, x, y);
				renderItem.zLevel = 0;
				RenderHelper.enableStandardItemLighting();
				GlStateManager.color(0, 0, 0, 0);
			}
			break;
		case MODEL:
			GlStateManager.enableDepth();
			GlStateManager.enableCull();
			world = new DummyBlockAccess(this.block);
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 50);
			GlStateManager.scale(9, 9, 9);
			GlStateManager.translate(0.87, 0.85, 0.8);
			long time = System.currentTimeMillis();
			float xRotation = (float) ((double) (time % HORIZONTAL_CYCLE_TIME) / HORIZONTAL_CYCLE_TIME * 360);
			float yRotation = (float) (30 * Math.sin(
					((double) (System.currentTimeMillis() % VERTICAL_CYCLE_TIME) / VERTICAL_CYCLE_TIME * 2 * Math.PI)))
					+ 180;
			GlStateManager.rotate(xRotation, 0, 1, 0);
			GlStateManager.rotate(yRotation, 1, 0, 0);
			GlStateManager.disableLighting();
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			worldRenderer.setTranslation(-0.5, -0.5, -0.5);
			BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
			IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(this.block);
			blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, this.block, BlockPos.ORIGIN,
					worldRenderer, false);
			worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
			GlStateManager.disableDepth();
			break;
		case INVISIBLE:
			break;
		}
	}

}
