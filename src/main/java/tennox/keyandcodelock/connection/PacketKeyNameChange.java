package tennox.keyandcodelock.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import tennox.keyandcodelock.IKeyAndCodeLockPacket;
import tennox.keyandcodelock.KeyAndCodeLock;
import tennox.keyandcodelock.blocks.BlockDoorLockedKey;
import tennox.keyandcodelock.blocks.BlockRedstoneLockKey;
import tennox.keyandcodelock.blocks.TileEntityKeyLocked;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;

public class PacketKeyNameChange implements IKeyAndCodeLockPacket {

	int x, y, z;
	String name;
	int hash;

	public PacketKeyNameChange() {
	}

	public PacketKeyNameChange(TileEntity tile, String name, int hash) {
		x = tile.xCoord;
		y = tile.yCoord;
		z = tile.zCoord;
		this.name = name;
		this.hash = hash;
	}

	@Override
	public void readBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		int l = buffer.readShort();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < l; i++) {
			builder.append(buffer.readChar());
		}
		name = builder.toString();
		hash = buffer.readInt();
		KeyAndCodeLock.debug("Read PacketKeyNameChange: " + x + "," + y + "," + z + "_" + name + "_" + hash);
	}

	@Override
	public void writeBytes(ByteBuf buffer) {
		KeyAndCodeLock.debug("Writing PacketKeyNameChange... " + x + "," + y + "," + z + "_" + name + "_" + hash);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeShort(name.length());
		for (int i = 0; i < name.length(); i++) {
			buffer.writeChar(Character.valueOf(name.charAt(i)));
		}
		buffer.writeInt(hash);
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
		TileEntity t = player.worldObj.getTileEntity(x, y, z);
		if ((t != null) && ((t instanceof TileEntityKeyLocked))) {
			TileEntityKeyLocked tile = (TileEntityKeyLocked) t;

			if ((name == null) || (name.length() == 0) || (hash <= 0)) {
				KeyAndCodeLock.logger.warn("Recieved KeyNameChange with wrong data!");
				sendKeyNameAnswer(x, y, z, name, hash, false, player);
				return;
			}

			ItemStack itemstack = ((EntityPlayerMP) player).inventory.getCurrentItem();

			if ((itemstack != null) && (itemstack.getItem() == KeyAndCodeLock.key)) {
				if (tile.name != null || tile.hash > 0) {
					sendKeyNameAnswer(x, y, z, name, hash, false, player);

					player.worldObj.markBlockForUpdate(x, y, z);
					KeyAndCodeLock.logger.warn("Client tried setting a key to an already set door...");
				} else {
					if (itemstack.stackTagCompound == null) {
						itemstack.setTagCompound(new NBTTagCompound());
					}
					itemstack.stackTagCompound.setInteger("hash", hash);
					itemstack.stackTagCompound.setString("name", name);

					tile.name = name;
					tile.hash = hash;

					EntityPlayerMP player2 = (EntityPlayerMP) player;

					if (player2.worldObj.getBlock(x, y, z) == KeyAndCodeLock.codelockeddoor)
						((BlockDoorLockedKey) KeyAndCodeLock.keylockeddoor).flipDoor(player2.worldObj, x, y, z, player2);
					else if (player2.worldObj.getBlock(x, y, z) == KeyAndCodeLock.coderedstonelock) {
						((BlockRedstoneLockKey) KeyAndCodeLock.keyredstonelock).changePowerState(player2.worldObj, x, y, z);
					}

					player.playerNetServerHandler.sendPacket(tile.getDescriptionPacket());
					sendKeyNameAnswer(x, y, z, name, hash, true, player);
					KeyAndCodeLock.debug("KeyName updated! name=" + name);
				}
			} else {
				KeyAndCodeLock.logger.warn("Recieved KeyNameChange packet but Player has no key?!");
				sendKeyNameAnswer(x, y, z, name, hash, false, player);
				return;
			}
		}
	}

	private void sendKeyNameAnswer(int x, int y, int z, String name, int hash, boolean answer, EntityPlayer player) {
		KeyAndCodeLock.debug("Sending KeyNameAnswer... (" + answer + ")");

		KeyAndCodeLock.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		KeyAndCodeLock.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		KeyAndCodeLock.channels.get(Side.SERVER).writeOutbound(new PacketKeyNameAnswer(x, y, z, name, hash, answer));
		KeyAndCodeLock.debug("KeyNameAnswer sent!");
	}

	@Override
	public void executeClient() {
	}
}
