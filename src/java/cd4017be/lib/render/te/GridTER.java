package cd4017be.lib.render.te;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.api.grid.IDynamicPart;
import cd4017be.lib.BlockEntity.Grid;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockEntity.BlockEntityRenderer;
import net.minecraft.client.renderer.BlockEntity.BlockEntityRendererDispatcher;


/**
 * @author CD4017BE */
public class GridTER extends BlockEntityRenderer<Grid> {

	public GridTER(BlockEntityRendererDispatcher terd) {
		super(terd);
	}

	@Override
	public void render(
		Grid te, float t, MatrixStack ms, IRenderTypeBuffer rtb, int light, int overlay
	) {
		IDynamicPart[] parts = te.dynamicParts;
		if (parts == null) return;
		long o = te.opaque;
		for (IDynamicPart part : parts) part.render(ms, rtb, light, overlay, light, o);
	}

}
