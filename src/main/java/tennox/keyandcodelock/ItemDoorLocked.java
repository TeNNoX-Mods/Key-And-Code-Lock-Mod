package tennox.keyandcodelock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemDoorLocked extends Item {
	boolean code;

	public ItemDoorLocked(Material material, boolean c) {
		super();
		setCreativeTab(CreativeTabs.tabRedstone);

		this.maxStackSize = 1;
		this.code = c;
	}

	public void registerIcons(IIconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon("keyandcodelock:" + (this.code ? "codelocked_item" : "keylocked_item"));
	}

	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (par7 != 1) {
			return false;
		}

		par5++;

		if ((!par2EntityPlayer.canPlayerEdit(par4, par5, par6, par7, par1ItemStack)) || (!par2EntityPlayer.canPlayerEdit(par4, par5 + 1, par6, par7, par1ItemStack))) {
			return false;
		}

		if (!KeyAndCodeLock.keylockeddoor.canPlaceBlockAt(par3World, par4, par5, par6)) {
			return false;
		}
		int i = MathHelper.floor_double((par2EntityPlayer.rotationYaw + 180.0F) * 4.0F / 360.0F - 0.5D) & 0x3;
		placeDoorBlock(par3World, par4, par5, par6, i, this.code ? KeyAndCodeLock.codelockeddoor : KeyAndCodeLock.keylockeddoor);
		par1ItemStack.stackSize -= 1;
		return true;
	}

	// ItemDoor
	public static void placeDoorBlock(World world, int i, int j, int k, int dir, Block block) {
		byte b0 = 0;
		byte b1 = 0;

		if (dir == 0)
			b1 = 1;
		if (dir == 1)
			b0 = -1;
		if (dir == 2)
			b1 = -1;
		if (dir == 3)
			b0 = 1;

		int i1 = (world.getBlock(i - b0, j, k - b1).isNormalCube() ? 1 : 0) + (world.getBlock(i - b0, j + 1, k - b1).isNormalCube() ? 1 : 0);
		int j1 = (world.getBlock(i + b0, j, k + b1).isNormalCube() ? 1 : 0) + (world.getBlock(i + b0, j + 1, k + b1).isNormalCube() ? 1 : 0);
		boolean flag = world.getBlock(i - b0, j, k - b1) == block || world.getBlock(i - b0, j + 1, k - b1) == block;
		boolean flag1 = world.getBlock(i + b0, j, k + b1) == block || world.getBlock(i + b0, j + 1, k + b1) == block;
		boolean flag2 = false;

		if (flag && !flag1) {
			flag2 = true;
		} else if (j1 > i1) {
			flag2 = true;
		}

		world.setBlock(i, j, k, block, dir, 3);
		world.setBlock(i, j + 1, k, block, 8 | (flag2 ? 1 : 0), 3);
		world.notifyBlocksOfNeighborChange(i, j, k, block);
		world.notifyBlocksOfNeighborChange(i, j + 1, k, block);
	}
}