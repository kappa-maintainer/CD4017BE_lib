package cd4017be.lib.network;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * implemented by TileEntities or Containers to receive data packets send by the server
 * @author CD4017BE
 */
public interface IServerPacketReceiver {

	/**
	 * InteractionHandle a data packet from server
	 * @param pkt contained payload
	 * @throws Exception potential decoding errors
	 */
	@OnlyIn(Dist.CLIENT)
	void InteractionHandleServerPacket(FriendlyByteBuf pkt) throws Exception;

	/**
	 * special version for Items
	 * @author CD4017BE
	 */
	public interface ItemSPR {
		/**
		 * InteractionHandle a data packet from server
		 * @param stack target ItemStack
		 * @param player the player holding the item
		 * @param slot where the item is located in player's inventory
		 * @param pkt packet payload
		 */
		@OnlyIn(Dist.CLIENT)
		void InteractionHandleServerPacket(ItemStack stack, ClientPlayerEntity player, int slot, FriendlyByteBuf pkt) throws Exception;
	}

}
