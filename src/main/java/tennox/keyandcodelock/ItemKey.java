package tennox.keyandcodelock;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemKey extends Item {
	protected ItemKey() {
		super();

		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	public void registerIcons(IIconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon("keyandcodelock:key");
	}

	public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack) {
		return true;
	}

	public String getItemStackDisplayName(ItemStack item) {
		if (item.stackTagCompound == null)
			item.setTagCompound(new NBTTagCompound());
		String name = item.stackTagCompound.getString("name");

		if (name.length() > 0) {
			return name;
		}
		return super.getItemStackDisplayName(item);
	}

}