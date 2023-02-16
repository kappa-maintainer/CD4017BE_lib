package cd4017be.lib.container.slot;

import java.util.function.Predicate;

import cd4017be.lib.capability.IFluidInteractionHandlerModifiable;
import cd4017be.lib.capability.IMultiFluidInteractionHandler;
import cd4017be.lib.container.AdvancedContainer;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidInteractionHandler;
import net.minecraftforge.items.SlotItemInteractionHandler;

/**The fluid equivalent of {@link SlotItemInteractionHandler} for use in {@link AdvancedContainer}.
 * Slot interaction allows filling and emptying of fluid containers with the referenced Fluid Tank.
 * @author CD4017BE */
public class SlotFluidInteractionHandler extends Slot implements IFluidSlot, ISpecialSlot {

	private static final IInventory emptyInventory = new Inventory(0);
	private final IFluidInteractionHandler fluidInteractionHandler;

	public SlotFluidInteractionHandler(IFluidInteractionHandler fluidInteractionHandler, int index, int xPosition, int yPosition) {
		super(emptyInventory, index, xPosition, yPosition);
		this.fluidInteractionHandler = fluidInteractionHandler;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack getItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public void set(ItemStack stack) {
		throw new UnsupportedOperationException("Fluid slots don't have an item stack to set!");
	}

	@Override
	public int getMaxStackSize() {
		return 0;
	}

	@Override
	public ItemStack remove(int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean mayPickup(PlayerEntity playerIn) {
		return false;
	}

	//fluid slot

	@Override
	public FluidStack getFluid() {
		return fluidInteractionHandler.getFluidInTank(getSlotIndex());
	}

	@Override
	public void putFluid(FluidStack stack) {
		((IFluidInteractionHandlerModifiable)fluidInteractionHandler).setFluidInTank(getSlotIndex(), stack);
	}

	@Override
	public int getCapacity() {
		return fluidInteractionHandler.getTankCapacity(getSlotIndex());
	}

	@Override
	public boolean insertHereOnly(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack onClick(int b, ClickType ct, PlayerEntity player, AdvancedContainer advancedContainer) {
		ItemStack curItem = player.inventory.getCarried();
		if (ct == ClickType.CLONE) {
			if (!curItem.isEmpty()) return ItemStack.EMPTY;
			FluidStack stack = getFluid();
			Predicate<FluidStack> filter = f ->
				stack.isEmpty() ? fluidInteractionHandler.isFluidValid(getSlotIndex(), f)
					: f.isEmpty() || f.isFluidEqual(stack);
			NonNullList<ItemStack> inv =  player.inventory.items;
			for (int i = 0; i < inv.size(); i++) {
				ItemStack item = inv.get(i);
				if (FluidUtil.getFluidContained(item).filter(filter).isPresent()) {
					player.inventory.setCarried(item);
					inv.set(i, curItem);
					break;
				}
			}
			if (player.isCreative() && !stack.isEmpty())
				player.inventory.setCarried(FluidUtil.getFilledBucket(stack));
		} else if (ct == ClickType.PICKUP || ct == ClickType.PICKUP_ALL || ct == ClickType.QUICK_MOVE) {
			IFluidInteractionHandler inv = fluidInteractionHandler;
			if (inv instanceof IMultiFluidInteractionHandler)
				inv = ((IMultiFluidInteractionHandler)inv).accessTank(getSlotIndex());
			int limit = ct == ClickType.QUICK_MOVE ? Integer.MAX_VALUE : 1000;
			FluidActionResult r;
			if (b == 0)
				r = FluidUtil.tryEmptyContainerAndStow(curItem, inv, null, limit, player, true);
			else r = FluidUtil.tryFillContainerAndStow(curItem, inv, null, limit, player, true);
			if (r.success) player.inventory.setCarried(r.result);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(ItemStack stack, boolean sim) {
		return stack;
	}

	@Override
	public ItemStack extractItem(int am, boolean sim) {
		return ItemStack.EMPTY;
	}

}
