package tennox.keyandcodelock.blocks;

import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;
import tennox.keyandcodelock.KeyAndCodeLock;
import tennox.keyandcodelock.connection.KeyAndCodeLockClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDoorLockedKey extends BlockContainer {
	IIcon texture_top;
	IIcon texture_bottom;

	public BlockDoorLockedKey(Material material) {
		super(material);

		float f = 0.5F;
		float f1 = 1.0F;
		setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		this.texture_top = register.registerIcon("keyandcodelock:keylocked_top");
		this.texture_bottom = register.registerIcon("keyandcodelock:keylocked_bottom");
		this.blockIcon = this.texture_bottom;
	}

	@Override
	public IIcon getIcon(IBlockAccess iblockaccess, int par2, int par3, int par4, int side) {
		int i = getFullMetadata(iblockaccess, par2, par3, par4);
		int dir = i % 8;
		boolean flipped = i >= 16;
		boolean open = isOpen(iblockaccess, par2, par3, par4);
		boolean code = (this instanceof BlockDoorLockedCode);
		IIcon icon = (i & 0x8) != 0 ? this.texture_top : this.texture_bottom;
		IIcon doorSteel = Blocks.iron_door.getIcon(iblockaccess, par2, par3, par4, side);

		if (side <= 1)
			return doorSteel;
		if (code) {
			if (((dir == 0) && (side == 5)) || ((dir == 1) && (side == 3)) || ((dir == 2) && (side == 4)) || ((dir == 3) && (side == 2)))
				return doorSteel;
			if ((flipped != open) && (((dir == 4) && (side == 2)) || ((dir == 5) && (side == 5)) || ((dir == 6) && (side == 3)) || ((dir == 7) && (side == 4))))
				return doorSteel;
			if (((dir == 4) && (side == 3)) || ((dir == 5) && (side == 4)) || ((dir == 6) && (side == 2)) || ((dir == 7) && (side == 5)))
				return flipped ? doorSteel : icon;
			if ((i & 0x8) != 0)
				icon = this.texture_top;
			else
				icon = this.texture_bottom;
			if ((flipped) && (open))
				icon = new IconFlipped(icon, true, false);
		} else if (((dir == 0) && (side == 5)) || ((dir == 1) && (side == 3)) || ((dir == 2) && (side == 4)) || ((dir == 3) && (side == 2))) {
			icon = new IconFlipped(icon, true, false);
		} else if ((open) && (((dir == 4) && (side == 2)) || ((dir == 5) && (side == 5)) || ((dir == 6) && (side == 3)) || ((dir == 7) && (side == 4)))) {
			icon = (!flipped) && (code) ? doorSteel : new IconFlipped(icon, true, false);
		} else if ((i & 0x8) != 0) {
			icon = this.texture_top;
		} else {
			icon = this.texture_bottom;
		}

		return (flipped) && (!open) ? new IconFlipped(icon, true, false) : icon;
	}

	public boolean isOpen(IBlockAccess iblockaccess, int i, int j, int k) {
		int x = getFullMetadata(iblockaccess, i, j, k);
		if ((x & 0x8) != 0)
			return isOpen(iblockaccess, i, j - 1, k);
		return iblockaccess.getBlockMetadata(i, j, k) > 3;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		int i = getFullMetadata(par1IBlockAccess, par2, par3, par4);
		return (i & 0x4) != 0;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return KeyAndCodeLockClientProxy.renderID;
	}

	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (player.isSneaking())
			return false;
		int x = getFullMetadata(world, i, j, k);
		if ((x & 0x8) != 0) {
			return onBlockActivated(world, i, j - 1, k, player, par6, par6, par8, par9);
		}
		TileEntityKeyLocked door = (TileEntityKeyLocked) world.getTileEntity(i, j, k);
		ItemStack item = player.getCurrentEquippedItem();

		if (((item == null) || (item.getItem() != KeyAndCodeLock.remover)) && (isOpen(world, i, j, k))) {
			flipDoor(world, i, j, k, player);
			return true;
		}

		if ((item == null) || ((item.getItem() != KeyAndCodeLock.key) && (item.getItem() != KeyAndCodeLock.remover))) {
			if (world.isRemote) {
				if ((door.name == null) || (door.hash == 0))
					player.addChatMessage(new ChatComponentText("This door is not paired with a key yet!"));
				else
					player.addChatMessage(new ChatComponentText("This door is locked. You need a key to open it!"));
			}
			return true;
		}
		if (item.getItem() == KeyAndCodeLock.key)
			handleKey(world, player, door, item, i, j, k);
		else if (item.getItem() == KeyAndCodeLock.remover) {
			handleRemover(world, player, i, j, k);
		}
		return true;
	}

	private void handleKey(World world, EntityPlayer player, TileEntityKeyLocked door, ItemStack item, int i, int j, int k) {
		if (item.stackTagCompound == null)
			item.setTagCompound(new NBTTagCompound());
		String name = item.stackTagCompound.getString("name");
		int hash = item.stackTagCompound.getInteger("hash");

		if ((item != null) && (item.getItem() == KeyAndCodeLock.key) && (!name.equals("")) && (hash > 0) && (door.name == null)) {
			door.name = name;
			door.hash = hash;
			if (world.isRemote)
				player.addChatMessage(new ChatComponentText("The door is now locked with \"" + name + "\""));
			flipDoor(world, i, j, k, player);
			return;
		}

		if (world.isRemote) {
			if (((name.equals("")) || (hash == 0)) && (door.name == null)) {
				player.openGui(KeyAndCodeLock.instance, KeyAndCodeLock.keynameGUI, world, i, j, k);
			}
		} else if ((!name.equals("")) || (hash > 0) || (door.name != null)) {
			if ((door.name.equals(name)) && (door.hash == hash)) {
				flipDoor(world, i, j, k, player);
			} else if (door.name.equals(name)) {
				player.addChatMessage(new ChatComponentText("This is the wrong key! The door is locked to another key with the name \"" + door.name + "\""));
			} else if (((name.equals("")) || (hash == 0)) && (isOpen(world, i, j, k))) {
				item.stackTagCompound.setString("name", door.name);
				item.stackTagCompound.setInteger("hash", door.hash);
				player.addChatMessage(new ChatComponentText("This is now a duplicate key of \"" + door.name + "\""));
			} else if ((name.equals("")) || (hash == 0)) {
				player.addChatMessage(new ChatComponentText("This door is already locked with \"" + door.name + "\""));
			} else {
				player.addChatMessage(new ChatComponentText("This is the wrong key! The door is locked with \"" + door.name + "\""));
			}
		}
	}

	private void handleRemover(World world, EntityPlayer player, int i, int j, int k) {
		if (isOpen(world, i, j, k)) {
			dropBlockAsItem(world, i, j, k, (ItemStack) getDrops(world, i, j, k, 0, 1).get(0));
			world.setBlockToAir(i, j, k);
		} else if (world.isRemote) {
			player.addChatMessage(new ChatComponentText("Please open the door to remove it!"));
		}
	}

	public void onBlockHarvested(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer) {
		if ((par6EntityPlayer.capabilities.isCreativeMode) && ((par5 & 0x8) != 0) && (par1World.getBlock(par2, par3 - 1, par4) == this))
			par1World.setBlockToAir(par2, par3 - 1, par4);
	}

	public void flipDoor(World world, int i, int j, int k, EntityPlayer player) {
		int i1 = this.getFullMetadata(world, i, j, k);
		int j1 = i1 & 7;
		j1 ^= 4;

		if ((i1 & 8) == 0) {
			world.setBlockMetadataWithNotify(i, j, k, j1, 2);
			world.markBlockRangeForRenderUpdate(i, j, k, i, j, k);
		} else {
			world.setBlockMetadataWithNotify(i, j - 1, k, j1, 2);
			world.markBlockRangeForRenderUpdate(i, j - 1, k, i, j, k);
		}

		world.playAuxSFXAtEntity(player, 1003, i, j, k, 0);
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, Block par5, int meta) {
		super.breakBlock(world, i, j, k, par5, meta);
		if (meta == 8)
			world.setBlockToAir(i, j - 1, k);
		else
			world.setBlockToAir(i, j + 1, k);
	}

	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		if ((par1 & 0x8) != 0) {
			return null;
		}
		return KeyAndCodeLock.itemkeylockeddoor;
	}

	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		setBlockBoundsBasedOnState(par1World, par2, par3, par4);
		return super.getSelectedBoundingBoxFromPool(par1World, par2, par3, par4);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		setBlockBoundsBasedOnState(par1World, par2, par3, par4);
		return super.getCollisionBoundingBoxFromPool(par1World, par2, par3, par4);
	}

	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		setDoorRotation(getFullMetadata(par1IBlockAccess, par2, par3, par4));
	}

	private void setDoorRotation(int par1) {
		float f = 0.1875F;
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
		int i = par1 & 0x3;
		boolean flag = (par1 & 0x4) != 0;
		boolean flag1 = (par1 & 0x10) != 0;

		if (i == 0) {
			if (!flag)
				setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
			else if (!flag1)
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
			else
				setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
		} else if (i == 1) {
			if (!flag)
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
			else if (!flag1)
				setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			else
				setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
		} else if (i == 2) {
			if (!flag)
				setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			else if (!flag1)
				setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
			else
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
		} else if (i == 3)
			if (!flag)
				setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
			else if (!flag1)
				setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
			else
				setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	public MovingObjectPosition collisionRayTrace(World par1World, int par2, int par3, int par4, Vec3 par5Vec3D, Vec3 par6Vec3D) {
		setBlockBoundsBasedOnState(par1World, par2, par3, par4);
		return super.collisionRayTrace(par1World, par2, par3, par4, par5Vec3D, par6Vec3D);
	}

	// BlockDoor
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
		if (par3 >= 255) {
			return false;
		}
		return super.canPlaceBlockAt(par1World, par2, par3, par4);
	}

	public int getMobilityFlag() {
		return 2;
	}

	public int getFullMetadata(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		int l = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
		boolean flag = (l & 8) != 0;
		int i1;
		int j1;

		if (flag) {
			i1 = par1IBlockAccess.getBlockMetadata(par2, par3 - 1, par4);
			j1 = l;
		} else {
			i1 = l;
			j1 = par1IBlockAccess.getBlockMetadata(par2, par3 + 1, par4);
		}

		boolean flag1 = (j1 & 1) != 0;
		return i1 & 7 | (flag ? 8 : 0) | (flag1 ? 16 : 0);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityKeyLocked();
	}
}