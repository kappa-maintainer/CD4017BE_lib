package cd4017be.lib.block;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.BlockEntity.BaseBlockEntity;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.world.entity.projectile.ProjecBlockEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.BlockEntity.BlockEntityType;
import net.minecraft.BlockEntity.BlockEntityType.Builder;
import net.minecraft.util.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.util.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**Passes most events and actions to interfaces implemented by the BlockEntity.
 * @param <T> the BlockEntity used by this block
 * @author CD4017BE */
public class BlockTE<T extends BlockEntity> extends Block {

	public BlockEntityType<T> tileType;
	public final int InteractionHandlerFlags;

	/**@param properties the block properties
	 * @param flags events passed to the BlockEntity
	 * @see #flags(Class) */
	public BlockTE(Properties properties, int flags) {
		super(properties);
		this.InteractionHandlerFlags = flags;
	}

	@Override
	public boolean hasBlockEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockEntity createBlockEntity(BlockState state, IBlockReader world) {
		if (!hasBlockEntity(state)) return null;
		BlockEntity te = tileType.create();
		if (te instanceof BaseBlockEntity)
			((BaseBlockEntity)te).unloaded = false;
		return te;
	}

	/**@param factory the BlockEntity factory function
	 * @return the BlockEntityType created for this block */
	public BlockEntityType<T> makeTEType(Function<BlockEntityType<T>, T> factory) {
		return makeTEType(factory, this);
	}

	/**@param factory the BlockEntity factory function
	 * @param blocks the assigned blocks
	 * @return the BlockEntityType created for the given blocks */
	@SafeVarargs
	public static <T extends BlockEntity> BlockEntityType<T> makeTEType(
		Function<BlockEntityType<T>, T> factory, BlockTE<T>... blocks
	) {
		BlockTE<T> block = blocks[0];
		BlockEntityType<T> type = Builder.of(() -> factory.apply(block.tileType), blocks).build(null);
		type.setRegistryName(block.getRegistryName());
		for (BlockTE<T> b : blocks) b.tileType = type;
		return type;
	}

	private final <I> void InteractionHandleTE(
		BlockState state, int event, IWorldReader world, BlockPos pos,
		Class<I> type, Consumer<I> action
	) {
		if((InteractionHandlerFlags & event) == 0 || !hasBlockEntity(state)) return;
		BlockEntity te = world.getBlockEntity(pos);
		if(type.isInstance(te))
			action.accept(type.cast(te));
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(
		BlockState state, World world, BlockPos pos,
		BlockState newState, boolean isMoving
	) {
		InteractionHandleTE(
			state, H_BREAK, world, pos, ITEBreak.class,
			te -> te.onBreak(newState, isMoving)
		);
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public void onNeighborChange(
		BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor
	) {
		InteractionHandleTE(
			state, H_NEIGHBOR, world, pos, ITENeighborChange.class,
			te -> te.onNeighborTEChange(neighbor)
		);
	}

	@Override
	public void neighborChanged(
		BlockState state, World world, BlockPos pos,
		Block block, BlockPos fromPos, boolean isMoving
	) {
		InteractionHandleTE(
			state, H_UPDATE, world, pos, ITEBlockUpdate.class,
			te -> te.onNeighborBlockChange(fromPos, block, isMoving)
		);
	}

	@Override
	public InteractionResult use(
		BlockState state, World world, BlockPos pos,
		PlayerEntity player, InteractionHand InteractionHand, BlockRayTraceResult hit
	) {
		if (!hasBlockEntity(state)) return InteractionResult.PASS;
		if((InteractionHandlerFlags & H_INTERACT) != 0) {
			BlockEntity te = world.getBlockEntity(pos);
			return te instanceof ITEInteract ? ((ITEInteract)te).onActivated(player, InteractionHand, hit)
				: InteractionResult.PASS;
		}
		INamedContainerProvider ncp = getMenuProvider(state, world, pos);
		if (ncp == null) return InteractionResult.PASS;
		if (!(player instanceof ServerPlayerEntity))
			return InteractionResult.SUCCESS;
		ServerPlayerEntity splayer = (ServerPlayerEntity)player;
		if (ncp instanceof ISpecialContainerProvider)
			NetworkHooks.openGui(splayer, ncp, (ISpecialContainerProvider)ncp);
		else NetworkHooks.openGui(splayer, ncp, pos);
		return InteractionResult.CONSUME;
	}

	@Override
	public void attack(
		BlockState state, World world, BlockPos pos, PlayerEntity player
	) {
		InteractionHandleTE(
			state, H_INTERACT, world, pos, ITEInteract.class,
			te -> te.onClicked(player)
		);
	}

	@Override
	public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
		InteractionHandleTE(
			state, H_COLLIDE, world, pos, ITECollision.class,
			te -> te.onEntityCollided(entity)
		);
	}

	@Override
	public void onProjectileHit(
		World world, BlockState state, BlockRayTraceResult hit, ProjecBlockEntity projectile
	) {
		InteractionHandleTE(
			state, H_COLLIDE, world, hit.getBlockPos(), ITECollision.class,
			te -> te.onProjectileCollided(projectile, hit)
		);
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return (InteractionHandlerFlags & H_REDSTONE) != 0 && hasBlockEntity(state);
	}

	@Override
	public boolean shouldCheckWeakPower(
		BlockState state, IWorldReader world, BlockPos pos, Direction side
	) {
		return !isSignalSource(state) && super.shouldCheckWeakPower(state, world, pos, side);
	}

	@Override
	public int getSignal(
		BlockState state, IBlockReader world, BlockPos pos, Direction side
	) {
		if(!isSignalSource(state)) return 0;
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof ITERedstone ? ((ITERedstone)te).redstoneSignal(side, false) : 0;
	}

	@Override
	public int getDirectSignal(
		BlockState state, IBlockReader world, BlockPos pos, Direction side
	) {
		if(!isSignalSource(state)) return 0;
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof ITERedstone ? ((ITERedstone)te).redstoneSignal(side, true) : 0;
	}

	@Override
	public boolean canConnectRedstone(
		BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side
	) {
		if(!isSignalSource(state)) return false;
		if(side == null) return true;
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof ITERedstone && ((ITERedstone)te).redstoneConnection(side);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return (InteractionHandlerFlags & H_COMPARATOR) != 0 && hasBlockEntity(state);
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
		if(!hasAnalogOutputSignal(state)) return 0;
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof ITEComparator ? ((ITEComparator)te).comparatorSignal() : 0;
	}

	@Override
	public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
		if ((InteractionHandlerFlags & H_GUI) == 0 || !hasBlockEntity(state)) return null;
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof INamedContainerProvider ? (INamedContainerProvider)te : null;
	}

	@Override
	public VoxelShape getShape(
		BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context
	) {
		if ((InteractionHandlerFlags & H_SHAPE) != 0 && hasBlockEntity(state)) {
			BlockEntity te = world.getBlockEntity(pos);
			if (te instanceof ITEShape)
				return ((ITEShape)te).getShape(context);
		}
		return VoxelShapes.block();
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return VoxelShapes.block();
	}

	@Override
	public ItemStack getPickBlock(
		BlockState state, RayTraceResult target,
		IBlockReader world, BlockPos pos, PlayerEntity player
	) {
		if ((InteractionHandlerFlags & H_ITEMDATA) != 0 && hasBlockEntity(state)) {
			BlockEntity te = world.getBlockEntity(pos);
			if (te instanceof ITEPickItem)
				return ((ITEPickItem)te).getPickItem((BlockRayTraceResult)target, player);
		}
		return new ItemStack(this);
	}

	@Override
	public ItemStack getCloneItemStack(IBlockReader world, BlockPos pos, BlockState state) {
		ItemStack stack = new ItemStack(this);
		if ((InteractionHandlerFlags & H_ITEMDATA) != 0 && hasBlockEntity(state)) {
			BlockEntity te = world.getBlockEntity(pos);
			if (te instanceof ITEPickItem)
				return ((ITEPickItem)te).getItem();
			else if (te != null)
				te.save(stack.getOrCreateTagElement(TE_TAG));
		}
		return stack;
	}

	@Override
	public void playerWillDestroy(
		World world, BlockPos pos, BlockState state, PlayerEntity player
	) {
		super.playerWillDestroy(world, pos, state, player);
		if (world.isClientSide || !player.isCreative()) return;
		if ((InteractionHandlerFlags & H_ITEMDATA) == 0 || !hasBlockEntity(state)) return;
		BlockEntity te = world.getBlockEntity(pos);
		if (te == null) return;
		ItemStack stack;
		if (te instanceof ITEPickItem) {
			stack = ((ITEPickItem)te).getItem();
			if (!stack.hasTag()) return;
		} else {
			stack = new ItemStack(this);
			CompoundTag nbt = te.save(stack.getOrCreateTagElement(TE_TAG));
			nbt.remove("id");
			nbt.remove("x");
			nbt.remove("y");
			nbt.remove("z");
		}
		ItemFluidUtil.dropStack(stack, world, pos);
	}

	@Override
	public void setPlacedBy(
		World world, BlockPos pos, BlockState state,
		LivingEntity entity, ItemStack stack
	) {
		InteractionHandleTE(state, H_PLACE, world, pos, ITEPlace.class,
			te -> te.onPlace(state, stack, entity));
	}

	public static final String TE_TAG = "BlockEntityTag";

	/** BlockEntity InteractionHandler flags */
	public static final int
	H_BREAK = 1, H_NEIGHBOR = 2, H_UPDATE = 4, H_INTERACT = 8,
	H_COLLIDE = 16, H_REDSTONE = 32, H_COMPARATOR = 64, H_DROPS = 128,
	H_GUI = 256, H_SHAPE = 512, H_ITEMDATA = 1024, H_PLACE = 2048;

	/**@param c BlockEntity class
	 * @return InteractionHandler flags based on implemented interfaces */
	public static int flags(Class<?> c) {
		int f = 0;
		if(ITEBreak.class.isAssignableFrom(c)) f |= H_BREAK;
		if(ITENeighborChange.class.isAssignableFrom(c)) f |= H_NEIGHBOR;
		if(ITEBlockUpdate.class.isAssignableFrom(c)) f |= H_UPDATE;
		if(ITEInteract.class.isAssignableFrom(c)) f |= H_INTERACT;
		if(ITECollision.class.isAssignableFrom(c)) f |= H_COLLIDE;
		if(ITERedstone.class.isAssignableFrom(c)) f |= H_REDSTONE;
		if(ITEComparator.class.isAssignableFrom(c)) f |= H_COMPARATOR;
		if(INamedContainerProvider.class.isAssignableFrom(c)) f |= H_GUI;
		if(ITEShape.class.isAssignableFrom(c)) f |= H_SHAPE;
		if(ITEPickItem.class.isAssignableFrom(c)) f |= H_ITEMDATA;
		if(ITEPlace.class.isAssignableFrom(c)) f |= H_PLACE;
		return f;
	}

	public interface ITEBreak {

		void onBreak(BlockState newState, boolean moving);
	}

	public interface ITENeighborChange {

		void onNeighborTEChange(BlockPos from);
	}

	public interface ITEBlockUpdate {

		void onNeighborBlockChange(BlockPos from, Block block, boolean moving);
	}

	public interface ITEInteract {

		InteractionResult onActivated(PlayerEntity player, InteractionHand InteractionHand, BlockRayTraceResult hit);

		void onClicked(PlayerEntity player);
	}

	public interface ITECollision {

		/** when entity collides into this block (<b>doesn't work on fullCube
		 * blocks!</b>) */
		void onEntityCollided(Entity entity);

		/** when projectile collides into this block */
		default void
		onProjectileCollided(ProjecBlockEntity projectile, BlockRayTraceResult hit) {}
	}

	public interface ITERedstone {

		/** @param side face of neighbor block to emit in
		 * @param strong whether requesting strong signal
		 * @return emitted redstone signal */
		int redstoneSignal(Direction side, boolean strong);

		/** @param side face of neighbor block to emit in
		 * @return whether to connect */
		boolean redstoneConnection(@Nullable Direction side);
	}

	public interface ITEComparator {

		/** @return comparator signal */
		int comparatorSignal();
	}

	public interface ISpecialContainerProvider extends Consumer<FriendlyByteBuf>, IUnnamedContainerProvider {
		/**Write extra data to be send to client for container creation.
		 * @param extraData */
		@Override
		void accept(FriendlyByteBuf extraData);
	}

	public interface ITEShape {
		/**@param context what the shape is used for
		 * @return block shape for collision, ray-trace and outline */
		VoxelShape getShape(ISelectionContext context);
	}

	public interface ITEPickItem {

		/**@return item for creative harvest and pick block. */
		ItemStack getItem();

		/**@param target
		 * @param player
		 * @return item for creative pick block */
		default ItemStack getPickItem(BlockRayTraceResult target, PlayerEntity player) {
			return getItem();
		}
	}

	public interface ITEPlace {

		void onPlace(BlockState state, ItemStack stack, LivingEntity entity);
	}

}
