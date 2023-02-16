package cd4017be.lib.network;

import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

/**
 * implemented by TileEntities, Containers or other game objects to receive data packets send by a players client
 * @author CD4017BE
 */
public interface IPlayerPacketReceiver {

	/**
	 * InteractionHandle a data packet from given player
	 * @param pkt contained payload
	 * @param sender the player who sent this
	 * @throws Exception potential decoding errors
	 */
	public void InteractionHandlePlayerPacket(FriendlyByteBuf pkt, ServerPlayerEntity sender) throws Exception;

	/**
	 * special version for Items
	 * @author CD4017BE
	 */
	public interface ItemPPR {
		/**
		 * InteractionHandle a data packet from given player
		 * @param stack target ItemStack
		 * @param slot where the item is located in player's inventory
		 * @param pkt packet payload
		 * @param sender the player holding the item
		 * @throws Exception potential decoding errors
		 */
		void InteractionHandlePlayerPacket(ItemStack stack, int slot, FriendlyByteBuf pkt, ServerPlayerEntity sender) throws Exception;
	}

}
