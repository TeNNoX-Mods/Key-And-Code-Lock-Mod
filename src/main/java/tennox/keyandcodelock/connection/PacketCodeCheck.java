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
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;

public class PacketCodeCheck implements IKeyAndCodeLockPacket {

	int x, y, z;
	int code;
	boolean open; // should I open the lock or just check?

	public PacketCodeCheck() {
	}

	public PacketCodeCheck(TileEntity tile, int code, boolean open) {
		x = tile.xCoord;
		y = tile.yCoord;
		z = tile.zCoord;
		this.code = code;
		this.open = open;
	}

	@Override
	public void readBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		code = buffer.readInt();
		open = buffer.readBoolean();
		KeyAndCodeLock.debug("Read PacketCodeCheck: " + x + "," + y + "," + z + " code=" + code + " open=" + open);
	}

	@Override
	public void writeBytes(ByteBuf buffer) {
		KeyAndCodeLock.debug("Writing PacketCodeCheck... " + x + "," + y + "," + z + " code=" + code + " open=" + open);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeInt(code);
		buffer.writeBoolean(open);
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
		TileEntity t = player.worldObj.getTileEntity(x, y, z);
		if ((t != null) && ((t instanceof TileEntityCodeLocked))) {
			TileEntityCodeLocked tile = (TileEntityCodeLocked) t;
			if (tile.code == code) {
				sendCodeCheckAnswer(x, y, z, true, player);

				if (open) {
					if (player.worldObj.getBlock(x, y, z) == KeyAndCodeLock.codelockeddoor)
						((BlockDoorLockedKey) KeyAndCodeLock.keylockeddoor).flipDoor(player.worldObj, x, y, z, player);
					else if (player.worldObj.getBlock(x, y, z) == KeyAndCodeLock.coderedstonelock) {
						((BlockRedstoneLockKey) KeyAndCodeLock.keyredstonelock).changePowerState(player.worldObj, x, y, z);
					}
				}
				player.playerNetServerHandler.sendPacket(tile.getDescriptionPacket());
				KeyAndCodeLock.debug("Code accepted!");
			} else {
				sendCodeCheckAnswer(x, y, z, false, player);
				KeyAndCodeLock.debug("Code denied!");
			}
		}
	}

	private void sendCodeCheckAnswer(int x, int y, int z, boolean answer, EntityPlayer player) {
		KeyAndCodeLock.debug("Sending CodeCheckAnswer...");

		KeyAndCodeLock.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		KeyAndCodeLock.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		KeyAndCodeLock.channels.get(Side.SERVER).writeOutbound(new PacketCodeCheckAnswer(x, y, z, answer));
		KeyAndCodeLock.debug("CodeCheckAnswer sent!");
	}

	@Override
	public void executeClient() {
	}
}
