package tennox.keyandcodelock.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import tennox.keyandcodelock.IKeyAndCodeLockPacket;
import tennox.keyandcodelock.KeyAndCodeLock;
import tennox.keyandcodelock.blocks.BlockDoorLockedKey;
import tennox.keyandcodelock.blocks.BlockRedstoneLockKey;
import tennox.keyandcodelock.blocks.TileEntityCodeLocked;

public class PacketCodeChange implements IKeyAndCodeLockPacket {

	int x, y, z;
	int oldcode, newcode;

	public PacketCodeChange() {
	}

	public PacketCodeChange(TileEntity tile, int oldcode, int newcode) {
		x = tile.xCoord;
		y = tile.yCoord;
		z = tile.zCoord;
		this.oldcode = oldcode;
		this.newcode = newcode;
	}

	@Override
	public void readBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		oldcode = buffer.readInt();
		newcode = buffer.readInt();
		KeyAndCodeLock.debug("Read PacketCodeChange: " + x + "," + y + "," + z + "_" + oldcode + "_" + newcode);
	}

	@Override
	public void writeBytes(ByteBuf buffer) {
		KeyAndCodeLock.debug("Writing PacketCodeChange... " + x + "," + y + "," + z + "_" + oldcode + "_" + newcode);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeInt(oldcode);
		buffer.writeInt(newcode);
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
		TileEntity t = player.worldObj.getTileEntity(x, y, z);
		if ((t != null) && ((t instanceof TileEntityCodeLocked))) {
			TileEntityCodeLocked tile = (TileEntityCodeLocked) t;

			if ((tile.code != oldcode) && (tile.code >= 0)) {
				KeyAndCodeLock.debug("No CodeChange! \"" + oldcode + "\" is not the old code!");
				return;
			}
			tile.code = newcode;
			tile.isSet = true;

			if (player.worldObj.getBlock(x, y, z) == KeyAndCodeLock.codelockeddoor)
				((BlockDoorLockedKey) KeyAndCodeLock.keylockeddoor).flipDoor(player.worldObj, x, y, z, player);
			else if (player.worldObj.getBlock(x, y, z) == KeyAndCodeLock.coderedstonelock) {
				((BlockRedstoneLockKey) KeyAndCodeLock.keyredstonelock).changePowerState(player.worldObj, x, y, z);
			}
			player.playerNetServerHandler.sendPacket(tile.getDescriptionPacket());
			KeyAndCodeLock.debug("CodeChange accepted!");
		}
	}

	@Override
	public void executeClient() {
	}
}
