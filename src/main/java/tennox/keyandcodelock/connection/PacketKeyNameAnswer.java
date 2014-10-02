package tennox.keyandcodelock.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import tennox.keyandcodelock.GuiNameKey;
import tennox.keyandcodelock.IKeyAndCodeLockPacket;
import tennox.keyandcodelock.KeyAndCodeLock;

public class PacketKeyNameAnswer implements IKeyAndCodeLockPacket {

	int x, y, z;
	String name;
	int hash;
	boolean answer;

	public PacketKeyNameAnswer() {
	}

	public PacketKeyNameAnswer(int x, int y, int z, String name, int hash, boolean answer) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.name = name;
		this.hash = hash;
		this.answer = answer;
	}

	@Override
	public void writeBytes(ByteBuf buffer) {
		KeyAndCodeLock.debug("Writing PacketKeyNameAnswer... " + x + "," + y + "," + z + "_" + name + "_" + hash + "_" + answer);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeShort(name.length());
		for (int i = 0; i < name.length(); i++) {
			buffer.writeChar(Character.valueOf(name.charAt(i)));
		}
		buffer.writeInt(hash);
		buffer.writeBoolean(answer);
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
		answer = buffer.readBoolean();

		KeyAndCodeLock.debug("Read PacketKeyNameAnswer: " + x + "," + y + "," + z + "_" + name + "_" + hash + "_" + answer);
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
	}

	@Override
	public void executeClient() {
		if ((name == null) || (name.length() == 0) || (hash <= 0)) {
			KeyAndCodeLock.logger.warn("Recieved KeyNameAnswer with wrong data!");
			return;
		}
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;

		if ((gui != null) && ((gui instanceof GuiNameKey))) {
			GuiNameKey guikey = (GuiNameKey) gui;
			guikey.onPacketAnswer(name, hash, answer);
		} else {
			KeyAndCodeLock.logger.warn("KeyNameChangeAnswer received but no gui?!");
		}

		if (answer)
			KeyAndCodeLock.debug("KeyNameChange was accepted by the Server!");
		else
			KeyAndCodeLock.debug("KeyNameChange was denied by the Server!");
	}
}
