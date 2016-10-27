package cd4017be.lib.templates;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasicInventory implements IItemHandlerModifiable {

	public final ItemStack[] items;

	public BasicInventory(int slots) {
		items = new ItemStack[slots];
	}

	@Override
	public ItemStack getStackInSlot(int s) 
	{
		return items[s];
	}

	@Override
	public int getSlots() {
		return items.length;
	}

	@Override
	public ItemStack insertItem(int i, ItemStack stack, boolean sim) {
		ItemStack item = items[i];
		int m = Math.min(stack.getMaxStackSize() - (item == null ? 0 : item.stackSize), stack.stackSize); 
		if (m <= 0 || (item != null && !ItemHandlerHelper.canItemStacksStack(item, stack))) return stack;
		if (!sim) {
			if (item == null) item = ItemHandlerHelper.copyStackWithSize(stack, m);
			else item.stackSize += m;
			items[i] = item;
		}
		return (m = stack.stackSize - m) > 0 ? ItemHandlerHelper.copyStackWithSize(stack, m) : null;
	}

	@Override
	public ItemStack extractItem(int i, int m, boolean sim) {
		ItemStack item = items[i];
		if (item == null || (m = item.stackSize < m ? item.stackSize : m) <= 0) return null;
		if (!sim) {
			if (item.stackSize <= m) items[i] = null;
			else items[i].stackSize -= m;
		}
		return ItemHandlerHelper.copyStackWithSize(item, m);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		items[slot] = stack;
	}

}
