package cd4017be.lib.gui;

import cd4017be.lib.Lib;
import cd4017be.lib.network.GuiNetworkHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.items.ItemHandlerHelper;

import static cd4017be.lib.gui.comp.IGuiComp.*;
import static cd4017be.lib.text.TooltipUtil.*;

import java.util.ArrayList;

import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.container.slot.IFluidSlot;
import cd4017be.lib.container.slot.SlotHolo;
import cd4017be.lib.gui.comp.GuiCompGroup;

/**
 * ContainerScreen based component manager template.
 * @see GuiCompGroup
 * @author CD4017BE
 */
@OnlyIn(Dist.CLIENT)
public class ModularGui<T extends AdvancedContainer> extends ContainerScreen<T> {
	
	public static final ResourceLocation LIB_TEX = Lib.rl("textures/icons.png");

	/** whether to draw main title (&1) and player inventory title (&2) */
	protected byte drawTitles;
	private Slot lastClickSlot;
	public GuiCompGroup compGroup;

	/**
	 * Creates a new ModularGui instance<br>
	 * Note: the GuiFrame {@link #compGroup} is still null, it must be initialized before {@link #initGui()} is called!
	 * @param container container providing the state from server
	 */
	public ModularGui(T container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);
		if (container.hasPlayerInv()) {
			this.drawTitles |= 2;
			Slot slot = container.getSlot(container.playerInvStart());
			playerInventoryTitleX = slot.xPos;
			playerInventoryTitleY = slot.yPos - 13;
		}
	}

	public ModularGui<T> setComps(GuiCompGroup comps, boolean title) {
		this.compGroup = comps;
		if (title) this.drawTitles |= 1;
		return this;
	}

	@Override
	protected void init() {
		this.xSize = compGroup.w;
		this.ySize = compGroup.h;
		super.init();
		compGroup.init(width, height, 0, font);
		compGroup.position(guiLeft, guiTop);
		//Keyboard.enableRepeatEvents(true);
	}

	@Override
	public void onClose() {
		super.onClose();
		//Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void render(MatrixStack matrixStack, int mx, int my, float partialTicks) {
		renderBackground(matrixStack);
		super.render(matrixStack, mx, my, partialTicks);
		renderHoveredTooltip(matrixStack, mx, my);
	}

	@Override
	protected void renderHoveredTooltip(MatrixStack matrixStack, int x, int y) {
		super.renderHoveredTooltip(matrixStack, x, y);
		if (hoveredSlot instanceof IFluidSlot) {
			IFluidSlot fslot = ((IFluidSlot)hoveredSlot);
			FluidStack stack = fslot.getFluid();
			ArrayList<String> info = new ArrayList<String>();
			info.add(stack != null ? stack.getDisplayName().getString() : translate("cd4017be.tankEmpty"));
			info.add(format("cd4017be.tankAmount", stack != null ? (double)stack.getAmount() / 1000D : 0D, (double)fslot.getCapacity() / 1000D));
			GuiUtils.drawHoveringText(matrixStack, convertText(info), x, y, width, height, -1, font);
		}
		//GlStateManager.color4f(1, 1, 1, 1);
		//GlStateManager.disableDepthTest();
		//GlStateManager.disableAlphaTest();
		//GlStateManager.enableBlend();
		compGroup.drawOverlay(matrixStack, x, y);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
		for (Slot slot : container.inventorySlots)
			if (slot instanceof IFluidSlot) {
				IFluidSlot fslot = ((IFluidSlot)slot);
				FluidStack stack = fslot.getFluid();
				if (stack == null) continue;
				float h = (float)stack.getAmount() / (float)fslot.getCapacity();
				if (Float.isNaN(h) || h > 1F || h < 0F) h = 16F;
				else h *= 16F;
				drawFluid(matrixStack, stack, slot.xPos, slot.yPos + 16 - h, 16, h);
			}
		color(-1);
	}

	protected void drawFluid(MatrixStack matrixStack, FluidStack stack, float x, float y, float w, float h) {
		Matrix4f mat = matrixStack.getLast().getMatrix();
		FluidAttributes attr = stack.getFluid().getAttributes();
		TextureAtlasSprite tex = minecraft.getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
			.apply(attr.getStillTexture(stack));
		float u0 = tex.getMinU(), v0 = tex.getMinV(), u1 = tex.getMaxU(), v1 = tex.getMaxV();
		color(attr.getColor(stack));
		minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(mat, x, y + h, 0F).tex(u0, v1).endVertex();
		bufferbuilder.pos(mat, x + w, y + h, 0F).tex(u1, v1).endVertex();
		bufferbuilder.pos(mat, x + w, y, 0F).tex(u1, v0).endVertex();
		bufferbuilder.pos(mat, x, y, 0F).tex(u0, v0).endVertex();
		tessellator.draw();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(
		MatrixStack matrixStack, float partialTicks, int mx, int my
	) {
		compGroup.drawBackground(matrixStack, mx, my, partialTicks);
		if ((drawTitles & 1) != 0) font.func_243248_b(
				matrixStack, title, guiLeft + titleX, guiTop + titleY, 0x404040
			);
		if ((drawTitles & 2) != 0) font.func_243248_b(
				matrixStack, playerInventory.getDisplayName(),
				guiLeft + playerInventoryTitleX, guiTop + playerInventoryTitleY, 0x404040
			);
	}

	@Override
	public boolean mouseClicked(double x, double y, int b) {
		return compGroup.mouseIn((int)x, (int)y, b, A_DOWN)
			|| super.mouseClicked(x, y, b);
	}

	@Override
	public boolean mouseDragged(double x, double y, int b, double dx, double dy) {
		if (compGroup.mouseIn((int)x, (int)y, b, A_HOLD)) return true;
		Slot slot = this.getSlotUnderMouse();
		ItemStack itemstack = playerInventory.getItemStack();
		if (slot instanceof SlotHolo && slot != lastClickSlot) {
			ItemStack slotstack = slot.getStack();
			if (itemstack.isEmpty() || slotstack.isEmpty() || ItemHandlerHelper.canItemStacksStack(itemstack, slotstack))
				this.handleMouseClick(slot, slot.slotNumber, b, ClickType.PICKUP);
			lastClickSlot = slot;
			return true;
		}
		lastClickSlot = slot;
		return super.mouseDragged(x, y, b, dx, dy);
	}

	@Override
	public boolean mouseReleased(double x, double y, int b) {
		lastClickSlot = null;
		return compGroup.mouseIn((int)x, (int)y, b, A_UP)
			|| super.mouseReleased(x, y, b);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return compGroup.keyIn((char)keyCode, keyCode, A_DOWN) //TODO other key events
			|| super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double delta) {
		return compGroup.mouseIn((int)x, (int)y, (int)(Double.doubleToRawLongBits(delta) >> 63) | 1, A_SCROLL)
			|| super.mouseScrolled(x, y, delta);
	}

	public void drawFormatInfo(MatrixStack stack, int x, int y, String key, Object... args) {
		this.renderWrappedToolTip(stack, convertText(format(key, args)), x, y, font);
	}

	public void drawLocString(MatrixStack stack, int x, int y, int h, int c, String s, Object... args) {
		String[] text = format(s, args).split("\n");
		for (String l : text) {
			this.font.drawString(stack, l, x, y, c);
			y += h;
		}
	}

	public void drawStringCentered(MatrixStack stack, String s, int x, int y, int c) {
		this.font.drawString(stack, s, x - this.font.getStringWidth(s) / 2, y, c);
	}

	/**
	 * draws a block overlay next to the GuiScreen that is useful to visualizes block faces.
	 * @param side face to highlight with an arrow
	 * @param type arrow variant
	 */
	public void drawSideConfig(MatrixStack stack, Direction side, int type) {
		/* TODO reimplement
		GlStateManager.enableDepthTest();
		GlStateManager.disableLighting();
		this.fillGradient(stack, -64, 0, 0, 64, 0xff000000, 0xff000000);
		this.minecraft.textureManager.bindTexture(minecraft.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(-32, 32, 32);
		GlStateManager.scalef(16F, -16F, 16F);
		PlayerEntity player = container.inv.player;
		GlStateManager.rotatef(player.rotationPitch, 1, 0, 0);
		GlStateManager.rotatef(player.rotationYaw + 180, 0, 1, 0);
		GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
		
		GlStateManager.pushMatrix();
		BlockPos pos = container.getPos();
		GlStateManager.translatef(-pos.getX(), -pos.getY(), -pos.getZ());
		BufferBuilder t = Tessellator.getInstance().getBuffer();
		t.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		renderBlock(player.world, pos, t);
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
		
		if (side == null) {
			GlStateManager.popMatrix();
			return;
		}
		this.minecraft.textureManager.bindTexture(LIB_TEX);
		Vec3 p = Vec3.Def(0.5, 0.5, 0.5), a, b;
		switch(side) {
		case DOWN: a = Vec3.Def(0, -1, 0); break;
		case UP: a = Vec3.Def(0, 1, 0); break;
		case NORTH: a = Vec3.Def(0, 0, -1); break;
		case SOUTH: a = Vec3.Def(0, 0, 1); break;
		case WEST: a = Vec3.Def(-1, 0, 0); break;
		default: a = Vec3.Def(1, 0, 0);
		}
		Vector3d look = player.getLookVec();
		b = Vec3.Def(look.x, look.y, look.z).mult(a).norm();
		p = p.add(a.scale(0.5)).add(b.scale(-0.5));
		a = a.scale(1.5);
		final float tx = (float)(144 + 16 * type) / 256F, dtx = 16F / 256F, ty = 24F / 256F, dty = 8F / 256F;
		
		t.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(p.x + b.x, p.y + b.y, p.z + b.z).tex(tx, ty + dty).endVertex();
		t.pos(p.x + a.x + b.x, p.y + a.y + b.y, p.z + a.z + b.z).tex(tx + dtx, ty + dty).endVertex();
		t.pos(p.x + a.x, p.y + a.y, p.z + a.z).tex(tx + dtx, ty).endVertex();
		t.pos(p.x, p.y, p.z).tex(tx, ty).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
		*/
	}

	protected void renderBlock(MatrixStack stack, IWorldReader world, BlockPos pos, BufferBuilder t) {
		/* TODO reimplement
		BlockRendererDispatcher render = minecraft.getBlockRendererDispatcher();
		BlockState state = world.getBlockState(pos);
		if (state.getRenderType() != BlockRenderType.MODEL) state = Blocks.GLASS.getDefaultState();
		state = state.getActualState(world, pos);
		IBakedModel model = render.getModelForState(state);
		state = state.getBlock().getExtendedState(state, world, pos);
		render.getBlockModelRenderer().renderModel(world, model, state, pos, stack, t, false, new Random(), 0, 0, null);
		*/
	}

	public void drawItemStack(ItemStack stack, int x, int y, String altText){
		itemRenderer.zLevel = 200.0F;
		net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = this.font;
		itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, altText);
		itemRenderer.zLevel = 0.0F;
	}

	/**
	 * posts a client side chat message
	 * @param msg message to post
	 */
	public void sendChat(String msg) {
		minecraft.player.sendMessage(new StringTextComponent(msg), null);
	}

	public static void color(int c) {
		GlStateManager.enableBlend();
		GlStateManager.color4f((float)(c >> 16 & 0xff) / 255F, (float)(c >> 8 & 0xff) / 255F, (float)(c & 0xff) / 255F, (float)(c >> 24 & 0xff) / 255F);
	}

	/**
	 * sends a packet to the server that is addressed to this GUI's data provider and contains a single byte of payload.<br>
	 * (convenience method for handling button events)
	 * @param c value to send
	 */
	public void sendCommand(int c) {
		PacketBuffer buff = GuiNetworkHandler.preparePacket(container);
		buff.writeByte(c);
		GuiNetworkHandler.GNH_INSTANCE.sendToServer(buff);
	}

	/**
	 * sends a packet to the server that is addressed to this GUI's data provider and contains the given values as payload.<br>
	 * (convenience method for handling button events)
	 * @param args data to send (supports: byte, short, int, long, float, double, String)
	 */
	public void sendPkt(Object... args) {
		PacketBuffer buff = GuiNetworkHandler.preparePacket(container);
		for (Object arg : args) {
			if (arg instanceof Byte) buff.writeByte((Byte)arg);
			else if (arg instanceof Short) buff.writeShort((Short)arg);
			else if (arg instanceof Integer) buff.writeInt((Integer)arg);
			else if (arg instanceof Long) buff.writeLong((Long)arg);
			else if (arg instanceof Float) buff.writeFloat((Float)arg);
			else if (arg instanceof Double) buff.writeDouble((Double)arg);
			else if (arg instanceof String) buff.writeString((String)arg);
			else throw new IllegalArgumentException();
		}
		GuiNetworkHandler.GNH_INSTANCE.sendToServer(buff);
	}

}
