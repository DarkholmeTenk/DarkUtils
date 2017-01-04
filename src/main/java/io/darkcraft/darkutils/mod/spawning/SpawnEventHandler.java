package io.darkcraft.darkutils.mod.spawning;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;

import io.darkcraft.darkcore.mod.abstracts.AbstractWorldDataStore;
import io.darkcraft.darkcore.mod.nbt.Mapper;
import io.darkcraft.darkcore.mod.nbt.NBTHelper;
import io.darkcraft.darkcore.mod.nbt.NBTProperty;
import io.darkcraft.darkcore.mod.nbt.NBTProperty.SerialisableType;
import io.darkcraft.darkcore.mod.nbt.NBTSerialisable;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SpawnEventHandler
{
	private static SpawnWorldDataStore spwds;

	public static void clear()
	{
		spwds = null;
	}

	public static SpawnWorldDataStore getSpwds()
	{
		if(spwds == null)
		{
			spwds = new SpawnWorldDataStore();
			spwds.load();
		}
		return spwds;
	}

	@SubscribeEvent
	public void entSpawnEvent(CheckSpawn e)
	{
		EntityLivingBase base = e.entityLiving;
		if(e.isCanceled() || (base == null) || (base instanceof EntityPlayer)) return;
		if(!getSpwds().spawn(base))
			e.setCanceled(true);
	}

	@NBTSerialisable
	public static class SpawnWorldDataStore extends AbstractWorldDataStore
	{
		private Mapper<SpawnWorldDataStore> mapper = NBTHelper.getMapper(SpawnWorldDataStore.class, SerialisableType.WORLD);
		private Random r = new Random();

		@NBTProperty
		public Map<String, Double> mobChances = new HashMap<String, Double>();

		public SpawnWorldDataStore(String _name)
		{
			this();
		}

		public SpawnWorldDataStore()
		{
			super("dcutils.spawnstore");
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt)
		{
			mapper.fillFromNBT(nbt, this);
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt)
		{
			mapper.writeToNBT(nbt, this);
		}

		public boolean spawn(EntityLivingBase ent)
		{
			String name = ent.getClass().getSimpleName();
			double chance = 0;
			if(!mobChances.containsKey(name))
				mobChances.put(name,chance=1);
			else
				chance = mobChances.get(name);
			return r.nextDouble() <= chance;
		}
	}
}
