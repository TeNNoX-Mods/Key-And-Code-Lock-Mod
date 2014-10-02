package tennox.keyandcodelock;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import tennox.keyandcodelock.blocks.BlockDoorLockedCode;
import tennox.keyandcodelock.blocks.BlockDoorLockedKey;
import tennox.keyandcodelock.blocks.BlockRedstoneLockCode;
import tennox.keyandcodelock.blocks.BlockRedstoneLockKey;
import tennox.keyandcodelock.blocks.TileEntityCodeLocked;
import tennox.keyandcodelock.blocks.TileEntityKeyLocked;
import tennox.keyandcodelock.connection.ChannelHandler;
import tennox.keyandcodelock.connection.KeyAndCodeLockCommonProxy;
import tennox.keyandcodelock.connection.KeyAndCodeLockGuiHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "TeNNoX_KeyAndCodeLock", name = "Key and Code Lock", version = "1.4")
// remember to change debug method!
public class KeyAndCodeLock {

	@SidedProxy(clientSide = "tennox.keyandcodelock.connection.KeyAndCodeLockClientProxy", serverSide = "tennox.keyandcodelock.connection.KeyAndCodeLockCommonProxy")
	public static KeyAndCodeLockCommonProxy proxy;

	public static EnumMap<Side, FMLEmbeddedChannel> channels;

	@Mod.Instance("TeNNoX_KeyAndCodeLock")
	public static KeyAndCodeLock instance;
	public static Logger logger;

	public static int keynameGUI = 1;
	public static int codelockGUI = 2;
	public static Block keylockeddoor;
	public static Block codelockeddoor;
	public static Block keyredstonelock;
	public static Block coderedstonelock;
	public static Item itemkeylockeddoor;
	public static Item itemcodelockeddoor;
	public static Item key;
	public static Item remover;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		logger = e.getModLog();
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		proxy.registerRenderInformation();

		// GUI //
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new KeyAndCodeLockGuiHandler());

		// Network channels //
		channels = NetworkRegistry.INSTANCE.newChannel("KeyAndCodeLock", new ChannelHandler());

		keylockeddoor = new BlockDoorLockedKey(Material.iron).setBlockName("keylockeddoor").setBlockUnbreakable().setResistance(6000000.0F).setStepSound(Block.soundTypeMetal);
		codelockeddoor = new BlockDoorLockedCode(Material.iron).setBlockName("codelockeddoor").setBlockUnbreakable().setResistance(6000000.0F).setStepSound(Block.soundTypeMetal);
		keyredstonelock = new BlockRedstoneLockKey(Material.rock).setBlockName("keyredstonelock").setBlockUnbreakable().setResistance(6000000.0F)
				.setStepSound(Block.soundTypeMetal);
		coderedstonelock = new BlockRedstoneLockCode(Material.rock).setBlockName("coderedstonelock").setBlockUnbreakable().setResistance(6000000.0F)
				.setStepSound(Block.soundTypeMetal);

		itemkeylockeddoor = new ItemDoorLocked(Material.iron, false).setUnlocalizedName("itemkeylockeddoor");
		itemcodelockeddoor = new ItemDoorLocked(Material.iron, true).setUnlocalizedName("itemcodelockeddoor");
		key = new ItemKey().setUnlocalizedName("key");
		remover = new ItemRemover().setUnlocalizedName("remover");

		GameRegistry.registerBlock(keylockeddoor, "keylockeddoor");
		GameRegistry.registerBlock(codelockeddoor, "codelockeddoor");
		GameRegistry.registerBlock(keyredstonelock, "keyredstonelock");
		GameRegistry.registerBlock(coderedstonelock, "coderedstonelock");

		GameRegistry.registerItem(itemkeylockeddoor, "itemkeylockeddoor");
		GameRegistry.registerItem(itemcodelockeddoor, "itemcodelockeddoor");
		GameRegistry.registerItem(key, "key");
		GameRegistry.registerItem(remover, "remover");

		GameRegistry.registerTileEntity(TileEntityKeyLocked.class, "KeyLockedDoor");
		GameRegistry.registerTileEntity(TileEntityCodeLocked.class, "CodeLockedDoor");
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

		GameRegistry.addRecipe(new ItemStack(itemkeylockeddoor, 1), new Object[] { "#*", "#*", "#*", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('*'),
				Blocks.obsidian });
		GameRegistry.addRecipe(new ItemStack(itemcodelockeddoor, 1), new Object[] { "#*", "#+", "#*", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('*'),
				Blocks.obsidian, Character.valueOf('+'), Items.redstone });
		GameRegistry.addRecipe(new ItemStack(keyredstonelock, 1), new Object[] { "###", "+##", "###", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('+'),
				Items.redstone });
		GameRegistry.addRecipe(new ItemStack(coderedstonelock, 1), new Object[] { "###", "+#+", "###", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('+'),
				Items.redstone });
		GameRegistry.addRecipe(new ItemStack(key, 1), new Object[] { "###", "* #", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('*'), Items.redstone });
		GameRegistry.addRecipe(new ItemStack(key, 1), new Object[] { "###", "# *", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('*'), Items.redstone });
		GameRegistry.addRecipe(new ItemStack(remover, 1), new Object[] { " + ", "#*#", "###", Character.valueOf('#'), Items.iron_ingot, Character.valueOf('*'), Items.redstone,
				Character.valueOf('+'), Items.flint_and_steel });

		GameRegistry.addShapelessRecipe(new ItemStack(key, 1), new Object[] { key, key });
	}

	public static void debug(String s) {
		// logger.debug(s);
	}

	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event) {
		if (!(event.crafting.getItem() instanceof ItemKey)) {
			return;
		}

		String name = null;
		int hash = 0;
		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
			ItemStack item = event.craftMatrix.getStackInSlot(i);
			if (item != null) {
				if (item.stackTagCompound == null)
					item.setTagCompound(new NBTTagCompound());
				String name2 = item.stackTagCompound.getString("name");
				int hash2 = item.stackTagCompound.getInteger("hash");

				if ((!name2.equals("")) && (hash2 > 0)) {
					name = name2;
					hash = hash2;
					if (!event.player.inventory.addItemStackToInventory(item))
						event.player.entityDropItem(item, 0f);
					event.craftMatrix.setInventorySlotContents(i, null);
					break;
				}
			}
		}
		if ((name == null) || (hash == 0)) {
			if ((event.player instanceof EntityPlayerMP))
				event.player.addChatComponentMessage(new ChatComponentText("No valid key to copy data from!"));
			for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
				ItemStack item = event.craftMatrix.getStackInSlot(i);
				if (item != null) {
					if (!event.player.inventory.addItemStackToInventory(item))
						event.player.entityDropItem(item, 0f);
					event.craftMatrix.setInventorySlotContents(i, null);
					break;
				}
			}
			return;
		}

		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
			ItemStack item = event.craftMatrix.getStackInSlot(i);
			if (item != null) {
				if (item.stackTagCompound == null)
					item.setTagCompound(new NBTTagCompound());
				String name2 = item.stackTagCompound.getString("name");
				int hash2 = item.stackTagCompound.getInteger("hash");

				if ((name2.equals("")) && (hash2 == 0)) {
					if (event.crafting.stackTagCompound == null)
						event.crafting.setTagCompound(new NBTTagCompound());
					event.crafting.stackTagCompound.setString("name", name);
					event.crafting.stackTagCompound.setInteger("hash", hash);
					if ((event.player instanceof EntityPlayerMP))
						event.player.addChatComponentMessage(new ChatComponentText("Key \"" + name + "\" copied!"));
					event.craftMatrix.setInventorySlotContents(i, null);
					return;
				}
			}
		}
		if ((event.player instanceof EntityPlayerMP)) {
			event.player.addChatComponentMessage(new ChatComponentText("You need a fresh key to copy the data!"));
		}
		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
			ItemStack item = event.craftMatrix.getStackInSlot(i);
			if (item != null) {
				if (item.stackTagCompound == null)
					item.setTagCompound(new NBTTagCompound());
				String name2 = item.stackTagCompound.getString("name");
				int hash2 = item.stackTagCompound.getInteger("hash");

				if ((!name2.equals("")) && (hash2 > 0)) {
					if (event.crafting.stackTagCompound == null)
						event.crafting.setTagCompound(new NBTTagCompound());
					event.crafting.stackTagCompound.setString("name", name2);
					event.crafting.stackTagCompound.setInteger("hash", hash2);
					event.craftMatrix.setInventorySlotContents(i, null);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onMapMissing(FMLMissingMappingsEvent event) {
		List<MissingMapping> list = event.get();

		for (MissingMapping m : list) {
			logger.info("missing mapping: " + m.name);
			if (m.name.toLowerCase().equals("tennox_keyandcodelock:key-locked door")) {
				m.remap(itemkeylockeddoor);
			} else if (m.name.toLowerCase().equals("tennox_keyandcodelock:code-locked door")) {
				m.remap(itemcodelockeddoor);
			} else if (m.name.toLowerCase().equals("tennox_keyandcodelock:key")) {
				m.remap(key);
			} else if (m.name.toLowerCase().equals("tennox_keyandcodelock:locked door remover")) {
				m.remap(remover);
			} else
				System.out.println("STILL MISSING: " + m.name);
		}
	}
}