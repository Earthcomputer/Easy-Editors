package net.earthcomputer.easyeditors.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class AnimatedBlockRenderer {

	private static final int HORIZONTAL_CYCLE_TIME = 8000;
	private static final int VERTICAL_CYCLE_TIME = 4000;

	private IBlockState block;

	public AnimatedBlockRenderer(IBlockState block) {
		this.block = block;
	}

	public void render(int x, int y) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		Block block = this.block.getBlock();
		IBlockAccess world = new DummyBlockAccess(this.block);

		if (block.getRenderType() != -1) {
			if (block.getRenderType() == 3) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, 50);
				GlStateManager.scale(9, 9, 9);
				GlStateManager.translate(0.87, 0.85, 0.8);
				long time = System.currentTimeMillis();
				float xRotation = (float) ((double) (time % HORIZONTAL_CYCLE_TIME) / HORIZONTAL_CYCLE_TIME * 360);
				float yRotation = (float) (30 * Math.sin(((double) (System.currentTimeMillis() % VERTICAL_CYCLE_TIME)
						/ VERTICAL_CYCLE_TIME * 2 * Math.PI))) + 180;
				GlStateManager.rotate(xRotation, 0, 1, 0);
				GlStateManager.rotate(yRotation, 1, 0, 0);
				GlStateManager.disableLighting();
				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldrenderer = tessellator.getWorldRenderer();
				worldrenderer.startDrawingQuads();
				worldrenderer.setVertexFormat(DefaultVertexFormats.BLOCK);
				worldrenderer.setTranslation(-0.5, -0.5, -0.5);
				BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
				IBakedModel ibakedmodel = blockrendererdispatcher.getModelFromBlockState(this.block, world,
						(BlockPos) null);
				blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, this.block,
						BlockPos.ORIGIN, worldrenderer, false);
				worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
				tessellator.draw();
				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
			}
		}
	}

}
