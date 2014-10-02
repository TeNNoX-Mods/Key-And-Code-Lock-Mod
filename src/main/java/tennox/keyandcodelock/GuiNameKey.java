package tennox.keyandcodelock;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import tennox.keyandcodelock.blocks.TileEntityKeyLocked;
import tennox.keyandcodelock.connection.PacketKeyNameChange;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;

public class GuiNameKey extends GuiScreen {
	World world;
	EntityPlayer player;
	TileEntityKeyLocked tile;
	String current = "";
	int maxlength = 24;
	boolean checking;
	ResourceLocation background = new ResourceLocation("keyandcodelock", "textures/gui/namekey.png");

	int counter = 0;

	public GuiNameKey(World w, TileEntityKeyLocked t, EntityPlayer p) {
		this.player = p;
		this.world = w;
		this.tile = t;
		Keyboard.enableRepeatEvents(true);

		ItemStack item = this.player.getCurrentEquippedItem();
		if (item.stackTagCompound == null)
			item.setTagCompound(new NBTTagCompound());
		String name = item.stackTagCompound.getString("name");
		this.current = name;
	}

	public void initGui() {
		this.buttonList.clear();
		int x = (this.width - 176) / 2 + 16;
		int y = (this.height - 103) / 2 + 55;
		this.buttonList.add(new GuiButton(0, x - 5, y, 60, 20, "Cancel"));
		this.buttonList.add(new GuiButton(1, x + 87, y, 60, 20, "Done"));
	}

	protected void actionPerformed(GuiButton button) {
		buttonPressed(button.id);
	}

	public void buttonPressed(int id) {
		if (id == 0) {
			this.mc.displayGuiScreen(null);
		} else if (id == 1) {
			if (this.checking)
				return;
			if (this.current.trim().length() > 0) {
				this.checking = true;
				sendKeyNamePacket(this.current.trim(), new Random().nextInt(2147483645) + 1);
			}
		}
	}

	private void sendKeyNamePacket(String name, int hash) {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		KeyAndCodeLock.channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		KeyAndCodeLock.channels.get(Side.CLIENT).writeOutbound(new PacketKeyNameChange(this.tile, name, hash));
	}

	public void onPacketAnswer(String name, int hash, boolean answer) {
		if (answer) {
			ItemStack item = this.player.getCurrentEquippedItem();
			if (item.stackTagCompound == null)
				item.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = item.stackTagCompound;

			nbt.setString("name", name);
			nbt.setInteger("hash", hash);

			this.tile.name = name;
			this.tile.hash = hash;
			player.addChatMessage(new ChatComponentText("This door is now locked with the key \"" + name + "\""));
		}

		this.mc.displayGuiScreen(null);
	}

	protected void keyTyped(char c, int i) {
		if (i == 1) {
			this.mc.displayGuiScreen(null);
		}

		if ((this.current.length() < this.maxlength) && ((Character.isLetter(c)) || (Character.isDigit(c)) || (Character.toString(c).equals(" ")))) {
			this.current += c;
		}

		if ((i == 14) && (this.current.length() > 0)) {
			this.current = this.current.substring(0, this.current.length() - 1);
		}

		if (i == 28)
			buttonPressed(1);
	}

	public void updateScreen() {
		this.counter += 1;
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public void drawScreen(int par1, int par2, float par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(this.background);
		int j = (this.width - 176) / 2;
		int k = (this.height - 103) / 2;
		drawTexturedModalRect(j, k, 0, 0, 176, 166);

		drawString(this.fontRendererObj, "Enter name:", j + 12, k + 11, 16777215);

		String s = (this.counter % 16 > 8) && (this.current.length() < this.maxlength) ? "_" : "";
		String s1 = this.counter % 9 > 2 ? ".." : this.counter % 9 > 5 ? "..." : ".";

		drawString(this.fontRendererObj, this.current + s, j + 17, k + 33, 65280);
		super.drawScreen(par1, par2, par3);
	}

	public boolean doesGuiPauseGame() {
		return false;
	}
}