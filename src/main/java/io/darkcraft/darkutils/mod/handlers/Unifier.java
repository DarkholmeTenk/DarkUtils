package io.darkcraft.darkutils.mod.handlers;

import io.darkcraft.darkcore.mod.config.ConfigFile;
import io.darkcraft.darkcore.mod.helpers.MessageHelper;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkutils.mod.DarkUtilsMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;

public class Unifier
{
	private static ConfigFile config = null;
	private HashMap<String,ItemStack> unifiedMap = new HashMap<String,ItemStack>();
	int tt = 0;
	private static String[] acceptedBeginnings;
	private static String[] acceptedEnds;
	private static int delay = 20;

	public static HashSet<String> disabledUsernames = new HashSet();

	public static void refreshConfigs()
	{
		if(config == null) config = DarkUtilsMod.configHandler.registerConfigNeeder("Unifier");
		String joinedBeginnings = config.getString("Valid beginnings", "ore,ingot,gem",
				"Comma separated list of valid ore dictionary name prefixes",
				"An ore dictionary name must be in EITHER of these two lists");
		String joinedEnds = config.getString("Valid endings", "",
				"Comma separated list of valid ore dictionary name suffixes",
				"An ore dictionary name must be in EITHER of these two lists");
		acceptedBeginnings = joinedBeginnings.split(",");
		acceptedEnds = joinedEnds.split(",");
		delay = config.getInt("Delay", 20, "Number of ticks between repeated ore dictionary checks");
	}

	private boolean isValidOreName(String name)
	{
		for(String b : acceptedBeginnings)
			if(!b.isEmpty())
				if(name.startsWith(b)) return true;
		for(String e : acceptedEnds)
			if(!e.isEmpty())
				if(name.endsWith(e)) return true;
		return false;
	}

	public void getUnificationList()
	{
		String[] oreNames = OreDictionary.getOreNames();
		for(String oreName : oreNames)
		{
			if(!isValidOreName(oreName)) continue;
			List<ItemStack> items = OreDictionary.getOres(oreName);
			if((items == null) || (items.size() == 0)) continue;
			unifiedMap.put(oreName, items.get(0));
		}
	}

	@SubscribeEvent
	public void handleTick(ServerTickEvent event)
	{
		if (event.side.equals(Side.SERVER) && event.phase.equals(TickEvent.Phase.END))
		{
			if(((++tt) % 20) == 0)
			{
				tt = 1;
				String[] usernames = MinecraftServer.getServer().getAllUsernames();
				for(String username : usernames)
				{
					synchronized(disabledUsernames)
					{
						if(disabledUsernames.contains(username)) continue;
					}
					EntityPlayer player = ServerHelper.getPlayer(username);
					if(player != null)
						unify(player);
				}
			}
		}
	}

	@SubscribeEvent
	public void handleItemSpawn(EntityJoinWorldEvent event)
	{
		if(event.isCanceled()) return;
		Entity e = event.entity;
		if(e instanceof EntityItem)
		{
			EntityItem ei = (EntityItem) e;
			ItemStack i = ei.getEntityItem();
			ItemStack n = getUnified(i);
			if(n != i)
				ei.setEntityItemStack(n);
		}
	}

	private ItemStack newItemStack(ItemStack old, int amount)
	{
		if(old == null) return null;
		ItemStack toRet = old.copy();
		toRet.stackSize = amount;
		return toRet;
	}

	private ItemStack getUnified(ItemStack is)
	{
		if(is == null) return null;
		int[] possibleIDs = OreDictionary.getOreIDs(is);
		for(int id : possibleIDs)
		{
			String associatedName = OreDictionary.getOreName(id);
			if(unifiedMap.containsKey(associatedName))
			{
				ItemStack unified = unifiedMap.get(associatedName);
				if(OreDictionary.itemMatches(unified, is, true)) break;
				ItemStack ret = unified.copy();
				ret.stackSize = is.stackSize;
				return ret;
			}
		}
		return is;
	}

	private void unify(EntityPlayer player)
	{
		ItemStack[] inventory = player.inventory.mainInventory;
		HashMap<String, Integer> current = new HashMap<String,Integer>();
		boolean changed = true;
		for(int i = 0; i < inventory.length; i++)
		{
			ItemStack is = inventory[i];
			if(is == null) continue;
			int[] possibleIDs = OreDictionary.getOreIDs(is);
			for(int id : possibleIDs)
			{
				String associatedName = OreDictionary.getOreName(id);
				if(unifiedMap.containsKey(associatedName))
				{
					ItemStack unified = unifiedMap.get(associatedName);
					if(OreDictionary.itemMatches(unified, is, true)) break;
					changed = true;
					int size = is.stackSize;
					if(current.containsKey(associatedName))
						size += current.get(associatedName);
					current.put(associatedName, size);
					inventory[i] = null;
					break;
				}
			}
		}
		for(String name : current.keySet())
		{
			ItemStack is = unifiedMap.get(name);
			int amount = current.get(name);
			Item i = is.getItem();
			int max = i.getItemStackLimit(is);
			while(amount >= max)
			{
				WorldHelper.giveItemStack(player, newItemStack(is, max));
				amount -= max;
			}
			if(amount > 0)
				WorldHelper.giveItemStack(player, newItemStack(is, amount));
		}
		if(changed)
			player.inventory.markDirty();
	}

	public static void togglePlayer(EntityPlayer pl)
	{
		synchronized(disabledUsernames)
		{
			String un = PlayerHelper.getUsername(pl);
			if(disabledUsernames.contains(un))
			{
				disabledUsernames.remove(un);
				MessageHelper.sendMessage(pl, "Unifier now disabled for you");
			}
			else
			{
				disabledUsernames.add(un);
				MessageHelper.sendMessage(pl, "Unifier now enabled for you");
			}
		}
	}
}
