package io.darkcraft.darkutils.mod.teams.commands;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommandNew;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkutils.mod.teams.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.Team;

public class RegionCommand extends AbstractCommandNew
{
	public RegionCommand()
	{
		super(new ListSC(), new CreateSC(), new RemoveSC());
	}

	@Override
	public String getCommandName()
	{
		return "region";
	}

	@Override
	public void getAliases(List<String> list)
	{
		list.add("reg");
		list.add("r");
	}

	@Override
	public boolean process(ICommandSender sen, List<String> strList)
	{
		return false;
	}

	private static class ListSC extends AbstractCommandNew
	{
		@Override
		public String getCommandName(){return "list";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			sendString(sen, "Region IDs:");
			List<String> ids = new ArrayList(Region.getRegionIds());
			Collections.sort(ids);
			for(String s : ids)
			{
				Region r = Region.getRegion(s, false);
				if(r == null) continue;
				sendString(sen,String.format("%10s: Name:%s ParentID:%s Team:%s", s, r.getName(),r.parentRegion,r.owningTeam));
			}
			return true;
		}
	}

	private static class CreateSC extends AbstractCommandNew
	{
		@Override
		public String getCommandName(){return "create";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public void getCommandUsage(ICommandSender s, String tc)
		{
			sendString(s,tc + " [newID] <name> <team> <parent>");
		}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() >= 1)
			{
				String newID = strList.get(0);
				Region r = Region.getRegion(newID, false);
				sendString(sen,(r == null ? "Created region " : "Updating region ") + newID);
				r = Region.getRegion(newID, true);
				if(strList.size() == 4)
				{
					String name = strList.get(1);
					String tN = strList.get(2);
					Team t = null;
					if(!(tN.equals("null") || tN.isEmpty()))
					{
						t = PlayerHelper.getTeam(0, strList.get(2));
						if(t == null)
						{
							sendString(sen,"Team does not exist");
							return false;
						}
					}
					Region parent = null;
					String pN = strList.get(3);
					if(!(pN.equals("null") || pN.isEmpty()))
					{
						parent = Region.getRegion(pN, false);
						if(parent == null)
						{
							sendString(sen,"Parent region does not exist");
							return false;
						}
					}
					r.setName(name);
					r.owningTeam = t;
					if((parent == null) && (r.parentRegion != null)) r.parentRegion.removeChild(r);
				}
				return true;
			}
			return false;
		}
	}

	private static class RemoveSC extends AbstractCommandNew
	{
		@Override
		public String getCommandName(){return "remove";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public void getCommandUsage(ICommandSender s, String tc)
		{
			sendString(s,tc + " [id]");
		}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() >= 1)
			{
				String newID = strList.get(0);
				Region.removeRegion(newID);
				sendString(sen,"Removed region");
				return true;
			}
			return false;
		}
	}

}
