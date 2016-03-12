package io.darkcraft.darkutils.mod.teams.commands;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommandNew;
import io.darkcraft.darkcore.mod.datastore.SimpleCoordStore;
import io.darkcraft.darkutils.mod.teams.TeamData;
import io.darkcraft.darkutils.mod.teams.TeamSystem;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class TeamCommand extends AbstractCommandNew
{
	public TeamCommand()
	{
		super(new RegionCommand(), new ZoneCommand(), new HomeSC(), new SetScoreSC());
	}

	@Override
	public String getCommandName()
	{
		return "dcteam";
	}

	@Override
	public void getAliases(List<String> list)
	{
		list.add("dcut");
		list.add("dct");
		list.add("dcuteam");
	}

	@Override
	public boolean process(ICommandSender sen, List<String> strList)
	{
		return false;
	}

	private static class HomeSC extends AbstractCommandNew
	{
		@Override
		public String getCommandName(){ return "home";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public void getCommandUsage(ICommandSender s, String tc)
		{
			sendString(s,tc + " [teamName] [get/set/clear]");
		}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(!(sen instanceof EntityPlayer))
			{
				sendString(sen,"Players only");
				return false;
			}
			if(strList.size() != 2) return false;
			String tn = strList.get(0);
			String c = strList.get(1);
			if(!TeamSystem.getTeamStore().getTeams().contains(tn))
			{
				sendString(sen,"Team not found, try creating it or assigning somebody to it");
				return false;
			}
			TeamData td = TeamSystem.getTeamStore().getTeamData(tn);
			if(c.equals("get"))
			{
				SimpleCoordStore scs = td.getHome();
				sendString(sen,"Home = " + scs.toSimpleString());
			}
			else if(c.equals("set"))
			{
				SimpleCoordStore scs = new SimpleCoordStore((EntityPlayer)sen);
				td.setHome(scs);
				sendString(sen,"Home set to " + scs.toString());
			}
			else if(c.equals("clear"))
			{
				sendString(sen,"Home cleared");
				td.setHome(null);
			}
			else
			{
				sendString(sen,"Unrecognised command - " + c);
				return false;
			}
			return true;
		}
	}

	private static class SetScoreSC extends AbstractCommandNew
	{
		@Override
		public String getCommandName(){ return "setscore";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public void getCommandUsage(ICommandSender s, String tc)
		{
			sendString(s,tc + " [teamName] [against] [newScore]");
		}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() != 3) return false;
			String tn = strList.get(0);
			String to = strList.get(1);
			String sc = strList.get(2);
			int score;
			try
			{
				score = Integer.parseInt(sc);
			}
			catch(NumberFormatException e)
			{
				sendString(sen, sc + " is not a valid number");
				return false;
			}
			if(!TeamSystem.getTeamStore().getTeams().contains(tn))
			{
				sendString(sen,"Team not found, try creating it or assigning somebody to it");
				return false;
			}
			if(!TeamSystem.getTeamStore().getTeams().contains(to))
			{
				sendString(sen,"Team not found, try creating it or assigning somebody to it");
				return false;
			}
			TeamData td = TeamSystem.getTeamStore().getTeamData(tn);
			td.setScore(to, score);
			sendString(sen,"Score set");
			return true;
		}
	}
}
