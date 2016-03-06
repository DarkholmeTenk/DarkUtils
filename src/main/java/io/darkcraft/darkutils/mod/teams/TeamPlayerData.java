package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.helpers.MessageHelper;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class TeamPlayerData implements IExtendedEntityProperties
{
	private final WeakReference<EntityPlayer> player;
	private Region pastRegion = null;
	private Region inRegion = null;
	private int timeInRegion = 0;

	public TeamPlayerData(EntityPlayer pl)
	{
		player = new WeakReference(pl);
	}

	public void tick()
	{

	}

	public void setInRegion(Region r)
	{
		EntityPlayer pl = player.get();
		if(pl == null) return;
		if(r != inRegion)
		{
			pastRegion = inRegion;
			inRegion = r;
			timeInRegion = 0;
		}
		else
		{
			timeInRegion++;
			if(timeInRegion == 2)
			{
				if(pastRegion != null)
				{
					if(inRegion == null)
						MessageHelper.sendMessage(pl, "You are now leaving " + pastRegion.getName());
					else
						MessageHelper.sendMessage(pl, "You are now leaving " + pastRegion.getName()+"\nAnd now entering " + inRegion.getName());
				}
				else
					if(inRegion != null)
						MessageHelper.sendMessage(pl, "You are now entering " + inRegion.getName());
			}
		}
	}



	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void init(Entity entity, World world)
	{
		// TODO Auto-generated method stub

	}

}
