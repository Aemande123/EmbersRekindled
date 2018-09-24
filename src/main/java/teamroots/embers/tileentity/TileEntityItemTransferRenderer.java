package teamroots.embers.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class TileEntityItemTransferRenderer extends TileEntitySpecialRenderer<TileEntityItemTransfer> {
	RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
	Random random = new Random();
	public TileEntityItemTransferRenderer(){
		super();
	}
	
	@Override
	public void render(TileEntityItemTransfer tile, double x, double y, double z, float partialTicks, int destroyStage, float tileAlpha){
		if (tile != null){
			if (!tile.filterItem.isEmpty() && Minecraft.getMinecraft().world != null) {
				GlStateManager.pushAttrib();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				GL11.glPushMatrix();
				//EntityItem item = new EntityItem(Minecraft.getMinecraft().world, x, y, z, new ItemStack(tile.filterItem.getItem(), 1, tile.filterItem.getMetadata()));
				//item.hoverStart = 0;
				//item.onGround = false;
				GL11.glTranslated(x + 0.5, y + 0.15+0.25, z + 0.5);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glRotated(tile.angle + ((tile.turnRate)) * partialTicks, 0, 1.0, 0);
				Minecraft.getMinecraft().getRenderItem().renderItem(tile.filterItem, ItemCameraTransforms.TransformType.GROUND);
				GL11.glPopMatrix();
				GlStateManager.popAttrib();
			}
		}
	}
}
