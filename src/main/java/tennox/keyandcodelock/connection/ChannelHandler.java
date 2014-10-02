package tennox.keyandcodelock.connection;

import tennox.keyandcodelock.IKeyAndCodeLockPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.NetworkRegistry;

public class ChannelHandler extends FMLIndexedMessageToMessageCodec<IKeyAndCodeLockPacket> {
	public ChannelHandler() {
		addDiscriminator(0, PacketCodeCheck.class);
		addDiscriminator(1, PacketCodeCheckAnswer.class);
		addDiscriminator(2, PacketCodeChange.class);
		addDiscriminator(3, PacketKeyNameChange.class);
		addDiscriminator(4, PacketKeyNameAnswer.class);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, IKeyAndCodeLockPacket packet, ByteBuf data) throws Exception {
		packet.writeBytes(data);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, IKeyAndCodeLockPacket packet) {
		packet.readBytes(data);
		switch (FMLCommonHandler.instance().getEffectiveSide()) {
		case CLIENT:
			packet.executeClient();
			break;
		case SERVER:
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			packet.executeServer(((NetHandlerPlayServer) netHandler).playerEntity);
			break;
		}
	}
}