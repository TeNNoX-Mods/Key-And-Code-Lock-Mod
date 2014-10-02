package tennox.keyandcodelock.blocks;

import tennox.keyandcodelock.KeyAndCodeLock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BlockRedstoneLockCode extends BlockRedstoneLockKey {
	public BlockRedstoneLockCode(Material material) {
		super(material);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.texture_off = par1IconRegister.registerIcon("keyandcodelock:coderedstonelock");
		this.texture_on = par1IconRegister.registerIcon("keyandcodelock:coderedstonelock_on");
		this.blockIcon = par1IconRegister.registerIcon("keyandcodelock:redstonelock");
	}

	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityCodeLocked();
	}

	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (player.isSneaking()) {
			return false;
		}

		if (world.isRemote) {
			if ((player.getCurrentEquippedItem() != null) && (player.getCurrentEquippedItem().getItem() == KeyAndCodeLock.remover)) {
				return true;
			}

			player.openGui(KeyAndCodeLock.instance, KeyAndCodeLock.codelockGUI, world, i, j, k);
			return true;
		}

		if ((player.getCurrentEquippedItem() != null) && (player.getCurrentEquippedItem().getItem() == KeyAndCodeLock.remover)) {
			if (isPowering(world, i, j, k)) {
				dropBlockAsItem(world, i, j, k, (ItemStack) getDrops(world, i, j, k, 0, 1).get(0));
				world.setBlockToAir(i, j, k);
			} else {
				player.addChatMessage(new ChatComponentText("Please open the lock to remove it!"));
			}
			return true;
		}

		return true;
	}
}