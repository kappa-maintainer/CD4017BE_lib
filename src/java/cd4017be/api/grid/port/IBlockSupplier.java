package cd4017be.api.grid.port;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.Link;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**Grid port handler for remote block interaction.
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

	static IBlockSupplier of(Object handler, IBlockSupplier fallback) {
		return handler instanceof IBlockSupplier ? (IBlockSupplier)handler : fallback;
	}

	static IBlockSupplier of(Object handler) {
		return handler instanceof IBlockSupplier ? (IBlockSupplier)handler : NOP;
	}

	static String toString(IBlockSupplier handler) {
		ImmutablePair<BlockPos, ServerLevel> dest = handler.getBlock();
		if (dest == null) return "cd4017be.unloaded";
		return String.format(
			"\\(%d,%d,%d)%s", dest.left.getX(),
			dest.left.getY(), dest.left.getZ(),
			dest.right.dimension().location().getPath()
		);
	}

}
