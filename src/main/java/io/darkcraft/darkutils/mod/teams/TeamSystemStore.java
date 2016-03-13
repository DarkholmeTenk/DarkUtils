package io.darkcraft.darkutils.mod.teams;

import io.darkcraft.darkcore.mod.abstracts.AbstractWorldDataStore;
import io.darkcraft.darkcore.mod.datastore.SimpleDoubleCoordStore;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.TeleportHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Type;

public class TeamSystemStore extends AbstractWorldDataStore
{
	private HashMap<String,String> teamPlayerMap = new HashMap();
	private WeakHashMap<EntityPlayer,TeamPlayerData> playerData = new WeakHashMap();
	private HashMap<String,TeamData> teamData = new HashMap();

	public TeamSystemStore()
	{
		super("DUTeamSystem",0);
	}

	public TeamSystemStore(String s)
	{
		super(s);
	}

	int tt = 0;
	public TeamPlayerData getTeamPlayerData(EntityPlayer pl)
	{
		return playerData.get(pl);
	}

	public Set<String> getTeams()
	{
		return teamData.keySet();
	}

	public TeamData getTeamData(String name)
	{
		if(!teamData.containsKey(name))
			teamData.put(name, new TeamData());
		return teamData.get(name);
	}

	public TeamData getTeamData(Team t)
	{
		if(t == null) return null;
		return getTeamData(t.getRegisteredName());
	}

	public TeamData getTeamData(EntityPlayer pl)
	{
		if(pl == null) return null;
		return getTeamData(pl.getTeam());
	}

	@SubscribeEvent
	public void playerHandler(EntityConstructing event)
	{
		Entity ent = event.entity;
		if(ent instanceof EntityPlayer)
		{
			EntityPlayer pl = (EntityPlayer) ent;
			TeamPlayerData tpd = new TeamPlayerData(pl);
			playerData.put(pl, tpd);
			ent.registerExtendedProperties("dcTPD", tpd);
		}
	}

	private void handlePlayerTeam(String pl, String t)
	{
		String ot = teamPlayerMap.get(pl);
		if((ot == null) && (t == null)) return;
		if((ot != null) && !(ot.equals(t)))
		{
			TeamData td = getTeamData(ot);
			td.removePlayer(pl);
			if(t == null)
				teamPlayerMap.remove(pl);
		}
		if((t != null) && !(t.equals(ot)))
		{
			TeamData td = getTeamData(t);
			td.addPlayer(pl);
			teamPlayerMap.put(pl, t);
		}
	}

	private void handlePlayerData()
	{
		for(Entry<EntityPlayer, TeamPlayerData> entry : playerData.entrySet())
		{
			EntityPlayer pl = entry.getKey();
			TeamPlayerData tpd = entry.getValue();
			if(pl.isDead) continue;
			Team t = PlayerHelper.getTeam(pl);
			handlePlayerTeam(PlayerHelper.getUsername(pl), t==null ? null : t.getRegisteredName());
			Region r = Region.getRegion(pl);
			tpd.setInRegion(r);
		}
	}

	private volatile Map<EntityLivingBase,SimpleDoubleCoordStore> teleportQueue = new HashMap();
	public void addToTeleportQueue(EntityLivingBase ent, SimpleDoubleCoordStore location)
	{
		synchronized(teleportQueue)
		{
			teleportQueue.put(ent,location);
		}
	}

	@SubscribeEvent
	public void tickHandler(ServerTickEvent event)
	{
		if((event.type != Type.SERVER) || (event.phase != Phase.END)) return;
		synchronized(teleportQueue)
		{
			for(Entry<EntityLivingBase,SimpleDoubleCoordStore> ent : teleportQueue.entrySet())
				TeleportHelper.teleportEntity(ent.getKey(), ent.getValue());
			teleportQueue.clear();
		}
		tt++;
		if((tt % 20) == 0) handlePlayerData();
	}

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void deathHandler(LivingDeathEvent event)
	{
		if(event.isCanceled()) return;
		EntityLivingBase ent = event.entityLiving;
		Entity oth = event.source.getSourceOfDamage();
		if(!(oth instanceof EntityLivingBase)) return;
		EntityLivingBase other = (EntityLivingBase)oth;
		Team deadTeam = ent.getTeam();
		Team killTeam = other.getTeam();
		if((killTeam == null) || (deadTeam == null)) return;
		TeamData killData = getTeamData(killTeam);
		TeamData deadData = getTeamData(deadTeam);
		if(deadTeam == killTeam) killData.addPoint(killTeam.getRegisteredName(), TeamSystem.selfTeamKillPoints);
		Region or = Region.getRegion(ent);
		Team owningTeam = (or == null) ? null : or.owningTeam;
		if(owningTeam == null) killData.addPoint(deadTeam.getRegisteredName(), TeamSystem.killInNoRegionPoints);
		if(killTeam.isSameTeam(owningTeam)) killData.addPoint(deadTeam.getRegisteredName(), TeamSystem.killInYourRegionPoints);
		if(deadTeam.isSameTeam(owningTeam)) killData.addPoint(deadTeam.getRegisteredName(), TeamSystem.killInEnemyRegionPoints);
		if(ent instanceof EntityPlayer)
		{
			EntityPlayer pl = (EntityPlayer)ent;
			if((pl.getBedLocation(0) == null) && (deadData.getHome() != null))
				pl.setSpawnChunk(deadData.getHome().toChunkCoordinates(), true, 0);
		}
		//String tn = StatCollector.translateToLocal(killTeam.getRegisteredName());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		if(ServerHelper.isClient()) return;
		for(int i = 0; nbt.hasKey("r"+i); i++)
			Region.readFromNBT(nbt, "r"+i);
		for(int i = 0; nbt.hasKey("t"+i); i++)
		{
			NBTTagCompound snbt = nbt.getCompoundTag("t"+i);
			String name = snbt.getString("tn");
			teamData.put(name, TeamData.readFromNBTStatic(snbt));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		//Write the regions
		{
			int i = 0;
			Set<String> regionIDs = Region.getRegionIds();
			for(String s : regionIDs)
			{
				Region r = Region.getRegion(s, false);
				if(r == null) continue;
				if(r.parentRegion != null) continue;
				NBTTagCompound rnbt = new NBTTagCompound();
				r.writeToNBT(rnbt);
				nbt.setTag("r"+(i++), rnbt);
			}
		}
		{
			int i = 0;
			for(Entry<String,TeamData> tent : teamData.entrySet())
			{
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setString("tn", tent.getKey());
				tent.getValue().writeToNBT(snbt);
				nbt.setTag("t"+(i++), snbt);
			}
		}
	}

}
