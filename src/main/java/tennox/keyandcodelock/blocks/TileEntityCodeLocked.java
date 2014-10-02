package tennox.keyandcodelock.blocks;

import tennox.keyandcodelock.KeyAndCodeLock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityCodeLocked extends TileEntity {
	public int code = -1;

	public boolean isSet;

	public Packet getDescriptionPacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		nbttagcompound.removeTag("code");
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		NBTTagCompound nbt = pkt.func_148857_g();
		readFromNBT(nbt);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			code = nbt.getInteger("code");
		isSet = nbt.getBoolean("isSet");
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && code > 0)
			isSet = true;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if ((FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER))
			nbt.setInteger("code", code);
		nbt.setBoolean("isSet", isSet);
	}

}