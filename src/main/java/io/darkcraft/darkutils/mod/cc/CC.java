package io.darkcraft.darkutils.mod.cc;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlock;
import io.darkcraft.darkutils.mod.teams.TeamSystem;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.Interface(iface="dan200.computercraft.api.peripheral.IPeripheralProvider",modid="ComputerCraft")
public class CC implements IPeripheralProvider
{
	public static final CC i = new CC();
	public static boolean installed = false;

	public static AbstractBlock ccTeamBlock;
	public static AbstractBlock ccProjector;
	public static void init()
	{
		installed = Loader.isModLoaded("ComputerCraft");
		if(installed)
		{
			i.register();
			if(TeamSystem.teamSystemEnabled)
				ccTeamBlock = new CCTeamBlock().register();
			ccProjector = new CCProjector().register();
		}
	}

	private void register()
	{
		ComputerCraftAPI.registerPeripheralProvider(this);
	}

	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side)
	{
		Block b = world.getBlock(x,y,z);
		TileEntity te = world.getTileEntity(x, y, z);
		if(b instanceof CCTeamBlock) return (CCTeamBlock) b;
		if(te instanceof CCProjector.CCProjectorTE) return (CCProjector.CCProjectorTE) te;
		return null;
	}

}
