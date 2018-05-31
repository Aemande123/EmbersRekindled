package teamroots.embers.tileentity;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import teamroots.embers.EventManager;
import teamroots.embers.recipe.FluidMixingRecipe;
import teamroots.embers.recipe.RecipeRegistry;

public class TileEntityMixerBottom extends TileEntity implements ITileEntityBase, ITickable {
	public FluidTank north = new FluidTank(8000);
	public FluidTank south = new FluidTank(8000);
	public FluidTank east = new FluidTank(8000);
	public FluidTank west = new FluidTank(8000);
	public FluidTank[] tanks;
	Random random = new Random();
	int progress = -1;
	
	public TileEntityMixerBottom(){
		super();
		tanks = new FluidTank[]{north,south,east,west};
	}

	public FluidTank[] getTanks()
	{
		return tanks;
	}

	public ArrayList<FluidStack> getFluids() {
		ArrayList<FluidStack> fluids = new ArrayList<>();
		if (north.getFluid() != null) {
			fluids.add(north.getFluid());
		}
		if (south.getFluid() != null) {
			fluids.add(south.getFluid());
		}
		if (east.getFluid() != null) {
			fluids.add(east.getFluid());
		}
		if (west.getFluid() != null) {
			fluids.add(west.getFluid());
		}
		return fluids;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag){
		super.writeToNBT(tag);
		NBTTagCompound northTank = new NBTTagCompound();
		north.writeToNBT(northTank);
		tag.setTag("northTank", northTank);
		NBTTagCompound southTank = new NBTTagCompound();
		south.writeToNBT(southTank);
		tag.setTag("southTank", southTank);
		NBTTagCompound eastTank = new NBTTagCompound();
		east.writeToNBT(eastTank);
		tag.setTag("eastTank", eastTank);
		NBTTagCompound westTank = new NBTTagCompound();
		west.writeToNBT(westTank);
		tag.setTag("westTank", westTank);
		tag.setInteger("progress", progress);
		return tag;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag){
		super.readFromNBT(tag);
		north.readFromNBT(tag.getCompoundTag("northTank"));
		south.readFromNBT(tag.getCompoundTag("southTank"));
		east.readFromNBT(tag.getCompoundTag("eastTank"));
		west.readFromNBT(tag.getCompoundTag("westTank"));
		if (tag.hasKey("progress")){
			progress = tag.getInteger("progress");
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean activate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		this.invalidate();
		world.setTileEntity(pos, null);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != EnumFacing.UP && facing != EnumFacing.DOWN){
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null){
			switch (facing) {
			case DOWN:
				//
			case EAST:
				return (T)east;
			case NORTH:
				return (T)north;
			case SOUTH:
				return (T)south;
			case UP:
				//
			case WEST:
				return (T)west;
			default:
				//
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void update() {
		World world = getWorld();
		BlockPos pos = getPos();
		TileEntityMixerTop top = (TileEntityMixerTop) world.getTileEntity(pos.up());
		if (top != null) {
			double emberCost = 2.0;
			if (top.capability.getEmber() >= emberCost) {
				ArrayList<FluidStack> fluids = getFluids();
				FluidMixingRecipe recipe = RecipeRegistry.getMixingRecipe(fluids);
				if (recipe != null) {
					IFluidHandler tank = top.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
					FluidStack output = recipe.getResult(fluids);
					int amount = tank.fill(output, false);
					if (amount != 0) {
						tank.fill(output, true);
						consumeFluids(recipe);
						top.capability.removeAmount(emberCost, true);
						markDirty();
						top.markDirty();
					}
				}
			}
		}
	}

	public void consumeFluids(FluidMixingRecipe recipe) {
		for (FluidTank tank : tanks) {
			FluidStack tankFluid = tank.getFluid();
			boolean doContinue = true;
			for (int j = 0; j < recipe.inputs.size() && doContinue; j++) {
				FluidStack recipeFluid = recipe.inputs.get(j);
				if (recipeFluid != null && tankFluid != null && recipeFluid.getFluid() == tankFluid.getFluid()) {
					doContinue = false;
					tank.drain(recipeFluid.amount,true);
				}
			}
		}
	}
	
	public boolean dirty = false;
	
	@Override
	public void markForUpdate(){
		EventManager.markTEForUpdate(getPos(), this);
	}
	
	@Override
	public void markDirty(){
		markForUpdate();
		super.markDirty();
	}
}
