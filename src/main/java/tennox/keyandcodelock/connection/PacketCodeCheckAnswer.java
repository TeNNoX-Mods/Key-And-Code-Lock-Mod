package tennox.keyandcodelock.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import tennox.keyandcodelock.GuiCodeLock;
import tennox.keyandcodelock.IKeyAndCodeLockPacket;
import tennox.keyandcodelock.KeyAndCodeLock;

public class PacketCodeCheckAnswer implements IKeyAndCodeLockPacket {

	int x, y, z;
	boolean answer;

	public PacketCodeCheckAnswer() {
	}

	public PacketCodeCheckAnswer(int x, int y, int z, boolean answer) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.answer = answer;
	}

	@Override
	public void readBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		answer = buffer.readBoolean();
		KeyAndCodeLock.debug("Read PacketCodeCheckAnswer: " + x + "," + y + "," + z + "_" + answer);
	}

	@Override
	public void writeBytes(ByteBuf buffer) {
		KeyAndCodeLock.debug("Writing PacketCodeCheckAnswer... " + x + "," + y + "," + z + "_" + answer);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeBoolean(answer);
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
	}

	@Override
	public void executeClient() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;

		if ((gui != null) && ((gui instanceof GuiCodeLock))) {
			GuiCodeLock guicode = (GuiCodeLock) gui;
			guicode.handleCodeCheckAnswer(answer);
		} else {
			KeyAndCodeLock.logger.warn("CodeCheckAnswer received but no gui open?!");
		}
		if (answer) {
			KeyAndCodeLock.debug("Code was accepted by the Server!");
		} else
			KeyAndCodeLock.debug("Code was denied by the Server!");
	}
}
