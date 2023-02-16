package cd4017be.lib.util;

import static cd4017be.lib.Lib.CFG_SERVER;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;


/**
 * This should fix problems with unsupported methods (involving network communication attempts) that were overseen by Forge's FakePlayer implementation.
 * @author CD4017BE
 */
public class SaferFakePlayer extends FakePlayer {

	public static final GameProfile DEFAULT_PROFILE
	= new GameProfile(new UUID(0, 0), "FakePlayer");

	public final boolean hasAdvancements;

	/**
	 * @param world
	 * @param name
	 */
	public SaferFakePlayer(ServerLevel world, GameProfile name) {
		super(world, name);
		this.inventory = new FullHotbarInventory(this);
		this.hasAdvancements = CFG_SERVER.fakePlayerAdvancements.get();
//		setNetInteractionHandler(this);
	}

	@Override
	public PlayerAdvancements getAdvancements() {
		return hasAdvancements ? super.getAdvancements() : null;
	}

/* TODO reimplement if needed
	@Override public void displayGui(IInteractionObject guiOwner) {}
	@Override public void displayGUIChest(IInventory chestInventory) {}
	@Override public void displayVillagerTradeGui(IMerchant villager) {}
	@Override public void displayGuiCommandBlock(BlockEntityCommandBlock commandBlock) {}
	@Override public boolean isPotionApplicable(PotionEffect potioneffectIn) {return false;}
	@Override public void openEditSign(BlockEntitySign signTile) {}
	@Override public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {}
	@Override public void openBook(ItemStack stack, InteractionHand InteractionHand) {}
	@Override public void setPositionAndUpdate(double x, double y, double z) {}
	@Override public SleepResult trySleep(BlockPos bedLocation) {return SleepResult.OTHER_PROBLEM;}
	@Override public boolean startRiding(Entity entityIn, boolean force) {return false;}
	@Override public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}
	@Override public String getPlayerIP() {return "";}

	private static void setNetInteractionHandler(SaferFakePlayer player) {
		if (FAKE_NET_InteractionHandLER != null) {
			player.connection = FAKE_NET_InteractionHandLER;
			return;
		}
		FAKE_NET_InteractionHandLER = new NetInteractionHandlerPlayServer(null, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
			@Override public void sendPacket(Packet<?> packetIn) {}
			@Override public void update() {}
			@Override public void disconnect(ITextComponent textComponent) {}
			@Override public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<EnumFlags> relativeSet) {}
			@Override public void onDisconnect(ITextComponent reason) {}
		};
		FAKE_NET_InteractionHandLER.player = null; //player was only provided to prevent NPE in constructor.
	}

	/**dummy net InteractionHandler shared across all FakePlayer instances. Only used to avoid issues with attempts to send network packets to the FakePlayer */
//	private static NetInteractionHandlerPlayServer FAKE_NET_InteractionHandLER;


}
