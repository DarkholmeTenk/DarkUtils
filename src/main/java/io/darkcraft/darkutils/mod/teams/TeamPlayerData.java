package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.helpers.MessageHelper;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class TeamPlayerData implements IExtendedEntityProperties
{
	private final WeakReference<EntityPlayer> player;
	private Region pastRegion = null;
	private Region inRegion = null;
	private int timeInRegion = 0;
	private boolean inPVP = true;

	public TeamPlayerData(EntityPlayer pl)
	{
		player = new WeakReference(pl);
	}

	public void tick()
	{

	}

	private String getRegionName(Region r)
	{
		EntityPlayer pl = player.get();
		if(pl == null) return "";
		Team t = pl.getTeam();
		if((t != null) && (r.owningTeam!=t))
		{
			if(r.owningTeam != null)
				return "(H)"+r.getName();
			else
				return "(N)"+r.getName();
		}
		return r.getName();
	}

	public void setInRegion(Region r)
	{
		EntityPlayer pl = player.get();
		if(pl == null) return;
		Team plT = pl.getTeam();
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
						MessageHelper.sendMessage(pl, "You are now leaving " + getRegionName(pastRegion));
					else
						MessageHelper.sendMessage(pl, "You are now leaving " + getRegionName(pastRegion)+"\nAnd now entering " + getRegionName(inRegion));
				}
				else
					if(inRegion != null)
						MessageHelper.sendMessage(pl, "You are now entering " + getRegionName(inRegion));
				Team ot = pastRegion == null ? null : pastRegion.owningTeam;
				Team nt = inRegion == null ? null : inRegion.owningTeam;
				if((ot == null) || (nt == null) || !ot.isSameTeam(nt))
				{
					if(!inPVP)
					{
						inPVP = true;
						if((nt != null) && !nt.isSameTeam(PlayerHelper.getTeam(pl)))
							MessageHelper.sendMessage(pl, "PVP has been re-enabled, use '/dct pvp' to disable", 10);
						else
							MessageHelper.sendMessage(pl, "PVP has been re-enabled",10);
					}
				}
			}
			if((plT!= null)&&(inRegion != null) && (inRegion.owningTeam!= null) && (inRegion.owningTeam!=plT))
			{
				if((timeInRegion >= TeamSystem.goHomeTime) && inPVP())
				{
					TeamData pltD = TeamSystem.getTeamStore().getTeamData(plT);
					if(pltD.getHome() != null)
						pltD.teleportHome(pl);
				}
			}
		}
	}



	@Override
	public void saveNBTData(NBTTagCompound nbt)
	{
		nbt.setBoolean("inPVP", inPVP);
		if(inRegion != null)
			nbt.setString("rid", inRegion.id);
		nbt.setInteger("rt", timeInRegion);
	}

	@Override
	public void loadNBTData(NBTTagCompound nbt)
	{
		inPVP = nbt.hasKey("inPVP") ? nbt.getBoolean("inPVP") : true;
		inRegion = nbt.hasKey("rid") ? Region.getRegion(nbt.getString("rid"), false) : null;
		timeInRegion = nbt.getInteger("rt");
	}

	@Override
	public void init(Entity entity, World world)
	{
		// TODO Auto-generated method stub

	}

	public void togglePVP()
	{
		inPVP = !inPVP;
	}

	public boolean inPVP()
	{
		return inPVP;
	}

}
