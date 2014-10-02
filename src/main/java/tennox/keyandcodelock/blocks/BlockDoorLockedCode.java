package tennox.keyandcodelock.blocks;

import java.util.Random;

import tennox.keyandcodelock.KeyAndCodeLock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockDoorLockedCode extends BlockDoorLockedKey {
	int tex2;

	public BlockDoorLockedCode(Material material) {
		super(material);
	}

	public void registerBlockIcons(IIconRegister register) {
		this.texture_top = register.registerIcon("keyandcodelock:codelocked_top");
		this.texture_bottom = register.registerIcon("keyandcodelock:codelocked_bottom");
		this.blockIcon = this.texture_bottom;
	}

	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (player.isSneaking())
			return false;
		int x = getFullMetadata(world, i, j, k);
		if ((x & 0x8) != 0) {
			return onBlockActivated(world, i, j - 1, k, player, par6, par6, par8, par9);
		}
		world.getTileEntity(i, j, k);

		if (world.isRemote) {
			if ((player.getCurrentEquippedItem() != null) && (player.getCurrentEquippedItem().getItem() == KeyAndCodeLock.remover))
				return true;
			if (isOpen(world, i, j, k)) {
				return true;
			}
			player.openGui(KeyAndCodeLock.instance, KeyAndCodeLock.codelockGUI, world, i, j, k);
			return true;
		}

		if ((player.getCurrentEquippedItem() != null) && (player.getCurrentEquippedItem().getItem() == KeyAndCodeLock.remover)) {
			if (isOpen(world, i, j, k)) {
				dropBlockAsItem(world, i, j, k, (ItemStack) getDrops(world, i, j, k, 0, 1).get(0));
				world.setBlockToAir(i, j, k);
			} else {
				player.addChatComponentMessage(new ChatComponentText("Please open the door to remove it!"));
			}
			return true;
		}

		if (isOpen(world, i, j, k)) {
			flipDoor(world, i, j, k, player);
			return true;
		}

		return true;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return new ItemStack(Block.getBlockFromItem(KeyAndCodeLock.itemcodelockeddoor), 1);
	}

	public Item getItemDropped(int meta, Random par2Random, int par3) {
		if ((meta & 0x8) != 0) {
			return null;
		}
		return KeyAndCodeLock.itemcodelockeddoor;
	}

	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityCodeLocked();
	}
}