package io.darkcraft.darkutils.mod.cc;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlock;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkutils.mod.DarkUtilsMod;
import io.darkcraft.darkutils.mod.teams.Region;
import io.darkcraft.darkutils.mod.teams.TeamData;
import io.darkcraft.darkutils.mod.teams.TeamSystem;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Optional.Interface(iface="dan200.computercraft.api.peripheral.IPeripheral",modid="ComputerCraft")
public class CCTeamBlock extends AbstractBlock implements IPeripheral
{

	public CCTeamBlock()
	{
		super(DarkUtilsMod.modName);
	}

	@Override
	public void initData()
	{
		setBlockName("CCTeam");
	}

	@Override
	public void initRecipes()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getType()
	{
		return "darkutil.team";
	}

	private static final String[] meths = {"getTeamNames","getTeamMembers","getTeam","getOnlinePlayers","getPos","getRegion","getRegionOwner","getRegionParent",
		"teleportHome","copyInv","getTeamScore"};
	@Override
	public String[] getMethodNames()
	{
		return meths;
	}

	private int gI(Object o)
	{
		if(o instanceof String)
			return Integer.parseInt((String)o);
		if(o instanceof Double)
		{
			double d = (Double) o;
			return (int) d;
		}
		return 0;
	}

	private Object[] toMap(Object[] in)
	{
		HashMap<Integer,Object> map = new HashMap<Integer,Object>();
		for(int i = 0; i < in.length; i++)
			map.put(i,in[i]);
		return new Object[]{map};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		if(method == 0)
			return toMap(TeamSystem.getTeamStore().getTeams().toArray());
		if(method == 1)
		{
			if(arguments.length == 1)
				return toMap(TeamSystem.getTeamStore().getTeamData((String) arguments[0]).getPlayers().toArray());
			return new Object[]{false};
		}
		if(method == 2)
		{
			if(arguments.length == 1)
			{
				EntityPlayer pl = PlayerHelper.getPlayer((String) arguments[0]);
				if(pl == null) return new Object[]{"offline"};
				Team t = pl.getTeam();
				if(t == null) return new Object[]{null};
				return new Object[]{t.getRegisteredName()};
			}
			return new Object[]{false};
		}
		if(method == 3)
		{
			return toMap(PlayerHelper.getAllUsernames());
		}
		if(method == 4)
		{
			if(arguments.length == 1)
			{
				EntityPlayer pl = PlayerHelper.getPlayer((String) arguments[0]);
				if(pl == null) return new Object[]{false};
				return new Object[]{WorldHelper.getWorldID(pl),pl.posX,pl.posY,pl.posZ};
			}
			return new Object[]{"noargs"};
		}
		if(method == 5)
		{
			if(arguments.length == 1)
			{
				EntityPlayer pl = PlayerHelper.getPlayer((String) arguments[0]);
				if(pl == null) return new Object[]{false};
				Region r = Region.getRegion(pl);
				if(r == null) return new Object[]{null};
				return new Object[]{r.id};
			}
			return new Object[]{"noargs"};
		}
		if(method == 6)
		{
			if(arguments.length == 1)
			{
				Region r = Region.getRegion((String) arguments[0], false);
				if(r == null) return new Object[]{"Noregion"};
				if(r.owningTeam == null) return new Object[]{null};
				return new Object[]{r.owningTeam.getRegisteredName()};
			}
			return new Object[]{"noargs"};
		}
		if(method == 7)
		{
			if(arguments.length == 1)
			{
				Region r = Region.getRegion((String) arguments[0], false);
				if(r == null) return new Object[]{"Noregion"};
				if(r.parentRegion == null) return new Object[]{null};
				return new Object[]{r.parentRegion.id};
			}
			return new Object[]{"noargs"};
		}
		if(method == 8)
		{
			if(arguments.length == 1)
			{
				EntityPlayer pl = PlayerHelper.getPlayer((String) arguments[0]);
				if(pl == null) return new Object[]{"nopl"};
				Team t = pl.getTeam();
				if(t == null) return new Object[]{"noteam"};
				TeamData td = TeamSystem.getTeamStore().getTeamData(t);
				td.teleportHome(pl);
				return new Object[]{true};
			}
			return new Object[]{"noargs"};
		}
		if(method == 9)
		{
			if(arguments.length == 5)
			{
				EntityPlayer pl = PlayerHelper.getPlayer((String) arguments[0]);
				if(pl == null) return new Object[]{"nopl"};
				World w = WorldHelper.getWorldServer(gI(arguments[1]));
				if(w == null) return new Object[]{"nowrld"};
				int x = gI(arguments[2]);
				int y = gI(arguments[3]);
				int z = gI(arguments[4]);
				TileEntity te = w.getTileEntity(x, y, z);
				if(!(te instanceof IInventory)) return new Object[]{"noinv"};
				IInventory inv = (IInventory)te;
				int num = Math.min(pl.inventory.mainInventory.length, inv.getSizeInventory());
				for(int i = 0;i < num; i++)
					if(inv.getStackInSlot(i) != null)
						pl.inventory.mainInventory[i] = inv.getStackInSlot(i).copy();
				pl.inventory.inventoryChanged = true;
				return new Object[]{true};
			}
			return new Object[]{"noargs"};
		}
		if(method == 10)
		{
			if(arguments.length == 1)
			{
				Team t = PlayerHelper.getTeam(0, (String) arguments[0]);
				if(t == null) return new Object[]{"noteam"};
				TeamData td = TeamSystem.getTeamStore().getTeamData(t);
				return new Object[]{td.getTotalScore()};
			}
			else if(arguments.length == 2)
			{
				Team t = PlayerHelper.getTeam(0, (String) arguments[0]);
				if(t == null) return new Object[]{"noteam"};
				TeamData td = TeamSystem.getTeamStore().getTeamData(t);
				Team to = PlayerHelper.getTeam(0, (String) arguments[1]);
				if(to == null) return new Object[]{"noteam2"};
				return new Object[]{td.getScore(to.getRegisteredName())};
			}
			else
				return new Object[]{"noargs"};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer){}

	@Override
	public void detach(IComputerAccess computer){}

	@Override
	public boolean equals(IPeripheral other)
	{
		return other == this;
	}
}
