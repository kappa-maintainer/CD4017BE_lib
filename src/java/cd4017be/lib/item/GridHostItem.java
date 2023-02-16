package cd4017be.lib.item;

import static net.minecraft.util.ActionResult.sidedSuccess;
import static net.minecraftforge.fml.network.NetworkHooks.openGui;

import cd4017be.lib.block.BlockTE;
import cd4017be.lib.container.ContainerGrid;
import cd4017be.lib.container.IUnnamedContainerProvider;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.InteractionHand;
import net.minecraft.world.World;

/**
 * @author CD4017BE */
public class GridHostItem extends TEModeledItem implements IUnnamedContainerProvider {

	public GridHostItem(BlockTE<?> id, Properties p) {
		super(id, p);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, InteractionHand InteractionHand) {
		if (InteractionHand == InteractionHand.OFF_InteractionHand) return super.use(world, player, InteractionHand);
		if (!world.isClientSide) {
			int slot = player.inventory.selected;
			openGui((ServerPlayerEntity)player, this, pkt -> pkt.writeByte(slot));
		}
		return sidedSuccess(player.getItemInInteractionHand(InteractionHand), world.isClientSide);
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerGrid(id, inv, inv.selected);
	}

}
