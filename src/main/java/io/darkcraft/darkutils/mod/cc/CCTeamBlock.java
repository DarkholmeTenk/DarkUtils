package io.darkcraft.darkutils.mod.cc;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlockContainer;
import io.darkcraft.darkcore.mod.abstracts.AbstractTileEntity;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkutils.mod.DarkUtilsMod;
import io.darkcraft.darkutils.mod.teams.Region;
import io.darkcraft.darkutils.mod.teams.TeamData;
import io.darkcraft.darkutils.mod.teams.TeamSystem;
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
public class CCTeamBlock extends AbstractBlockContainer implements IPeripheral
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
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
	{
		return new CCTeamTE();
	}

	@Override
	public Class<? extends TileEntity> getTEClass()
	{
		return CCTeamTE.class;
	}

	private static class CCTeamTE extends AbstractTileEntity
	{

	}

	@Override
	public String getType()
	{
		return "darkutil.team";
	}

	private static final String[] meths = {"getTeamNames","getTeamMembers","getTeam","getOnlinePlayers","getPos","getRegion","getRegionOwner","getRegionParent",
		"teleportHome","copyInv"};
	@Override
	public String[] getMethodNames()
	{
		return meths;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		if(method == 0)
			return TeamSystem.getTeamStore().getTeams().toArray();
		if(method == 1)
		{
			if(arguments.length == 1)
				return TeamSystem.getTeamStore().getTeamData((String) arguments[0]).getPlayers().toArray();
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
			return PlayerHelper.getAllUsernames();
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
		if(method == 6)
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
		if(method == 9)
		{
			if(arguments.length == 5)
			{
				EntityPlayer pl = PlayerHelper.getPlayer((String) arguments[0]);
				if(pl == null) return new Object[]{"nopl"};
				World w = WorldHelper.getWorld((Integer) arguments[1]);
				int x = (Integer) arguments[2];
				int y = (Integer) arguments[3];
				int z = (Integer) arguments[4];
				TileEntity te = w.getTileEntity(x, y, z);
				if(!(te instanceof IInventory)) return new Object[]{"noinv"};
				IInventory inv = (IInventory)te;
				int num = Math.min(pl.inventory.getSizeInventory(), inv.getSizeInventory());
				for(int i = 0;i < num; i++)
					pl.inventory.setInventorySlotContents(i, inv.getStackInSlot(i).copy());
			}
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
