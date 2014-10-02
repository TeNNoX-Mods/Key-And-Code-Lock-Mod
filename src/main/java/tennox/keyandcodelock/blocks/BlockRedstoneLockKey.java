package tennox.keyandcodelock.blocks;

import tennox.keyandcodelock.KeyAndCodeLock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRedstoneLockKey extends BlockContainer {
	IIcon texture_off;
	IIcon texture_on;

	public BlockRedstoneLockKey(Material material) {
		super(material);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.texture_off = par1IconRegister.registerIcon("keyandcodelock:keyredstonelock");
		this.texture_on = par1IconRegister.registerIcon("keyandcodelock:keyredstonelock_on");
		this.blockIcon = par1IconRegister.registerIcon("keyandcodelock:redstonelock");
	}

	// BlockDispenser BlockFurnace
	@Override
	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int side) {
		int meta = iblockaccess.getBlockMetadata(i, j, k);
		int l = meta & 7;
		IIcon tex = isPowering(iblockaccess, i, j, k) ? this.texture_on : this.texture_off;

		return side == l ? (l != 1 && l != 0 ? tex : tex) : (l != 1 && l != 0 ? (side != 1 && side != 0 ? this.blockIcon : this.blockIcon) : tex);
	}

	public IIcon getIcon(int side, int meta) {
		return side == 4 ? this.texture_on : this.blockIcon;
	}

	public int isProvidingStrongPower(IBlockAccess blockaccess, int i, int j, int k, int side) {
		return side == 1 ? isProvidingWeakPower(blockaccess, i, j, k, side) : 0;
	}

	public int isProvidingWeakPower(IBlockAccess blockaccess, int i, int j, int k, int side) {
		if (!isPowering(blockaccess, i, j, k)) {
			return 0;
		}
		int i1 = blockaccess.getBlockMetadata(i, j, k);
		return (i1 == 2) && (side == 4) ? 0 : (i1 == 1) && (side == 5) ? 0 : (i1 == 4) && (side == 2) ? 0 : (i1 == 3) && (side == 3) ? 0 : (i1 == 5) && (side == 1) ? 0 : 15;
	}

	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (player.isSneaking()) {
			return false;
		}
		TileEntityKeyLocked lock = (TileEntityKeyLocked) world.getTileEntity(i, j, k);
		ItemStack item = player.getCurrentEquippedItem();

		if ((item == null) || ((item.getItem() != KeyAndCodeLock.key) && (item.getItem() != KeyAndCodeLock.remover))) {
			if (world.isRemote) {
				if ((lock.name == null) || (lock.hash == 0))
					player.addChatMessage(new ChatComponentText("This lock is not paired with a key yet!"));
				else
					player.addChatMessage(new ChatComponentText("This lock is locked. You need a key to open it!"));
			}
			return true;
		}
		if (item.getItem() == KeyAndCodeLock.key)
			handleKey(world, player, lock, item, i, j, k);
		else if (item.getItem() == KeyAndCodeLock.remover) {
			handleRemover(world, player, i, j, k);
		}
		return true;
	}

	public boolean isPowering(IBlockAccess iblockaccess, int i, int j, int k) {
		return iblockaccess.getBlockMetadata(i, j, k) > 8;
	}

	public void changePowerState(World world, int i, int j, int k) {
		if (isPowering(world, i, j, k))
			world.setBlockMetadataWithNotify(i, j, k, world.getBlockMetadata(i, j, k) - 8, 3);
		else {
			world.setBlockMetadataWithNotify(i, j, k, world.getBlockMetadata(i, j, k) + 8, 3);
		}

		world.notifyBlocksOfNeighborChange(i - 1, j, k, this);
		world.notifyBlocksOfNeighborChange(i + 1, j, k, this);
		world.notifyBlocksOfNeighborChange(i, j - 1, k, this);
		world.notifyBlocksOfNeighborChange(i, j + 1, k, this);
		world.notifyBlocksOfNeighborChange(i, j, k - 1, this);
		world.notifyBlocksOfNeighborChange(i, j, k + 1, this);
	}

	private void handleKey(World world, EntityPlayer player, TileEntityKeyLocked lock, ItemStack item, int i, int j, int k) {
		if (item.stackTagCompound == null)
			item.setTagCompound(new NBTTagCompound());
		String name = item.stackTagCompound.getString("name");
		int hash = item.stackTagCompound.getInteger("hash");

		if ((item != null) && (item.getItem() == KeyAndCodeLock.key) && (!name.equals("")) && (hash > 0) && (lock.name == null)) {
			lock.name = name;
			lock.hash = hash;
			if (world.isRemote)
				player.addChatMessage(new ChatComponentText("This lock is now locked with \"" + name + "\""));
			changePowerState(world, i, j, k);
			return;
		}

		if (world.isRemote) {
			if (((name.equals("")) || (hash == 0)) && (lock.name == null)) {
				player.openGui(KeyAndCodeLock.instance, KeyAndCodeLock.keynameGUI, world, i, j, k);
			}
		} else if ((!name.equals("")) || (hash > 0) || (lock.name != null))
			if ((lock.name.equals(name)) && (lock.hash == hash))
				changePowerState(world, i, j, k);
			else if (lock.name.equals(name))
				player.addChatMessage(new ChatComponentText("This is the wrong key! This lock is locked to another key with the name \"" + lock.name + "\""));
			else if ((name.equals("")) || (hash == 0))
				player.addChatMessage(new ChatComponentText("This is locked with \"" + lock.name + "\""));
			else
				player.addChatMessage(new ChatComponentText("This is the wrong key! This lock is locked with \"" + lock.name + "\""));
	}

	private void handleRemover(World world, EntityPlayer player, int i, int j, int k) {
		if (isPowering(world, i, j, k)) {
			dropBlockAsItem(world, i, j, k, (ItemStack) getDrops(world, i, j, k, 0, 1).get(0));
			world.setBlockToAir(i, j, k);
		} else if (world.isRemote) {
			player.addChatMessage(new ChatComponentText("Please open the lock to remove it!"));
		}
	}

	public boolean canProvidePower() {
		return true;
	}

	public boolean isBlockNormalCube(World world, int x, int y, int z) {
		return false;
	}

	public boolean isBlockSolid(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return true;
	}

	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
		return true;
	}

	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
		super.onBlockAdded(par1World, par2, par3, par4);
		setDispenserDefaultDirection(par1World, par2, par3, par4);
	}

	// BlockDispenser
	private void setDispenserDefaultDirection(World world, int i, int j, int k) {
		if (!world.isRemote) {
			Block block = world.getBlock(i, j, k - 1);
			Block block1 = world.getBlock(i, j, k + 1);
			Block block2 = world.getBlock(i - 1, j, k);
			Block block3 = world.getBlock(i + 1, j, k);
			byte b0 = 3;

			if (block.func_149730_j() && !block1.func_149730_j()) {
				b0 = 3;
			}

			if (block1.func_149730_j() && !block.func_149730_j()) {
				b0 = 2;
			}

			if (block2.func_149730_j() && !block3.func_149730_j()) {
				b0 = 5;
			}

			if (block3.func_149730_j() && !block2.func_149730_j()) {
				b0 = 4;
			}

			world.setBlockMetadataWithNotify(i, j, k, b0, 2);
		}
	}

	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
		int l = BlockPistonBase.determineOrientation(par1World, par2, par3, par4, par5EntityLivingBase);
		if (l <= 1)
			setDispenserDefaultDirection(par1World, par2, par3, par4);
		else
			par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
	}

	// public static int determineOrientation(World par0World, int par1, int
	// par2, int par3, EntityLivingBase par4EntityLiving) {
	// if ((MathHelper.abs((float) par4EntityLiving.posX - par1) < 2.0F) &&
	// (MathHelper.abs((float) par4EntityLiving.posZ - par3) < 2.0F)) {
	// double d0 = par4EntityLiving.posY + 1.82D - par4EntityLiving.yOffset;
	//
	// if (d0 - par2 > 2.0D) {
	// return 1;
	// }
	//
	// if (par2 - d0 > 0.0D) {
	// return 0;
	// }
	// }
	//
	// int l = MathHelper.floor_double(par4EntityLiving.rotationYaw * 4.0F /
	// 360.0F + 0.5D) & 0x3;
	// return l == 3 ? 4 : l == 2 ? 3 : l == 1 ? 5 : l == 0 ? 2 : 0;
	// }

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityKeyLocked();
	}

}