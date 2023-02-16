package cd4017be.lib.BlockEntity;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;

import java.util.ArrayList;

import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.IServerPacketReceiver;
import cd4017be.lib.network.SyncNetworkInteractionHandler;
import cd4017be.lib.tick.IGate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.BlockEntity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**A BlockEntity with special fast synchronisation to nearby players.
 * @author CD4017BE */
public abstract class SyncBlockEntity extends BaseBlockEntity
implements IServerPacketReceiver, IPlayerPacketReceiver {

	public static double CLIENT_RANGE, SERVER_RANGE;

	private final ArrayList<ServerPlayerEntity> watching = new ArrayList<>();
	protected boolean update;

	public SyncBlockEntity(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		watching.clear();
	}

	/**Initiate a fast sync to near players */
	public void updateDisplay() {
		if (!update && !watching.isEmpty()) {
			update = true;
			GATE_UPDATER.add((IGate)this::sendSync);
		}
		this.saveDirty();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getViewDistance() {
		if (!update) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			if (worldPosition.distSqr(
				player.getX(), player.getY(), player.getZ(), true
			) < CLIENT_RANGE) {
				update = true;
				SyncNetworkInteractionHandler.instance.sendToServer(
					SyncNetworkInteractionHandler.preparePacket(this)
				);
			}
		}
		return super.getViewDistance();
	}

	@Override
	public void InteractionHandlePlayerPacket(FriendlyByteBuf pkt, ServerPlayerEntity sender) {
		if (!sender.isAlive() || watching.contains(sender)) return;
		watching.add(sender);
		SyncNetworkInteractionHandler.instance.sendToPlayer(makeSyncPacket(true), sender);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void InteractionHandleServerPacket(FriendlyByteBuf pkt) throws Exception {
		byte n = pkt.readByte();
		if (n == 0) update = false;
		else readSync(pkt, n);
	}

	private boolean sendSync() {
		update = false;
		for (int i = watching.size() - 1; i >= 0; i--) {
			ServerPlayerEntity player = watching.get(i);
			if (player.isAlive() && worldPosition.distSqr(
				player.getX(), player.getY(), player.getZ(), true
			) < SERVER_RANGE) continue;
			watching.remove(i);
			FriendlyByteBuf pkt = SyncNetworkInteractionHandler.preparePacket(this);
			pkt.writeByte(0);
			SyncNetworkInteractionHandler.instance.sendToPlayer(pkt, player);
		}
		if (!watching.isEmpty())
			SyncNetworkInteractionHandler.instance.sendToPlayers(makeSyncPacket(false), watching);
		return false;
	}

	private FriendlyByteBuf makeSyncPacket(boolean full) {
		FriendlyByteBuf pkt = SyncNetworkInteractionHandler.preparePacket(this);
		int p = pkt.writerIndex();
		pkt.writeByte(0);
		pkt.setByte(p, writeSync(pkt, full));
		return pkt;
	}

	/**@param pkt packet to write
	 * @param init whether this is the initial transmission
	 * @return first byte != 0 */
	protected abstract byte writeSync(FriendlyByteBuf pkt, boolean init);

	/**@param pkt packet to read
	 * @param n first byte != 0 */
	protected abstract void readSync(FriendlyByteBuf pkt, byte n);

}
