package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.helpers.PlayerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;

public class Region
{
	private static HashMap<String,Region> regionMap = new HashMap();
	public static Region getRegion(EntityLivingBase ent)
	{
		for(Region r : regionMap.values())
		{
			if(r.parentRegion != null) continue;
			Region i = r.getRegionIn(ent);
			if(i != null) return i;
		}
		return null;
	}

	public static Region getRegion(String id, boolean create)
	{
		if(regionMap.containsKey(id))
			return regionMap.get(id);
		if(!create) return null;
		Region r = new Region(id);
		regionMap.put(id,r);
		TeamSystem.getTeamStore().markDirty();
		return r;
	}
	public static Set<String> getRegionIds()
	{
		return regionMap.keySet();
	}
	public static void removeRegion(String id)
	{
		Region r = regionMap.get(id);
		if(r == null) return;
		if(r.parentRegion != null)
			r.parentRegion.removeChild(r);
		for(Region sr : r.subRegions)
			r.removeChild(sr);
		regionMap.remove(id);
		TeamSystem.getTeamStore().markDirty();
	}

	public static Region readFromNBT(NBTTagCompound nbt, String nbtName)
	{
		if(!nbt.hasKey(nbtName)) return null;
		NBTTagCompound rNBT = nbt.getCompoundTag(nbtName);
		if(!rNBT.hasKey("id")) return null;
		String id = rNBT.getString("id");
		if(regionMap.containsKey(id))
			return getRegion(id,false);
		Region r = getRegion(id,true);
		r.readFromNBT(rNBT);
		return r;
	}
	public static void clear()
	{
		regionMap.clear();
	}

	public final String id;
	public final ArrayList<RegionZone> zones = new ArrayList<RegionZone>();
	public final HashSet<Region> subRegions = new HashSet<Region>();
	private String name;
	public Team owningTeam = null;
	public Region parentRegion;

	private Region(String id)
	{
		this.id = id;
	}

	public void addChild(Region r)
	{
		if(r == null) return;
		if((r.parentRegion != null) && (r.parentRegion != this))
			r.parentRegion.removeChild(r);
		subRegions.add(r);
		r.parentRegion = this;
	}

	public void removeChild(Region r)
	{
		subRegions.remove(r);
		r.parentRegion = null;
	}

	public Region getRegionIn(EntityLivingBase ent)
	{
		Region in = null;
		for(Region sr : subRegions)
		{
			in = sr.getRegionIn(ent);
			if(in != null) return in;
		}
		for(RegionZone rz : zones)
			if(rz.inZone(ent)) return this;
		return null;
	}

	public boolean inRegion(EntityLivingBase ent)
	{
		for(RegionZone rz : zones)
			if(rz.inZone(ent)) return true;
		return false;
	}

	public String getName()
	{
		return name == null ? id : name;
	}

	public void setName(String n)
	{
		name = n;
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("id", id);
		{
			int i = 0;
			for(Region r : subRegions)
			{
				NBTTagCompound snbt = new NBTTagCompound();
				r.writeToNBT(snbt);
				nbt.setTag("r"+(i++), snbt);
			}
		}
		for(int i = 0; i < zones.size(); i++)
		{
			NBTTagCompound snbt = new NBTTagCompound();
			zones.get(i).writeToNBT(snbt);
			nbt.setTag("z"+(i++),snbt);
		}
		if(owningTeam != null) nbt.setString("ownTeam", owningTeam.getRegisteredName());
		nbt.setString("name", getName());
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		if(parentRegion != null) parentRegion.removeChild(this);
		subRegions.clear();
		for(int i = 0; nbt.hasKey("r"+i); i++)
			addChild(readFromNBT(nbt,"r"+i));
		parentRegion = nbt.hasKey("parent")? getRegion(nbt.getString("parent"),false) : null;
		zones.clear();
		for(int i = 0; nbt.hasKey("z"+i); i++)
			zones.add(RegionZone.readFromNBT(nbt, "z"+i));
		owningTeam = nbt.hasKey("ownTeam") ? PlayerHelper.getTeam(0, nbt.getString("ownTeam")) : null;
		name = nbt.hasKey("name") ? nbt.getString("name") : null;
	}
}
