package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.datastore.SimpleCoordStore;
import io.darkcraft.darkcore.mod.helpers.TeleportHelper;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class TeamData
{
	private HashSet<String> players = new HashSet();
	private SimpleCoordStore home = null;

	public Set<String> getPlayers()
	{
		return players;
	}

	public void addPlayer(String name)
	{
		players.add(name);
		TeamSystem.getTeamStore().markDirty();
	}

	public void removePlayer(String name)
	{
		players.remove(name);
		TeamSystem.getTeamStore().markDirty();
	}

	public void setHome(SimpleCoordStore newHome)
	{
		home = newHome;
		TeamSystem.getTeamStore().markDirty();
	}

	public SimpleCoordStore getHome()
	{
		return home;
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
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		for(int i = 0; nbt.hasKey("p"+i);i++)
			players.add(nbt.getString("p"+i));
		home = nbt.hasKey("home") ? SimpleCoordStore.readFromNBT(nbt,"home") : null;
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
