package cd4017be.lib.network;

import java.util.ArrayDeque;

import cd4017be.lib.Lib;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Provides GUIs and InteractionHandles their network communication.<dl>
 * To open GUIs with this, simply let your Blocks, Entities and Items implement the corresponding {@link IGuiInteractionHandlerBlock}, {@link IGuiInteractionHandlerEntity} or {@link IGuiInteractionHandlerItem}.
 * And for network communication let your Containers implement {@link IServerPacketReceiver} and {@link IPlayerPacketReceiver}.
 * @author CD4017BE
 */
public class GuiNetworkInteractionHandler extends NetworkInteractionHandler {

	/**the instance */
	public static GuiNetworkInteractionHandler GNH_INSTANCE;

	private static final int MAX_QUEUED = 16;
	private ArrayDeque<FriendlyByteBuf> packetQueue = new ArrayDeque<FriendlyByteBuf>(MAX_QUEUED);

	public static void register() {
		if (GNH_INSTANCE == null) GNH_INSTANCE = new GuiNetworkInteractionHandler(Lib.rl("ui"));
	}

	private GuiNetworkInteractionHandler(ResourceLocation channel) {
		super(channel);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void InteractionHandleServerPacket(FriendlyByteBuf pkt) throws Exception {
		Container container = Minecraft.getInstance().player.containerMenu;
		int curId = container.containerId;
		int id = pkt.markReaderIndex().readInt();
		if ((curId == id || curId == 0) && container instanceof IServerPacketReceiver) {
			((IServerPacketReceiver)container).InteractionHandleServerPacket(pkt);
			if (pkt.readableBytes() > 0) {
				StringBuilder sb = new StringBuilder("Packet > GUI: ");
				printPacketData(sb, pkt);
				Lib.LOG.info(NETWORK, sb.toString());
			}
		} else if (id > curId) {//packet received too early, schedule it for later processing when GUI is opened
			if (packetQueue.size() >= MAX_QUEUED) {
				packetQueue.remove();
				Lib.LOG.warn(NETWORK, "GUI packet queue overflow!");
			}
			pkt.resetReaderIndex();
			packetQueue.add(pkt);
		} else Lib.LOG.warn(NETWORK, "received packet for invalid GUI {} @CLIENT, expected id {} ({})", id, container.containerId, container.getClass());
	}

	@Override
	public void InteractionHandlePlayerPacket(FriendlyByteBuf pkt, ServerPlayerEntity sender) throws Exception {
		int id = pkt.readInt();
		Container container = sender.containerMenu;
		if (container.containerId == id && container instanceof IPlayerPacketReceiver) {
			((IPlayerPacketReceiver)container).InteractionHandlePlayerPacket(pkt, sender);
			if (pkt.readableBytes() > 0) {
				StringBuilder sb = new StringBuilder("Packet > SERVER: ");
				printPacketData(sb, pkt);
				Lib.LOG.info(NETWORK, sb.toString());
			}
		} else Lib.LOG.warn(NETWORK, "received packet for invalid GUI {} @SERVER, expected id {} ({})", id, container.containerId, container.getClass());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onGuiOpened(InitGuiEvent.Post event) {
		if (event.getGui() instanceof ContainerScreen)
			for (int i = packetQueue.size(); i > 0; i--) {
				FriendlyByteBuf buf = packetQueue.remove();
				try {InteractionHandleServerPacket(buf);}
				catch (Exception e) {logError(buf, "QUEUED", e);}
			}
	}

	/**
	 * @param container the container involved in GUI communication
	 * @return a new FriendlyByteBuf with prepared header
	 */
	public static FriendlyByteBuf preparePacket(Container container) {
		FriendlyByteBuf pkt = new FriendlyByteBuf(Unpooled.buffer());
		pkt.writeInt(container.containerId);
		return pkt;
	}

	@Override
	protected String version() {
		return "0";
	}

}
