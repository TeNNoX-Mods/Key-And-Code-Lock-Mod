package tennox.keyandcodelock.connection;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class KeyAndCodeLockClientProxy extends KeyAndCodeLockCommonProxy implements ISimpleBlockRenderingHandler {
	public static int renderID;

	public void registerRenderInformation() {
		renderID = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}

	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
	}

	public boolean renderWorldBlock(IBlockAccess iblockaccess, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		if (modelId == renderID) {
			return renderer.renderBlockDoor(block, x, y, z);
		}
		return false;
	}

	public int getRenderId() {
		return renderID;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}
}