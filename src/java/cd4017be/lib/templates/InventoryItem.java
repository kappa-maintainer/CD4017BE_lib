package cd4017be.lib.templates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class InventoryItem implements IItemHandlerModifiable {

	private final InventoryPlayer ref;
	private final IItemInventory inv;
	private final ItemStack[] cache;
	
	public InventoryItem(EntityPlayer player) {
		this.ref = player.inventory;
		ItemStack item = ref.mainInventory[ref.currentItem];
		if (item == null || !(item.getItem() instanceof IItemInventory)) throw new IllegalArgumentException("Held item not InventoryItem compatible!");
		this.inv = (IItemInventory)item.getItem();
		this.cache = new ItemStack[inv.getSlots(item)];
		for (int i = 0; i < cache.length; i++)
			cache[i] = inv.getStack(item, i);
	}

	@Override
	public int getSlots() {
		return cache.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return cache[slot];
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
		ItemStack item = cache[slot];
		int m = Math.min(stack.getMaxStackSize() - (item == null ? 0 : item.stackSize), stack.stackSize); 
		if (m <= 0 || !(item == null || ItemHandlerHelper.canItemStacksStack(item, stack))) return stack;
		if (!sim) {
			if (item != null) item.stackSize -= m;
			else item = ItemHandlerHelper.copyStackWithSize(stack, m);
			this.setStackInSlot(slot, item);
		}
		return (m = stack.stackSize - m) > 0 ? ItemHandlerHelper.copyStackWithSize(stack, m) : null;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean sim) {
		ItemStack item = this.getStackInSlot(slot);
		if (item == null) return null;
		int m = Math.min(item.stackSize, amount);
		if (!sim) {
			if (item.stackSize <= m) item = null;
			else item.stackSize -= m;
			this.setStackInSlot(slot, item);
		}
		return ItemHandlerHelper.copyStackWithSize(item, m);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		cache[slot] = stack;
		ItemStack item = ref.mainInventory[ref.currentItem];
		if (item != null && item.getItem() == inv) 
			inv.setStack(item, slot, stack);
	}

	public interface IItemInventory {
		public int getSlots(ItemStack inv);
		public ItemStack getStack(ItemStack inv, int slot);
		public void setStack(ItemStack inv, int slot, ItemStack stack);
	}
	
}
