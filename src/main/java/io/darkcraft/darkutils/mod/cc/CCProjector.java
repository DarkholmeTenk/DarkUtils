package io.darkcraft.darkutils.mod.cc;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlock;
import io.darkcraft.darkcore.mod.abstracts.AbstractBlockContainer;
import io.darkcraft.darkcore.mod.abstracts.AbstractBlockRenderer;
import io.darkcraft.darkcore.mod.abstracts.AbstractTileEntity;
import io.darkcraft.darkcore.mod.helpers.MathHelper;
import io.darkcraft.darkutils.mod.DarkUtilsMod;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class CCProjector extends AbstractBlockContainer
{

	public CCProjector()
	{
		super(DarkUtilsMod.modName);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
	{
		return new CCProjectorTE();
	}

	@Override
	public Class<? extends TileEntity> getTEClass()
	{
		return CCProjectorTE.class;
	}

	@Override
	public void initData()
	{
		setBlockName("CCProjector");
	}

	@Override
	public void initRecipes()
	{
	}

	public static TileEntitySpecialRenderer renderer()
	{
		return CCProjectorRenderer.i;
	}

	@Optional.Interface(iface="dan200.computercraft.api.peripheral.IPeripheral",modid="ComputerCraft")
	public static class CCProjectorTE extends AbstractTileEntity implements IPeripheral
	{
		private boolean enabled;
		private String text;
		private double offsetX;
		private double offsetY;
		private double offsetZ;
		private double angle;
		private int color;
		private boolean shadow;
		private double scale = 1;
		private boolean showBack = true;

		@Override
		public String getType(){ return "CCProjector";}

		private static String[] methods = new String[]{"setEnabled","setText","setOffset","setAngle","setColor","setShadow","setScale","setShowBack"};
		@Override
		public String[] getMethodNames(){return methods;}

		private void clamp()
		{
			offsetX = MathHelper.clamp(offsetX, -10, 10);
			offsetY = MathHelper.clamp(offsetY, -10, 10);
			offsetZ = MathHelper.clamp(offsetZ, -10, 10);
			scale = MathHelper.clamp(scale, 0.1, 10);
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
		{
			switch(method)
			{
				case 0:enabled = (Boolean)arguments[0]; break;
				case 1: text = (String)arguments[0]; break;
				case 2: offsetX = (Double)arguments[0]; offsetY = (Double)arguments[1]; offsetZ = (Double)arguments[2]; break;
				case 3: angle = (Double)arguments[0]; break;
				case 4:
					if(arguments.length == 1)
						color = (int)(double)(Double)arguments[0];
					else
						color = (int)(((Double)arguments[0]*65536) + ((Double)arguments[1]*256) + (Double)arguments[2]);
					break;
				case 5: shadow = (Boolean)arguments[0]; break;
				case 6: scale = (Double)arguments[0]; break;
				case 7: showBack = (Boolean)arguments[0]; break;
			}
			clamp();
			queueUpdate();
			return new Object[]{null};
		}

		@Override
		public void attach(IComputerAccess computer){}

		@Override
		public void detach(IComputerAccess computer){}

		@Override
		public boolean equals(IPeripheral other){return this == other;}

		@Override
		public void writeTransmittable(NBTTagCompound nbt)
		{
			if((text == null) || text.isEmpty()) return;
			nbt.setString("t", text);
			nbt.setBoolean("e", enabled);
			nbt.setBoolean("s", shadow);
			nbt.setDouble("a", angle);
			nbt.setDouble("ox", offsetX);
			nbt.setDouble("oy", offsetY);
			nbt.setDouble("oz", offsetZ);
			nbt.setInteger("c", color);
			nbt.setDouble("sc", scale);
			nbt.setBoolean("bf", showBack);
		}

		@Override
		public void readTransmittable(NBTTagCompound nbt)
		{
			text = nbt.getString("t");
			enabled = nbt.getBoolean("e");
			shadow = nbt.getBoolean("s");
			color = nbt.getInteger("c");
			angle = nbt.getDouble("a");
			offsetX = nbt.getDouble("ox");
			offsetY = nbt.getDouble("oy");
			offsetZ = nbt.getDouble("oz");
			scale = nbt.getDouble("sc");
			showBack = nbt.getBoolean("bf");
			clamp();
		}

		@Override
		@SideOnly(Side.CLIENT)
	    public AxisAlignedBB getRenderBoundingBox()
	    {
			double mX = Math.min(0, -5+offsetX);
			double MX = Math.max(1, 5+offsetX);
			double mY = Math.min(0, -5+offsetY);
			double MY = Math.max(1, 5+offsetY);
			double mZ = Math.min(0, -5+offsetZ);
			double MZ = Math.max(1, 5+offsetZ);
			return AxisAlignedBB.getBoundingBox(xCoord+mX, yCoord+mY, zCoord+mZ, xCoord+MX, yCoord+MY, zCoord+MZ);
	    }

	}

	public static class CCProjectorRenderer extends AbstractBlockRenderer
	{
		public static TileEntitySpecialRenderer i = new CCProjectorRenderer();

		@Override
		public AbstractBlock getBlock()
		{
			return CC.ccProjector;
		}

		@Override
		public boolean handleLighting(){return false;}

		@Override
		public void renderBlock(Tessellator tess, TileEntity te, int x, int y, int z)
		{
			if(!(te instanceof CCProjectorTE)) return;
			CCProjectorTE pr = (CCProjectorTE) te;
			if(!pr.enabled) return;
			if(pr.text == null) return;
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			boolean reen = GL11.glIsEnabled(GL11.GL_CULL_FACE);
			if(pr.showBack)
				GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glTranslated(pr.offsetX, pr.offsetY+1, pr.offsetZ);
			GL11.glRotated(pr.angle, 0, 1, 0);
			GL11.glRotated(180, 0, 0, 1);
			GL11.glScaled(0.025, 0.025, 0.025);
			GL11.glScaled(pr.scale, pr.scale, pr.scale);
			List list = fr.listFormattedStringToWidth(pr.text, Integer.MAX_VALUE);
			int i = 0;
			for(Object o : list)
			{
				String s = (String) o;
				fr.drawString(s, 0, 10*i++, pr.color, pr.shadow);
			}
			if(pr.showBack && reen)
				GL11.glEnable(GL11.GL_CULL_FACE);
		}

	}
}
