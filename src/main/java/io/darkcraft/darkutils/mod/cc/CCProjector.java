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
import net.minecraft.util.ResourceLocation;
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
		private boolean bgEnabled = false;
		private double bgOffsetX;
		private double bgOffsetY;
		private double bgSizeX;
		private double bgSizeY;
		private long bgColor;

		@Override
		public String getType(){ return "CCProjector";}

		private static String[] methods = new String[]{"setEnabled","setText","setOffset","setAngle","setColor","setShadow","setScale","setShowBack",
			"setBGEnabled","setBGOffset","setBGSize","setBGColor"};
		@Override
		public String[] getMethodNames(){return methods;}

		private void clamp()
		{
			offsetX = MathHelper.clamp(offsetX, -10, 10);
			offsetY = MathHelper.clamp(offsetY, -10, 10);
			offsetZ = MathHelper.clamp(offsetZ, -10, 10);
			scale = MathHelper.clamp(scale, 0.1, 10);
		}
		private long gL(Object o, int shift)
		{
			long x = (int)(double)(Double)o;
			return x<<shift;
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
						color = (int)(gL(arguments[0],16) | gL(arguments[1],8) | gL(arguments[2],0));
					break;
				case 5: shadow = (Boolean)arguments[0]; break;
				case 6: scale = (Double)arguments[0]; break;
				case 7: showBack = (Boolean)arguments[0]; break;
				case 8: bgEnabled = (Boolean)arguments[0]; break;
				case 9: bgOffsetX = (Double)arguments[0]; bgOffsetY = (Double)arguments[1]; break;
				case 10: bgSizeX = (Double)arguments[0]; bgSizeY = (Double)arguments[1]; break;
				case 11: if(arguments.length == 1)
					bgColor = (int)(double)(Double)arguments[0];
				else
					bgColor = gL(arguments[3],24) | gL(arguments[0],16) | gL(arguments[1],8) | gL(arguments[2],0);
				break;
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
			nbt.setBoolean("bgE", bgEnabled);
			nbt.setDouble("bgOX", bgOffsetX);
			nbt.setDouble("bgOY", bgOffsetY);
			nbt.setDouble("bgW", bgSizeX);
			nbt.setDouble("bgH", bgSizeY);
			nbt.setLong("bgC", bgColor);
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
			bgEnabled = nbt.getBoolean("bgE");
			bgOffsetX = nbt.getDouble("bgOX");
			bgOffsetY = nbt.getDouble("bgOY");
			bgSizeX = nbt.getDouble("bgW");
			bgSizeY = nbt.getDouble("bgH");
			bgColor = nbt.getLong("bgC");
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

		private static ResourceLocation bgL = new ResourceLocation(DarkUtilsMod.modName,"textures/blank.png");
		@Override
		public void renderBlock(Tessellator tess, TileEntity te, int x, int y, int z)
		{
			if(!(te instanceof CCProjectorTE)) return;
			CCProjectorTE pr = (CCProjectorTE) te;
			if(!pr.enabled) return;
			if(pr.text == null) return;
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_LIGHTING);
			boolean reen = GL11.glIsEnabled(GL11.GL_CULL_FACE);
			if(pr.showBack)
				GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glTranslated(pr.offsetX, pr.offsetY+1, pr.offsetZ);
			GL11.glRotated(pr.angle, 0, 1, 0);
			GL11.glRotated(180, 0, 0, 1);
			GL11.glScaled(0.025, 0.025, 0.025);
			GL11.glScaled(pr.scale, pr.scale, pr.scale);
			GL11.glPushMatrix();
			double bgSizeMult = 40;
			boolean useBG = pr.bgEnabled && (pr.bgSizeX > 0.2) && (pr.bgSizeY > 0.2);
			List list = fr.listFormattedStringToWidth(pr.text, useBG ? (int)(pr.bgSizeX * bgSizeMult) : Integer.MAX_VALUE);
			int i = 0;
			for(Object o : list)
			{
				String s = (String) o;
				fr.drawString(s, 0, 10*i++, pr.color, pr.shadow);
			}
			GL11.glPopMatrix();
			if(useBG)
			{
				GL11.glPushMatrix();
				float a = 1-(((pr.bgColor >> 24) & 255) / 255f);
				float r = ((pr.bgColor >> 16) & 255) / 255f;
				float g = ((pr.bgColor >> 8) & 255) / 255f;
				float b = (pr.bgColor & 255) / 255f;
				bindTexture(bgL);
				GL11.glColor4f(r, g, b, a);
				double w = pr.bgSizeX / 0.025;
				double h = pr.bgSizeY / 0.025;
				double xn = pr.bgOffsetX / 0.025;
				double yn = pr.bgOffsetY / 0.025;
				tess.startDrawingQuads();
				tess.addVertexWithUV(-xn, -yn, 0.01, 0, 1);
				tess.addVertexWithUV(xn+w, -yn, 0.01, 1, 1);
				tess.addVertexWithUV(xn+w, yn+h, 0.01, 1, 0);
				tess.addVertexWithUV(-xn, yn+h, 0.01, 0, 0);
				tess.draw();
				GL11.glPopMatrix();
			}
			if(pr.showBack && reen)
				GL11.glEnable(GL11.GL_CULL_FACE);
			if(light)
				GL11.glEnable(GL11.GL_LIGHTING);
		}

	}
}
