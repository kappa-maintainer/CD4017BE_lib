package cd4017be.lib.item;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import net.minecraft.block.BlockState;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**@author CD4017BE */
public abstract class GridItem extends DocumentedItem implements IGridItem {

	public GridItem(Properties p) {
		super(p);
	}

	public InteractionResult useOn(ItemUseContext context) {
		return placeAndInteract(context);
	}

	@Override
	public boolean
	doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
		return world.getBlockEntity(pos) instanceof IGridHost;
	}

	@Override
	public boolean canAttackBlock(
		BlockState state, World world, BlockPos pos, PlayerEntity player
	) {
		if (!world.isClientSide && player.isCreative())
			world.getBlockState(pos).attack(world, pos, player);
		return false;
	}

}
