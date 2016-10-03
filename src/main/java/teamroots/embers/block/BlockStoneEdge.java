package teamroots.embers.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import teamroots.embers.RegistryManager;
import teamroots.embers.tileentity.ITileEntityBase;
import teamroots.embers.tileentity.TileEntityEmitter;
import teamroots.embers.tileentity.TileEntityLargeTank;
import teamroots.embers.tileentity.TileEntityPipe;
import teamroots.embers.tileentity.TileEntityTank;

public class BlockStoneEdge extends BlockBase {
	public static final PropertyInteger state = PropertyInteger.create("state", 0, 9);
	
	public static final AxisAlignedBB AABB_BASE = new AxisAlignedBB(0,0,0,1,1,1);
	public static final AxisAlignedBB AABB_NULL = new AxisAlignedBB(0,0,0,0,0,0);
	public static final AxisAlignedBB AABB_XFACE = new AxisAlignedBB(0.25,0,0,0.75,1,1);
	public static final AxisAlignedBB AABB_ZFACE = new AxisAlignedBB(0,0,0.25,1,1,0.75);
	public static final AxisAlignedBB AABB_POSZ = new AxisAlignedBB(0.25,0,0.25,0.75,1,1);
	public static final AxisAlignedBB AABB_NEGZ = new AxisAlignedBB(0.25,0,0,0.75,1,0.75);
	public static final AxisAlignedBB AABB_POSX = new AxisAlignedBB(0.25,0,0.25,1,1,0.75);
	public static final AxisAlignedBB AABB_NEGX = new AxisAlignedBB(0,0,0.25,0.75,1,0.75);
	public static final AxisAlignedBB AABB_PXNZCORNER = new AxisAlignedBB(0,0,0.25,0.75,1,1);
	public static final AxisAlignedBB AABB_PXPZCORNER = new AxisAlignedBB(0,0,0,0.75,1,0.75);
	public static final AxisAlignedBB AABB_NXPZCORNER = new AxisAlignedBB(0.25,0,0,1,1,0.75);
	public static final AxisAlignedBB AABB_NXNZCORNER = new AxisAlignedBB(0.25,0,0.25,1,1,1);
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		if (state.getValue(this.state) == 9 || state.getValue(this.state) == 4){
			return AABB_ZFACE;
		}
		if (state.getValue(this.state) == 2 || state.getValue(this.state) == 6){
			return AABB_XFACE;
		}
		if (state.getValue(this.state) == 1){
			return AABB_NXNZCORNER;
		}
		if (state.getValue(this.state) == 3){
			return AABB_NXPZCORNER;
		}
		if (state.getValue(this.state) == 5){
			return AABB_PXPZCORNER;
		}
		if (state.getValue(this.state) == 7){
			return AABB_PXNZCORNER;
		}
		return AABB_BASE;
	}

    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn)
    {
		if (state.getValue(this.state) == 9 || state.getValue(this.state) == 4){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_ZFACE);
		}
		if (state.getValue(this.state) == 2 || state.getValue(this.state) == 6){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_XFACE);
		}
		if (state.getValue(this.state) == 1){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_POSZ);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_POSX);
		}
		if (state.getValue(this.state) == 3){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_NEGZ);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_POSX);
		}
		if (state.getValue(this.state) == 5){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_NEGZ);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_NEGX);
		}
		if (state.getValue(this.state) == 7){
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_POSZ);
	        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_NEGX);
		}
    }
	
	public BlockStoneEdge(Material material, String name, boolean addToTab) {
		super(material, name, addToTab);
	}
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side){
		return true;
	}
	
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, state);
	}
	
	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(this.state);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(state,meta);
	}
	
	public void breakBlockSafe(World world, BlockPos pos, EntityPlayer player){
		if (world.getTileEntity(pos) instanceof ITileEntityBase){
			((ITileEntityBase)world.getTileEntity(pos)).breakBlock(world, pos, world.getBlockState(pos), player);
		}
		if (world.getBlockState(pos).getBlock() == RegistryManager.stoneEdge){
			if (world.getBlockState(pos).getValue(this.state) == 8){
				boolean foundBlock = false;
				for (int i = 1; i < 64 && !foundBlock; i ++){
					if (world.getTileEntity(pos.add(0,-i,0)) instanceof TileEntityLargeTank){
						((TileEntityLargeTank)world.getTileEntity(pos.add(0,-i,0))).updateCapacity();
						foundBlock = true;
					}
				}
			}
		}
		world.setBlockToAir(pos);
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player){
		if (!world.isRemote && !player.capabilities.isCreativeMode){
			world.spawnEntityInWorld(new EntityItem(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,new ItemStack(this,1,0)));
		}
		if (state.getValue(this.state) == 9){
			breakBlockSafe(world,pos.south(),player);
			breakBlockSafe(world,pos.south(2),player);
			breakBlockSafe(world,pos.east(),player);
			breakBlockSafe(world,pos.west(),player);
			breakBlockSafe(world,pos.east().south(),player);
			breakBlockSafe(world,pos.west().south(),player);
			breakBlockSafe(world,pos.east().south(2),player);
			breakBlockSafe(world,pos.west().south(2),player);
		}
		if (state.getValue(this.state) == 1){
			breakBlockSafe(world,pos.east(),player);
			breakBlockSafe(world,pos.east(2),player);
			breakBlockSafe(world,pos.south(),player);
			breakBlockSafe(world,pos.south(2),player);
			breakBlockSafe(world,pos.east().south(),player);
			breakBlockSafe(world,pos.east(2).south(),player);
			breakBlockSafe(world,pos.east().south(2),player);
			breakBlockSafe(world,pos.east(2).south(2),player);
		}
		if (state.getValue(this.state) == 2){
			breakBlockSafe(world,pos.east(),player);
			breakBlockSafe(world,pos.east(2),player);
			breakBlockSafe(world,pos.north(),player);
			breakBlockSafe(world,pos.south(),player);
			breakBlockSafe(world,pos.north().east(),player);
			breakBlockSafe(world,pos.south().east(),player);
			breakBlockSafe(world,pos.north().east(2),player);
			breakBlockSafe(world,pos.south().east(2),player);
		}
		if (state.getValue(this.state) == 3){
			breakBlockSafe(world,pos.east(),player);
			breakBlockSafe(world,pos.east(2),player);
			breakBlockSafe(world,pos.north(),player);
			breakBlockSafe(world,pos.north(2),player);
			breakBlockSafe(world,pos.east().north(),player);
			breakBlockSafe(world,pos.east(2).north(),player);
			breakBlockSafe(world,pos.east().north(2),player);
			breakBlockSafe(world,pos.east(2).north(2),player);
		}
		if (state.getValue(this.state) == 4){
			breakBlockSafe(world,pos.north(),player);
			breakBlockSafe(world,pos.north(2),player);
			breakBlockSafe(world,pos.east(),player);
			breakBlockSafe(world,pos.west(),player);
			breakBlockSafe(world,pos.east().north(),player);
			breakBlockSafe(world,pos.west().north(),player);
			breakBlockSafe(world,pos.east().north(2),player);
			breakBlockSafe(world,pos.west().north(2),player);
		}
		if (state.getValue(this.state) == 5){
			breakBlockSafe(world,pos.west(),player);
			breakBlockSafe(world,pos.west(2),player);
			breakBlockSafe(world,pos.north(),player);
			breakBlockSafe(world,pos.north(2),player);
			breakBlockSafe(world,pos.west().north(),player);
			breakBlockSafe(world,pos.west(2).north(),player);
			breakBlockSafe(world,pos.west().north(2),player);
			breakBlockSafe(world,pos.west(2).north(2),player);
		}
		if (state.getValue(this.state) == 6){
			breakBlockSafe(world,pos.west(),player);
			breakBlockSafe(world,pos.west(2),player);
			breakBlockSafe(world,pos.north(),player);
			breakBlockSafe(world,pos.south(),player);
			breakBlockSafe(world,pos.north().west(),player);
			breakBlockSafe(world,pos.south().west(),player);
			breakBlockSafe(world,pos.north().west(2),player);
			breakBlockSafe(world,pos.south().west(2),player);
		}
		if (state.getValue(this.state) == 7){
			breakBlockSafe(world,pos.west(),player);
			breakBlockSafe(world,pos.west(2),player);
			breakBlockSafe(world,pos.south(),player);
			breakBlockSafe(world,pos.south(2),player);
			breakBlockSafe(world,pos.west().south(),player);
			breakBlockSafe(world,pos.west(2).south(),player);
			breakBlockSafe(world,pos.west().south(2),player);
			breakBlockSafe(world,pos.west(2).south(2),player);
		}
		if (state.getValue(this.state) == 8){
			breakBlockSafe(world,pos.west(),player);
			breakBlockSafe(world,pos.south(),player);
			breakBlockSafe(world,pos.west().south(),player);
			breakBlockSafe(world,pos.west().north(),player);
			breakBlockSafe(world,pos.south().east(),player);
			breakBlockSafe(world,pos.north().east(),player);
			breakBlockSafe(world,pos.north(),player);
			breakBlockSafe(world,pos.east(),player);
		}
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		if (world.isAirBlock(pos.east())
			&& world.isAirBlock(pos.west())
			&& world.isAirBlock(pos.north())
			&& world.isAirBlock(pos.south())
			&& world.isAirBlock(pos.east().north())
			&& world.isAirBlock(pos.east().south())
			&& world.isAirBlock(pos.west().north())
			&& world.isAirBlock(pos.west().south())){
			return true;
		}
		return false;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state){
		if (state.getValue(this.state) == 0){
			world.setBlockState(pos, RegistryManager.stoneEdge.getStateFromMeta(8));
			world.setBlockState(pos.north(), RegistryManager.stoneEdge.getStateFromMeta(9));
			world.setBlockState(pos.north().west(), RegistryManager.stoneEdge.getStateFromMeta(1));
			world.setBlockState(pos.west(), RegistryManager.stoneEdge.getStateFromMeta(2));
			world.setBlockState(pos.south().west(), RegistryManager.stoneEdge.getStateFromMeta(3));
			world.setBlockState(pos.south(), RegistryManager.stoneEdge.getStateFromMeta(4));
			world.setBlockState(pos.south().east(), RegistryManager.stoneEdge.getStateFromMeta(5));
			world.setBlockState(pos.east(), RegistryManager.stoneEdge.getStateFromMeta(6));
			world.setBlockState(pos.north().east(), RegistryManager.stoneEdge.getStateFromMeta(7));
		}
		boolean foundBlock = false;
		for (int i = 0; i < 64 && !foundBlock; i ++){
			if (world.getTileEntity(pos.add(0,-i,0)) instanceof TileEntityLargeTank){
				((TileEntityLargeTank)world.getTileEntity(pos.add(0,-i,0))).updateCapacity();
			}
		}
	}
}
