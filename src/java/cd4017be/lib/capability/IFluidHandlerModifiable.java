package cd4017be.lib.capability;

import cd4017be.lib.container.slot.SlotFluidInteractionHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidInteractionHandler;
import net.minecraftforge.items.IItemInteractionHandlerModifiable;

/**The fluid equivalent of {@link IItemInteractionHandlerModifiable}, mainly used for {@link SlotFluidInteractionHandler} on client side.
 * @author CD4017BE */
public interface IFluidInteractionHandlerModifiable extends IFluidInteractionHandler {

	void setFluidInTank(int tank, FluidStack stack);

}
