package cd4017be.lib.container.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemInteractionHandler;
import net.minecraftforge.items.SlotItemInteractionHandler;

/**
 *
 * @author CD4017BE
 */
public class SlotTank extends SlotItemInteractionHandler {

	public SlotTank(IItemInteractionHandler inv, int slot, int x, int y){
		super(inv, slot, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack item) {
		return FluidUtil.getFluidInteractionHandler(item) != null;
	}

}
