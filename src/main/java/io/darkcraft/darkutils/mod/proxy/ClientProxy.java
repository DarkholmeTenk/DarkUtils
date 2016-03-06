package io.darkcraft.darkutils.mod.proxy;

import io.darkcraft.darkutils.mod.cc.CCProjector;
import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy
{
	public void init()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(CCProjector.CCProjectorTE.class, CCProjector.renderer());
	}
}
