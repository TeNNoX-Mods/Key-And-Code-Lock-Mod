package tennox.keyandcodelock;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemRemover extends Item {
	protected ItemRemover() {
		super();
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	public void registerIcons(IIconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon("keyandcodelock:remover");
	}
}