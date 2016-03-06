package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class RegionZone
{
	private final int w;
	private final int x;
	private final int X;
	private final int z;
	private final int Z;

	public RegionZone(int _w, int minX, int minZ, int maxX, int maxZ)
	{
		w = _w;
		x = Math.min(minX,maxX);
		X = Math.max(maxX,minX);
		z = Math.min(minZ,maxZ);
		Z = Math.max(minZ,maxZ);
		TeamSystem.getTeamStore().markDirty();
	}

	public boolean inZone(EntityLivingBase ent)
	{
		int wid = WorldHelper.getWorldID(ent);
		if(wid != w) return false;
		if((ent.posX < x) || (ent.posX > X)) return false;
		if((ent.posZ < z) || (ent.posZ > Z)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return w +" - "+ x+"-"+X+","+z+"-"+Z;
	}

	public static RegionZone readFromNBT(NBTTagCompound nbt, String id)
	{
		if(!nbt.hasKey(id)) return null;
		NBTTagCompound snbt = nbt.getCompoundTag(id);
		int w = snbt.getInteger("w");
		int x = snbt.getInteger("x");
		int X = snbt.getInteger("X");
		int z = snbt.getInteger("z");
		int Z = snbt.getInteger("Z");
		return new RegionZone(w,x,z,X,Z);
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("w",w);
		nbt.setInteger("x",x);
		nbt.setInteger("X",X);
		nbt.setInteger("z",z);
		nbt.setInteger("Z",Z);
	}
}
