package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.datastore.SimpleCoordStore;
import io.darkcraft.darkcore.mod.helpers.TeleportHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class TeamData
{
	private HashMap<String,Integer> score = new HashMap();
	private HashSet<String> players = new HashSet();
	private SimpleCoordStore home = null;

	public Set<String> getPlayers()
	{
		return players;
	}

	public void addPlayer(String name)
	{
		players.add(name);
		markDirty();
	}

	public void removePlayer(String name)
	{
		players.remove(name);
		markDirty();
	}

	public void setHome(SimpleCoordStore newHome)
	{
		home = newHome;
		markDirty();
	}

	public SimpleCoordStore getHome()
	{
		return home;
	}

	public int getScore(String team)
	{
		return score.containsKey(team) ? score.get(team) : 0;
	}

	public int getTotalScore()
	{
		int t = 0;
		for(Integer i : score.values())
			t+=i;
		return t;
	}

	public void setScore(String team, int newScore)
	{
		score.put(team,newScore);
		markDirty();
	}

	public void addPoint(String team, int points)
	{
		int c = points + (score.containsKey(team) ? score.get(team) : 0);
		score.put(team, c);
		markDirty();
	}

	public void markDirty()
	{
		TeamSystem.getTeamStore().markDirty();
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		{
			int i = 0;
			for(String s : players)
				nbt.setString("p"+(i++),s);
		}
		if(home != null)
			home.writeToNBT(nbt, "home");
		int i = 0;
		for(Entry<String, Integer> ent : score.entrySet())
		{
			nbt.setString("sct"+i,ent.getKey());
			nbt.setInteger("scs"+i, ent.getValue());
			i++;
		}
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		for(int i = 0; nbt.hasKey("p"+i);i++)
			players.add(nbt.getString("p"+i));
		home = nbt.hasKey("home") ? SimpleCoordStore.readFromNBT(nbt,"home") : null;
		{
			score.clear();
			int i = 0;
			while(nbt.hasKey("sct"+i))
			{
				score.put(nbt.getString("sct"+i), nbt.getInteger("scs"+i));
				i++;
			}
		}
	}

	public static TeamData readFromNBTStatic(NBTTagCompound nbt)
	{
		TeamData td = new TeamData();
		td.readFromNBT(nbt);
		return td;
	}

	public void teleportHome(EntityPlayer pl)
	{
		TeleportHelper.teleportEntity(pl, home.getCenter());
	}

}
