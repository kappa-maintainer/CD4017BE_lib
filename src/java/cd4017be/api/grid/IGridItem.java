package cd4017be.api.grid;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**Implemented by {@link Item}s that create or interact with {@link GridPart}s.
 * @author CD4017BE */
public interface IGridItem {

	/**@param grid
	 * @param stack
	 * @param player
	 * @param InteractionHand used InteractionHand or null if left click
	 * @param hit original ray trace hit
	 * @return action result */
	InteractionResult onInteract(
		IGridHost grid, ItemStack stack, PlayerEntity player,
		InteractionHand InteractionHand, BlockRayTraceResult hit
	);

	default InteractionResult placeAndInteract(ItemUseContext itemContext) {
		BlockItemUseContext context = new BlockItemUseContext(itemContext);
		BlockPos pos = new BlockPos(context.getClickLocation().add(
			Vector3d.atLowerCornerOf(context.getClickedFace().getNormal()).scale(0.125)
		));
		World world = context.getLevel();
		BlockEntity te = world.getBlockEntity(pos);
		if (!(te instanceof IGridHost)) {
			if (!world.getBlockState(pos).canBeReplaced(context))
				return InteractionResult.FAIL;
			if (!world.setBlock(pos, GridPart.GRID_HOST_BLOCK, 11))
				return InteractionResult.FAIL;
			te = world.getBlockEntity(pos);
			if (!(te instanceof IGridHost))
				return InteractionResult.FAIL;
		}
		return onInteract(
			(IGridHost)te, context.getItemInInteractionHand(), context.getPlayer(), context.getInteractionHand(),
			new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), pos, false)
		);
	}

	/**@return a new GridPart for deserialization.
	 * Items that only want to interact with grids
	 * but not represent parts may return null. */
	@Nullable GridPart createPart();

}
