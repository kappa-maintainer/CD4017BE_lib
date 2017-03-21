package cd4017be.lib.templates;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import static net.minecraftforge.items.ItemHandlerHelper.*;

import java.util.function.ObjIntConsumer;
import java.util.function.IntFunction;

public class LinkedInventory implements IItemHandler {

	private final int slots;
	private final IntFunction<ItemStack> get;
	private final ObjIntConsumer<ItemStack> set;

	public LinkedInventory(int slots, IntFunction<ItemStack> get, ObjIntConsumer<ItemStack> set) {
		this.slots = slots;
		this.get = get;
		this.set = set;
	}

	@Override
	public int getSlots() {return slots;}

	@Override
	public ItemStack getStackInSlot(int slot) {return get.apply(slot);}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
		ItemStack item = get.apply(slot);
		if (item == null) {
			int n = stack.getMaxStackSize();
			if (stack.stackSize <= n) {
				if (!sim) set.accept(stack.copy(), slot);
				return null;
			} else {
				if (!sim) set.accept(copyStackWithSize(stack, n), slot);
				return copyStackWithSize(stack, stack.stackSize - n);
			}
		} else if (canItemStacksStack(stack, item)) {
			int n = item.getMaxStackSize() - item.stackSize;
			if (n <= 0) return stack;
			else if (stack.stackSize <= n) {
				if (!sim) {
					item.stackSize += stack.stackSize;
					set.accept(item, slot);
				}
				return null;
			} else {
				if (!sim) {
					item.stackSize += n;
					set.accept(item, slot);
				}
				return copyStackWithSize(stack, stack.stackSize - n);
			}
		} else return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack item = get.apply(slot);
		if (item == null) return null;
		if (amount >= item.stackSize) {
			set.accept(null, slot);
			return item;
		} else {
			item.stackSize -= amount;
			set.accept(item, slot);
			return copyStackWithSize(item, amount);
		}
	}

}
