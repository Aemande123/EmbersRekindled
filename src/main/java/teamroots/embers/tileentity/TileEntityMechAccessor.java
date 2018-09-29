package teamroots.embers.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.EventManager;
import teamroots.embers.api.tile.IExtraCapabilityInformation;
import teamroots.embers.api.tile.IExtraDialInformation;
import teamroots.embers.block.BlockMechAccessor;
import teamroots.embers.util.Misc;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TileEntityMechAccessor extends TileEntity implements ITileEntityBase, IExtraDialInformation, IExtraCapabilityInformation {
    static HashSet<Class<? extends TileEntity>> ACCESSIBLE_TILES = new HashSet<>();

    public static void registerAccessibleTile(Class<? extends TileEntity> type) {
        ACCESSIBLE_TILES.add(type);
    }

    public static boolean canAccess(TileEntity tile) {
        Class<? extends TileEntity> tileClass = tile.getClass();
        return tile instanceof IMultiblockMachine || ACCESSIBLE_TILES.stream().anyMatch(type -> type.isAssignableFrom(tileClass));
    }

    Random random = new Random();

    public TileEntityMechAccessor() {
        super();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
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
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        EnumFacing accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        return (tile != null && canAccess(tile) && tile.hasCapability(capability, accessFace)) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        EnumFacing accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        return tile != null && canAccess(tile) ? tile.getCapability(capability, accessFace) : super.getCapability(capability,facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
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
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this);
    }

    public EnumFacing getFacing() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockMechAccessor)
            return state.getValue(BlockMechAccessor.facing);
        return null;
    }

    public TileEntity getAttachedMultiblock(EnumFacing facing) {
        if (facing != null)
            return world.getTileEntity(pos.offset(facing.getOpposite()));
        return null;
    }

    @Override
    public void addDialInformation(EnumFacing facing, List<String> information, String dialType) {
        EnumFacing accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraDialInformation && canAccess(tile))
            ((IExtraDialInformation) tile).addDialInformation(accessFace, information, dialType);
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        EnumFacing accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraCapabilityInformation)
            return ((IExtraCapabilityInformation) tile).hasCapabilityDescription(capability);
        return false;
    }

    @Override
    public void addCapabilityDescription(List<String> strings, Capability<?> capability, EnumFacing facing) {
        EnumFacing accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraCapabilityInformation)
            ((IExtraCapabilityInformation) tile).addCapabilityDescription(strings, capability, accessFace);
    }

    @Override
    public void addOtherDescription(List<String> strings, EnumFacing facing) {
        EnumFacing accessFace = getFacing();
        TileEntity tile = getAttachedMultiblock(accessFace);
        if (tile instanceof IExtraCapabilityInformation)
            ((IExtraCapabilityInformation) tile).addOtherDescription(strings, accessFace);
    }
}
