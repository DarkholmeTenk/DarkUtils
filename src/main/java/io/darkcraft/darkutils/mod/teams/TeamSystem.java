package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.config.ConfigFile;
import io.darkcraft.darkcore.mod.handlers.CommandHandler;
import io.darkcraft.darkutils.mod.DarkUtilsMod;
import io.darkcraft.darkutils.mod.teams.commands.TeamCommand;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;

public class TeamSystem
{
	public static ConfigFile teamConfig = DarkUtilsMod.configHandler.registerConfigNeeder("teamSystem");

	public static boolean teamSystemEnabled = false;
	public static void refreshConfigs()
	{
		teamSystemEnabled = teamConfig.getBoolean("enabled", false, "If true, this enables the team system");
	}

	private static TeamSystemStore teamSystemStore;
	public static void init()
	{
		refreshConfigs();
		if(teamSystemEnabled) CommandHandler.registerCommand(new TeamCommand());
	}

	public static void serverStart()
	{
		if(teamSystemStore != null)
		{
			FMLCommonHandler.instance().bus().unregister(teamSystemStore);
			MinecraftForge.EVENT_BUS.unregister(teamSystemStore);
			teamSystemStore = null;
			Region.clear();
		}
		getTeamStore();
	}

	public static TeamSystemStore getTeamStore()
	{
		if(!teamSystemEnabled) return null;
		if(teamSystemStore == null)
		{
			teamSystemStore = new TeamSystemStore();
			teamSystemStore.load();
			teamSystemStore.save();
			FMLCommonHandler.instance().bus().register(teamSystemStore);
			MinecraftForge.EVENT_BUS.register(teamSystemStore);
		}
		return teamSystemStore;
	}
}
