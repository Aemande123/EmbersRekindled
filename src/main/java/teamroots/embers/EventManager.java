package teamroots.embers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.block.IDial;
import teamroots.embers.item.IEmberChargedTool;
import teamroots.embers.item.ItemAshenCloak;
import teamroots.embers.item.ItemEmberGauge;
import teamroots.embers.item.ItemGolemsEye;
import teamroots.embers.item.ItemGrandhammer;
import teamroots.embers.network.PacketHandler;
import teamroots.embers.network.message.MessageEmberBurstFX;
import teamroots.embers.network.message.MessageEmberDataRequest;
import teamroots.embers.network.message.MessageEmberGeneration;
import teamroots.embers.proxy.ClientProxy;
import teamroots.embers.research.ResearchBase;
import teamroots.embers.research.ResearchManager;
import teamroots.embers.tileentity.ITileEntitySpecialRendererLater;
import teamroots.embers.tileentity.TileEntityKnowledgeTable;
import teamroots.embers.util.BlockTextureUtil;
import teamroots.embers.util.FluidTextureUtil;
import teamroots.embers.util.Misc;
import teamroots.embers.util.RenderUtil;
import teamroots.embers.world.EmberWorldData;

public class EventManager {
	double gaugeAngle = 0;
	public static boolean hasRenderedParticles = false;
	Random random = new Random();
	public static float emberEyeView = 0;
	public static ResearchBase lastResearch = null;
	public static float frameTime = 0;
	public static float frameCounter = 0;
	public static long prevTime = 0;
	public static EnumHand lastHand = EnumHand.MAIN_HAND;
	public static float starlightRed = 255;
	public static float starlightGreen = 32;
	public static float starlightBlue = 255;
	public static float tickCounter = 0;
	public static double currentEmber = 0;
	
	static EntityPlayer clientPlayer = null;
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event){
		FluidTextureUtil.initTextures(event.getMap());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent event){
		ResourceLocation particleGlow = new ResourceLocation("embers:entity/particle_mote");
		event.getMap().registerSprite(particleGlow);
		ResourceLocation particleSparkle = new ResourceLocation("embers:entity/particle_star");
		event.getMap().registerSprite(particleSparkle);
		ResourceLocation particleSmoke = new ResourceLocation("embers:entity/particle_smoke");
		event.getMap().registerSprite(particleSmoke);
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event){
		EmberWorldData.get(event.getWorld());
	}
	
	@SubscribeEvent
	public void onLivingDamage(LivingHurtEvent event){
		if (event.getEntity() instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)event.getEntity();
			String source = event.getSource().getDamageType();
			if (source.compareTo("mob") != 0 && source.compareTo("generic") != 0 && source.compareTo("player") != 0 && source.compareTo("arrow") != 0){
				if (player.getHeldItemMainhand() != ItemStack.EMPTY){
					if (player.getHeldItemMainhand().getItem() == RegistryManager.inflictor_gem && player.getHeldItemMainhand().hasTagCompound()){
						player.getHeldItemMainhand().setItemDamage(1);
						player.getHeldItemMainhand().getTagCompound().setString("type", event.getSource().getDamageType());
					}
				}
				if (player.getHeldItemOffhand() != ItemStack.EMPTY){
					if (player.getHeldItemOffhand().getItem() == RegistryManager.inflictor_gem && player.getHeldItemOffhand().hasTagCompound()){
						player.getHeldItemOffhand().setItemDamage(1);
						player.getHeldItemOffhand().getTagCompound().setString("type", event.getSource().getDamageType());
					}
				}
			}
		}
		if (event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.HEAD) != ItemStack.EMPTY &&
				event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST) != ItemStack.EMPTY &&
				event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS) != ItemStack.EMPTY &&
				event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET) != ItemStack.EMPTY){
			if (event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemAshenCloak &&
					event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemAshenCloak &&
					event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof ItemAshenCloak &&
					event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemAshenCloak){
				float mult = Math.max(0,1.0f-ItemAshenCloak.getDamageMultiplier(event.getSource(), event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST)));
				if (mult == 0){
					event.setCanceled(true);
				}
				event.setAmount(event.getAmount()*mult);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGameOverlayRender(RenderGameOverlayEvent.Post e){
		if (e.getType() == ElementType.TEXT){
			EventManager.frameCounter ++;
			EventManager.frameTime = (System.nanoTime()-prevTime)/1000000000.0f;
			EventManager.prevTime = System.nanoTime();
		}
		EntityPlayer player = Minecraft.getMinecraft().player;
		boolean showBar = false;

		int w = e.getResolution().getScaledWidth();
		int h = e.getResolution().getScaledHeight();
		
		int x = w/2;
		int y = h/2;
		if (player.getHeldItemMainhand() != ItemStack.EMPTY){
			if (player.getHeldItemMainhand().getItem() instanceof ItemEmberGauge){
				showBar = true;
			}
		}
		if (player.getHeldItemOffhand() != ItemStack.EMPTY){
			if (player.getHeldItemOffhand().getItem() instanceof ItemEmberGauge){
				showBar = true;
			}
		}
		
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer b = tess.getBuffer();
		if (showBar){
			World world = player.getEntityWorld();
			if (e.getType() == ElementType.TEXT){
				GlStateManager.disableDepth();
				GlStateManager.disableCull();
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("embers:textures/gui/ember_meter_overlay.png"));
				GlStateManager.color(1f, 1f, 1f, 1f);
				
				int offsetX = 0;
				
				b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				RenderUtil.drawQuadGui(b, x-16, y-4, x+16, y-4, x+16, y-36, x-16, y-36, 0, 0, 1, 1);
				tess.draw();
				
				double angle = 195.0;
				EmberWorldData data = EmberWorldData.get(world);
				if (player != null){
					//if (data.emberData != null){
						//if (data.emberData.containsKey(""+((int)player.posX) / 16 + " " + ((int)player.posZ) / 16)){
							double ratio = data.getEmberForChunk((int)Math.floor((player).getPosition().getX()/16.0f), (int)Math.floor((player).getPosition().getZ()/16.0f))/822000.0;
							if (gaugeAngle == 0){
								gaugeAngle = 165.0+210.0*ratio;
							}
							else {
								gaugeAngle = gaugeAngle*0.99+0.01*(165.0+210.0*ratio);
							}
						//}
					//}
				}
				
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("embers:textures/gui/ember_meter_pointer.png"));
				GlStateManager.translate(x, y-20, 0);
				GlStateManager.rotate((float)gaugeAngle, 0, 0, 1);
				b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				RenderUtil.drawQuadGui(b, -2.5f, 13.5f, 13.5f, 13.5f, 13.5f, -2.5f, -2.5f, -2.5f, 0, 0, 1, 1);
				tess.draw();
				
				GlStateManager.popMatrix();
				GlStateManager.enableCull();
				GlStateManager.enableDepth();
			}
		}
		World world = player.getEntityWorld();
		RayTraceResult result = player.rayTrace(6.0, e.getPartialTicks());
		
		boolean showEye = false;
		boolean showingResearch = false;
		if (player.getHeldItemOffhand() != ItemStack.EMPTY){
			if (player.getHeldItemOffhand().getItem() instanceof ItemGolemsEye){
				showEye = true;
				lastHand = EnumHand.OFF_HAND;
			}
		}
		if (player.getHeldItemMainhand() != ItemStack.EMPTY){
			if (player.getHeldItemMainhand().getItem() instanceof ItemGolemsEye){
				showEye = true;
				lastHand = EnumHand.MAIN_HAND;
			}
		}
		if (showEye){
			if (result != null){
				ItemStack test = ItemStack.EMPTY;
				if (result.typeOfHit == RayTraceResult.Type.BLOCK){
					if (world.getTileEntity(result.getBlockPos()) instanceof TileEntityKnowledgeTable){
						TileEntityKnowledgeTable table = ((TileEntityKnowledgeTable)world.getTileEntity(result.getBlockPos()));
						if (table.inventory.getStackInSlot(0) != ItemStack.EMPTY){
							test = table.inventory.getStackInSlot(0);
						}
					}
					if (test == ItemStack.EMPTY){
						test = new ItemStack(world.getBlockState(result.getBlockPos()).getBlock(),1,world.getBlockState(result.getBlockPos()).getBlock().getMetaFromState(world.getBlockState(result.getBlockPos())));
					}
				}
				if (test != ItemStack.EMPTY){
					if (test.getItem() != null){
						ResearchBase research = ResearchManager.researches.get(test.getItem().getRegistryName().toString());
						if (research != null){
							EventManager.lastResearch = research;
							showingResearch = true;
							if (EventManager.emberEyeView < 1.0f){
								EventManager.emberEyeView += 0.2f*frameTime;
							}
							if (EventManager.emberEyeView > 1.0f){
								EventManager.emberEyeView = 1.0f;
							}
						}
					}
				}
			}
		}
		if (!showingResearch && EventManager.emberEyeView > 0){
			EventManager.emberEyeView -= 0.2f*frameTime;
		}
		if (EventManager.emberEyeView < 0){
			EventManager.emberEyeView = 0;
		}
		if (EventManager.emberEyeView > 0 && EventManager.lastResearch != null && e.getType() == ElementType.TEXT){
			if (lastHand == EnumHand.MAIN_HAND){
				x = w-85;
			}
			else {
				x = 85;
			}
			int func = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
			float ref = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
			GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0);
			GlStateManager.disableDepth();
			GlStateManager.disableCull();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.pushMatrix();
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("embers:textures/gui/gradient.png"));
			GlStateManager.color(1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView, 0.8f*EventManager.emberEyeView);
			b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			for (int i = 0; i < 36; i ++){
				float xCur = (float)(x+150.0f*Math.cos(Math.toRadians(i*10)));
				float yCur = (float)(y+150.0f*Math.sin(Math.toRadians(i*10)));
				float xNext = (float)(x+150.0f*Math.cos(Math.toRadians(i*10+10)));
				float yNext = (float)(y+150.0f*Math.sin(Math.toRadians(i*10+10)));
				RenderUtil.drawQuadGuiExt(b, x, y, x, y, xCur, yCur, xNext, yNext, 0, 0, 256, 256, 256, 256);
			}
			tess.draw();
			Random rand = new Random();
			rand.setSeed(Embers.MODID.hashCode());
			GlStateManager.color(1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView, 0.3f*EventManager.emberEyeView);
			b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			for (int j = 0; j < 80; j ++){
				float shiftAngle = rand.nextFloat()*360.0f;
				float rate = rand.nextFloat()+0.5f;
				int reversed = (int)Math.signum(rand.nextFloat()-0.5f);
				float radius = rand.nextFloat()*100.0f;
				float bubbleScale = 0.4f+2.0f*((100.0f-radius)/100.0f);
				float baseX = x+radius*(float)Math.cos(Math.toRadians(reversed*rate*(EventManager.frameCounter+shiftAngle)));
				float baseY = y+radius*(float)Math.sin(Math.toRadians(reversed*rate*(EventManager.frameCounter+shiftAngle)));
				for (int i = 0; i < 10; i ++){
					float xCur = (float)(baseX+bubbleScale*20.0f*Math.cos(Math.toRadians(i*36)));
					float yCur = (float)(baseY+bubbleScale*20.0f*Math.sin(Math.toRadians(i*36)));
					float xNext = (float)(baseX+bubbleScale*20.0f*Math.cos(Math.toRadians(i*36+36)));
					float yNext = (float)(baseY+bubbleScale*20.0f*Math.sin(Math.toRadians(i*36+36)));
					RenderUtil.drawQuadGuiExt(b, baseX, baseY, baseX, baseY, xCur, yCur, xNext, yNext, 0, 0, 256, 128, 256, 256);
				}
			}
			tess.draw();
			GlStateManager.color(1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView, 1f*EventManager.emberEyeView);

			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("embers:textures/gui/eye_gui_overlay.png"));
			b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			RenderUtil.drawQuadGuiExt(b, x-16, y-48, x+16, y-48, x+16, y-80, x-16, y-80, 0, 0, 32, 32, 128, 128);
			tess.draw();

			GlStateManager.enableDepth();
			GlStateManager.disableLighting();
			RenderHelper.disableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(lastResearch.icon, x-8, y-72);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			
			ArrayList<String> text = lastResearch.getLines();
			FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
			int yOff = -44;
			font.drawStringWithShadow(lastResearch.getTitle(), (x-font.getStringWidth(lastResearch.getTitle())/2), (y+yOff), ((int)(255*EventManager.emberEyeView) << 24) | 0xFF330F);
			yOff += (1 + font.FONT_HEIGHT);
			GlStateManager.scale(0.5, 0.5, 0);
			if ((int)(255.0f*EventManager.emberEyeView) > 0){
				for (int i = 0; i < text.size(); i ++){
					font.drawStringWithShadow(text.get(i), 2.0f*(x-font.getStringWidth(text.get(i))/4), 2.0f*(y+yOff+i*(font.FONT_HEIGHT/2+1)), ((int)(255*EventManager.emberEyeView) << 24) | 0xFF330F);
				}
			}
			GlStateManager.scale(2.0, 2.0, 0);
			GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.popMatrix();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(func, ref);
			x = w/2;
		}
		
		if (result != null){
			if (result.typeOfHit == RayTraceResult.Type.BLOCK){
				IBlockState state = world.getBlockState(result.getBlockPos());
				if (state.getBlock() instanceof IDial){
					List<String> text = ((IDial)state.getBlock()).getDisplayInfo(world, result.getBlockPos(), state);
					for (int i = 0; i < text.size(); i ++){
						Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text.get(i), x-Minecraft.getMinecraft().fontRendererObj.getStringWidth(text.get(i))/2, y+40+11*i, 0xFFFFFF);
					}
				}
			}
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("minecraft:textures/gui/icons.png"));
		GlStateManager.enableDepth();
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTick(WorldTickEvent event){
		if (Minecraft.getMinecraft().player != null){
			EmberWorldData.get(event.world).ticks ++;
			PacketHandler.INSTANCE.sendToServer(new MessageEmberDataRequest((Minecraft.getMinecraft().player).getUniqueID(), (int)Math.floor((Minecraft.getMinecraft().player).getPosition().getX()/16.0f), (int)Math.floor((Minecraft.getMinecraft().player).getPosition().getZ()/16.0f)));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTick(TickEvent.ClientTickEvent event){
		if (event.side == Side.CLIENT){
			ClientProxy.particleRenderer.updateParticles();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEntityDamaged(LivingHurtEvent event){
		if (event.getSource().damageType == RegistryManager.damage_ember.damageType){
			if (event.getEntityLiving().isPotionActive(Potion.getPotionFromResourceLocation("fire_resistance"))){
				event.setAmount(event.getAmount()*0.5f);
			}
		}
		if (event.getSource().getEntity() != null){
			if (event.getSource().getEntity() instanceof EntityPlayer){
				if (((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand() != ItemStack.EMPTY){
					if (((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getItem() instanceof IEmberChargedTool){
						if (((IEmberChargedTool)((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getItem()).hasEmber(((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand()) || ((EntityPlayer)event.getSource().getEntity()).capabilities.isCreativeMode){
							event.getEntityLiving().setFire(1);
							if (!event.getEntityLiving().getEntityWorld().isRemote){
								PacketHandler.INSTANCE.sendToAll(new MessageEmberBurstFX(event.getEntityLiving().posX,event.getEntityLiving().posY+event.getEntityLiving().getEyeHeight()/1.5,event.getEntityLiving().posZ));
								((EntityPlayer)event.getSource().getEntity()).getHeldItemMainhand().getTagCompound().setBoolean("didUse", true);
							}
						}
						else {
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event){
		if (event.getPlayer() != null){
			if (event.getPlayer().getHeldItemMainhand() != ItemStack.EMPTY){
				/*if (event.getPlayer().getHeldItemMainhand().getItem() instanceof IEmberChargedTool){
					PacketHandler.INSTANCE.sendToAll(new MessageEmberBurstFX(event.getPos().getX()+0.5,event.getPos().getY()+0.5,event.getPos().getZ()+0.5));
				}*/
				if (event.getPlayer().getHeldItemMainhand().getItem() instanceof ItemGrandhammer){
					event.setCanceled(true);
					event.getWorld().setBlockToAir(event.getPos());
				}
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderAfterWorld(RenderWorldLastEvent event){
		tickCounter ++;
		this.starlightBlue = 96.0f + 80.0f*(float)(Math.sin(Math.toRadians(tickCounter % 360))+1.0f);
		this.starlightRed = 255.0f - 80.0f*(float)(Math.sin(Math.toRadians(tickCounter % 360))+1.0f);
		if (Embers.proxy instanceof ClientProxy){
			ClientProxy.particleRenderer.renderParticles(clientPlayer, event.getPartialTicks());
		}
		List<TileEntity> list = Minecraft.getMinecraft().world.loadedTileEntityList;
		for (int i = 0; i < list.size(); i ++){
			TileEntitySpecialRenderer render = TileEntityRendererDispatcher.instance.getSpecialRenderer(list.get(i));
			if (render instanceof ITileEntitySpecialRendererLater){
				double x = Minecraft.getMinecraft().player.lastTickPosX + Minecraft.getMinecraft().getRenderPartialTicks()*(Minecraft.getMinecraft().player.posX-Minecraft.getMinecraft().player.lastTickPosX);
				double y = Minecraft.getMinecraft().player.lastTickPosY + Minecraft.getMinecraft().getRenderPartialTicks()*(Minecraft.getMinecraft().player.posY-Minecraft.getMinecraft().player.lastTickPosY);
				double z = Minecraft.getMinecraft().player.lastTickPosZ + Minecraft.getMinecraft().getRenderPartialTicks()*(Minecraft.getMinecraft().player.posZ-Minecraft.getMinecraft().player.lastTickPosZ);
				((ITileEntitySpecialRendererLater)render).renderLater(list.get(i), list.get(i).getPos().getX()-x, list.get(i).getPos().getY()-y, list.get(i).getPos().getZ()-z, Minecraft.getMinecraft().getRenderPartialTicks());
			}
		}
	}
}
