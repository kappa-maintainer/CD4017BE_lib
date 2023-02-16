package cd4017be.lib.util;

import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Special version of PlayerInventory where all 36 main inventory slots can be selected as currently held item in main InteractionHand.
 * Used for FakePlayers.
 * @author CD4017BE */
public class FullHotbarInventory extends Inventory {

	public FullHotbarInventory(Player playerIn) {
		super(playerIn);
	}

	@Override
	public ItemStack getSelected() {
		return selected >= 0 && selected < items.size()
			? items.get(selected) : ItemStack.EMPTY;
	}

}
