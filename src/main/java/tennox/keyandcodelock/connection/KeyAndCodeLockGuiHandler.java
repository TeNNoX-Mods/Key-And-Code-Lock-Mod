package tennox.keyandcodelock.connection;

import tennox.keyandcodelock.GuiCodeLock;
import tennox.keyandcodelock.GuiNameKey;
import tennox.keyandcodelock.KeyAndCodeLock;
import tennox.keyandcodelock.blocks.TileEntityKeyLocked;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class KeyAndCodeLockGuiHandler implements IGuiHandler {
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int i, int j, int k) {
		boolean door = (world.getBlock(i, j, k) == KeyAndCodeLock.codelockeddoor) || (world.getBlock(i, j, k) == KeyAndCodeLock.keylockeddoor);
		if (ID == KeyAndCodeLock.codelockGUI)
			return new GuiCodeLock(world, i, j - ((world.getBlockMetadata(i, j, k) >= 8) && (door) ? 1 : 0), k, player);
		if (ID == KeyAndCodeLock.keynameGUI)
			return new GuiNameKey(world, (TileEntityKeyLocked) world.getTileEntity(i, j, k), player);
		return null;
	}
}