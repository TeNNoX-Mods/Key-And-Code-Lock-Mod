package tennox.keyandcodelock.blocks;

import tennox.keyandcodelock.KeyAndCodeLock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityKeyLocked extends TileEntity {
	public String name;
	public int hash;

	public Packet getDescriptionPacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);
		this.name = par1NBTTagCompound.getString("name");
		this.hash = par1NBTTagCompound.getInteger("hash");
		if ((this.name != null) && (this.name.length() == 0))
			this.name = null;
	}

	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);
		if ((this.name != null) && (this.name.length() > 0))
			par1NBTTagCompound.setString("name", this.name);
		if (this.hash > 0)
			par1NBTTagCompound.setInteger("hash", this.hash);
	}
}