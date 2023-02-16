package cd4017be.api.grid.port;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.Link;
import net.minecraft.core.BlockPos;
import net.minecraft.world.server.ServerLevel;

/**Grid port InteractionHandler for remote block interaction.
 * @author CD4017BE */
@FunctionalInterface
public interface IBlockSupplier {

	/**@param rec recursion depth
	 * @return the block position and dimension */
	ImmutablePair<BlockPos, ServerLevel> getBlock(int rec);

	default ImmutablePair<BlockPos, ServerLevel> getBlock() {
		return getBlock(Link.REC_BLOCK);
	}

	/** port type id */
	int TYPE_ID = 4;

	IBlockSupplier NOP = r -> null;

	static IBlockSupplier of(Object InteractionHandler, IBlockSupplier fallback) {
		return InteractionHandler instanceof IBlockSupplier ? (IBlockSupplier)InteractionHandler : fallback;
	}

	static IBlockSupplier of(Object InteractionHandler) {
		return InteractionHandler instanceof IBlockSupplier ? (IBlockSupplier)InteractionHandler : NOP;
	}

	static String toString(IBlockSupplier InteractionHandler) {
		ImmutablePair<BlockPos, ServerLevel> dest = InteractionHandler.getBlock();
		if (dest == null) return "cd4017be.unloaded";
		return String.format(
			"\\(%d,%d,%d)%s", dest.left.getX(),
			dest.left.getY(), dest.left.getZ(),
			dest.right.dimension().location().getPath()
		);
	}

}
