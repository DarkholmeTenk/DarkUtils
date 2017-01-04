package io.darkcraft.darkutils.mod;

import io.darkcraft.darkcore.mod.config.ConfigHandler;
import io.darkcraft.darkcore.mod.config.ConfigHandlerFactory;
import io.darkcraft.darkcore.mod.handlers.CommandHandler;
import io.darkcraft.darkcore.mod.interfaces.IConfigHandlerMod;
import io.darkcraft.darkutils.mod.cc.CC;
import io.darkcraft.darkutils.mod.proxy.CommonProxy;
import io.darkcraft.darkutils.mod.spawning.SpawnCommand;
import io.darkcraft.darkutils.mod.spawning.SpawnEventHandler;
import io.darkcraft.darkutils.mod.teams.TeamSystem;
import io.darkcraft.darkutils.mod.unifier.Unifier;
import io.darkcraft.darkutils.mod.unifier.UnifierCommand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "darkutils", version = "0.1", acceptableRemoteVersions="*",  dependencies = "after:ComputerCraft")
public class DarkUtilsMod implements IConfigHandlerMod
{
	public static final String modName = "darkutils";
	public static final Unifier unifier = new Unifier();
	public static ConfigHandler configHandler;

	@SidedProxy(clientSide = "io.darkcraft.darkutils.mod.proxy.ClientProxy", serverSide = "io.darkcraft.darkutils.mod.proxy.CommonProxy")
	public static CommonProxy									proxy;

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		configHandler = ConfigHandlerFactory.getConfigHandler(this);
		TeamSystem.init();
		CC.init();
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		CommandHandler.registerCommand(new UnifierCommand());
		CommandHandler.registerCommand(new SpawnCommand());
		Unifier.refreshConfigs();
		unifier.getUnificationList();
		FMLCommonHandler.instance().bus().register(unifier);
		FMLCommonHandler.instance().bus().register(new SpawnEventHandler());
	}

	@EventHandler
	public void servStart(FMLServerStartingEvent event)
	{
		TeamSystem.serverStart();
		SpawnEventHandler.clear();
	}

	@Override
	public String getModID()
	{
		return modName;
	}
}
