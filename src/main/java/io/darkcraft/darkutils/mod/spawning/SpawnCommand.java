package io.darkcraft.darkutils.mod.spawning;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.command.ICommandSender;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommandNew;
import io.darkcraft.darkutils.mod.spawning.SpawnEventHandler.SpawnWorldDataStore;

public class SpawnCommand extends AbstractCommandNew
{
	public SpawnCommand()
	{
		super(new ListChancesSC());
	}

	@Override
	public String getCommandName()
	{
		return "dcspawn";
	}

	@Override
	public void getAliases(List<String> list)
	{
		list.addAll(Arrays.asList("darkutilspawning","darkutilspawn", "duspawn", "dspawn"));
	}

	@Override
	public boolean process(ICommandSender sen, List<String> strList)
	{
		return false;
	}

	private static class ListChancesSC extends AbstractCommandNew
	{

		@Override
		public String getCommandName()
		{
			return "list";
		}

		@Override
		public void getAliases(List<String> list){}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			for(Entry<String, Double> e : SpawnEventHandler.getSpwds().mobChances.entrySet())
			{
				double d = e.getValue();
				sendString(sen, String.format("%s: %.3d",e.getKey(),d));
			}
			return true;
		}
	}

	private static class SetChancesSC extends AbstractCommandNew
	{

		@Override
		public String getCommandName()
		{
			return "set";
		}

		@Override
		public void getAliases(List<String> list)
		{
			list.addAll(Arrays.asList("setchances", "setchance"));
		}

		@Override
		public boolean process(ICommandSender sen, List<String> strList)
		{
			if(strList.size() != 2)
				return false;
			try
			{
				String s = strList.get(1);
				double d = Double.parseDouble(strList.get(1));
				SpawnWorldDataStore spwds = SpawnEventHandler.getSpwds();
				if(spwds.mobChances.containsKey(s))
				{
					spwds.mobChances.put(s, d);
					return true;
				}
				return false;
			}
			catch(NumberFormatException e){}
			return false;
		}

	}
}
