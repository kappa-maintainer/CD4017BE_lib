package cd4017be.lib.render;

import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * 
 * @author CD4017BE
 */
@OnlyIn(Dist.CLIENT)
public class HybridFastTESR {

	/**
	 * @param te the BlockEntity
	 * @return whether the player currently aims at the given BlockEntity
	 */
	public static boolean isAimedAt(BlockEntity te) {
		@SuppressWarnings("resource")
		RayTraceResult rts = Minecraft.getInstance().hitResult;
		return rts instanceof BlockRayTraceResult
		&& te.getBlockPos().equals(((BlockRayTraceResult)rts).getBlockPos());
	}

	/**
	 * @param te the BlockEntity
	 * @param range maximum distance in blocks
	 * @return whether given BlockEntity is within given distance to the camera
	 */
	public static boolean isWithinRange(BlockEntity te, double range) {
		EntityRendererManager rm = Minecraft.getInstance().getEntityRenderDispatcher();
		BlockPos pos = te.getBlockPos();
		return rm.distanceToSqr(
			pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5
		) < range * range;
	}

}
