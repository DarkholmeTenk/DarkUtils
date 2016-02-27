package io.darkcraft.darkutils.mod;

import io.darkcraft.darkcore.mod.config.ConfigHandler;
import io.darkcraft.darkcore.mod.config.ConfigHandlerFactory;
import io.darkcraft.darkcore.mod.handlers.CommandHandler;
import io.darkcraft.darkcore.mod.interfaces.IConfigHandlerMod;
import io.darkcraft.darkutils.mod.commands.UnifierCommand;
import io.darkcraft.darkutils.mod.handlers.Unifier;

import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "darkutils", version = "0.1", acceptableRemoteVersions="*")
public class DarkUtilsMod implements IConfigHandlerMod
{
	public static final String modName = "darkutils";
	public static final Unifier unifier = new Unifier();
	public static ConfigHandler configHandler;

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		configHandler = ConfigHandlerFactory.getConfigHandler(this);
		CommandHandler.registerCommand(new UnifierCommand());
		Unifier.refreshConfigs();
		unifier.getUnificationList();
		FMLCommonHandler.instance().bus().register(unifier);
	}

	@Override
	public String getModID()
	{
		return modName;
	}

	@NetworkCheckHandler
	public boolean check(Map<String,String> a, Side b)
	{
		return true;
	}

}
