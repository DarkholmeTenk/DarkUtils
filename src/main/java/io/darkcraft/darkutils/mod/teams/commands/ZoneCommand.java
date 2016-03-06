package io.darkcraft.darkutils.mod.teams.commands;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommandNew;
import io.darkcraft.darkutils.mod.teams.Region;
import io.darkcraft.darkutils.mod.teams.RegionZone;

import java.util.List;

import net.minecraft.command.ICommandSender;

public class ZoneCommand extends AbstractCommandNew
{
	public ZoneCommand()
	{
		super(new ListSC(), new CreateSC(), new RemoveSC());
	}

	@Override
	public String getCommandName(){return "zone";}

	@Override
	public void getAliases(List<String> list){}

	@Override
	public boolean process(ICommandSender sen, List<String> strList){return false;}

	private static class ListSC extends AbstractCommandNew
	{

		@Override
		public String getCommandName(){return "list";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public void getCommandUsage(ICommandSender sen,String tc){sendString(sen,tc+" [regionID]");}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() == 1)
			{
				String rid = strList.get(0);
				Region r = Region.getRegion(rid, false);
				if(r == null)
				{
					sendString(sen,"Region not found");
					return false;
				}
				sendString(sen,"Region zone list:");
				for(int i = 0; i < r.zones.size(); i++)
					sendString(sen,String.format("%2d - %s",i,r.zones.get(i).toString()));
				return true;
			}
			return false;
		}

	}

	private static class CreateSC extends AbstractCommandNew
	{

		@Override
		public String getCommandName(){ return "create";}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public void getCommandUsage(ICommandSender sen,String tc){sendString(sen,tc+" [regionID] [world] [minX] [maxX] [minZ] [maxZ]");}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() == 6)
			{
				String rid = strList.get(0);
				Region r = Region.getRegion(rid, false);
				if(r == null)
				{
					sendString(sen,"Region not found");
					return false;
				}
				try
				{
					int w = Integer.parseInt(strList.get(1));
					int x = Integer.parseInt(strList.get(2));
					int X = Integer.parseInt(strList.get(3));
					int z = Integer.parseInt(strList.get(4));
					int Z = Integer.parseInt(strList.get(5));
					RegionZone rz = new RegionZone(w,x,z,X,Z);
					r.zones.add(rz);
					sendString(sen, "Zone created for region: "+ rid);
					sendString(sen, rz.toString());
					return true;
				}
				catch(NumberFormatException e)
				{
					sendString(sen,"Not a number");
					return false;
				}
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
		public void getCommandUsage(ICommandSender sen,String tc){sendString(sen,tc+" [regionID] [zoneID]");}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() == 2)
			{
				String rid = strList.get(0);
				Region r = Region.getRegion(rid, false);
				if(r == null)
				{
					sendString(sen,"Region not found");
					return false;
				}
				try
				{
					int z = Integer.parseInt(strList.get(1));
					if((z < 0) || (z >= r.zones.size()))
					{
						sendString(sen,z+" > max zone");
						return false;
					}
					RegionZone rz = r.zones.remove(z);
					sendString(sen,"Zone " + rz + " removed");
					return true;
				}
				catch(NumberFormatException e)
				{
					sendString(sen,"Not a number");
					return false;
				}
			}
			return false;
		}
	}
}
