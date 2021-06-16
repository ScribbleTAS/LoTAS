package de.pfannekuchen.lotas.core.utils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

public enum RenderUtils
{
	;
	
	private static final Box DEFAULT_AABB = new Box(0, 0, 0, 1, 1, 1);
	
	
	public static void applyRenderOffset()
	{
		Vec3d camPos = getCameraPos();
		GL11.glTranslated(-camPos.x, -camPos.y, -camPos.z);
	}
	
	public static void applyRegionalRenderOffset()
	{
		applyCameraRotationOnly();
		
		Vec3d camPos = getCameraPos();
		BlockPos blockPos = getCameraBlockPos();
		
		int regionX = (blockPos.getX() >> 9) * 512;
		int regionZ = (blockPos.getZ() >> 9) * 512;
		
		GL11.glTranslated(regionX - camPos.x, -camPos.y, regionZ - camPos.z);
	}
	
	public static void applyRegionalRenderOffset(Chunk chunk)
	{
		applyCameraRotationOnly();
		
		Vec3d camPos = getCameraPos();
		
		int regionX = (chunk.getPos().getStartX() >> 9) * 512;
		int regionZ = (chunk.getPos().getStartZ() >> 9) * 512;
		
		GL11.glTranslated(regionX - camPos.x, -camPos.y, regionZ - camPos.z);
	}
	
	public static void applyCameraRotationOnly()
	{
		Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
		GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1, 0, 0);
		GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0), 0, 1,
			0);
	}
	
	public static Vec3d getCameraPos()
	{
		return BlockEntityRenderDispatcher.INSTANCE.camera.getPos();
	}
	
	public static BlockPos getCameraBlockPos()
	{
		return BlockEntityRenderDispatcher.INSTANCE.camera.getBlockPos();
	}
	
	public static void drawSolidBox()
	{
		drawSolidBox(DEFAULT_AABB);
	}
	
	public static void drawSolidBox(Box bb)
	{
		GL11.glBegin(GL11.GL_QUADS);
		//#if MC>=11601
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
		//#else
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		//#endif
		GL11.glEnd();
	}
	
	public static void drawOutlinedBox()
	{
		drawOutlinedBox(DEFAULT_AABB);
	}
	
	public static void drawOutlinedBox(Box bb)
	{
		GL11.glBegin(GL11.GL_LINES);
		//#if MC>=11601
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
		//#else
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		//#endif
		GL11.glEnd();
	}
	
	public static void drawCrossBox()
	{
		drawCrossBox(DEFAULT_AABB);
	}
	
	public static void drawCrossBox(Box bb)
	{
		GL11.glBegin(GL11.GL_LINES);
		//#if MC>=11601
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
		//#else
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);

		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		//#endif
		GL11.glEnd();
	}
	
	public static void drawNode(Box bb)
	{
		//#if MC>=11601
//$$ 		double midX = (bb.minX + bb.maxX) / 2;
//$$ 		double midY = (bb.minY + bb.maxY) / 2;
//$$ 		double midZ = (bb.minZ + bb.maxZ) / 2;
//$$
//$$ 		GL11.glVertex3d(midX, midY, bb.maxZ);
//$$ 		GL11.glVertex3d(bb.minX, midY, midZ);
//$$
//$$ 		GL11.glVertex3d(bb.minX, midY, midZ);
//$$ 		GL11.glVertex3d(midX, midY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(midX, midY, bb.minZ);
//$$ 		GL11.glVertex3d(bb.maxX, midY, midZ);
//$$
//$$ 		GL11.glVertex3d(bb.maxX, midY, midZ);
//$$ 		GL11.glVertex3d(midX, midY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.maxY, midZ);
//$$ 		GL11.glVertex3d(bb.maxX, midY, midZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.maxY, midZ);
//$$ 		GL11.glVertex3d(bb.minX, midY, midZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.maxY, midZ);
//$$ 		GL11.glVertex3d(midX, midY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.maxY, midZ);
//$$ 		GL11.glVertex3d(midX, midY, bb.maxZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.minY, midZ);
//$$ 		GL11.glVertex3d(bb.maxX, midY, midZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.minY, midZ);
//$$ 		GL11.glVertex3d(bb.minX, midY, midZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.minY, midZ);
//$$ 		GL11.glVertex3d(midX, midY, bb.minZ);
//$$
//$$ 		GL11.glVertex3d(midX, bb.minY, midZ);
//$$ 		GL11.glVertex3d(midX, midY, bb.maxZ);
		//#else
		double midX = (bb.x1 + bb.x2) / 2;
		double midY = (bb.y1 + bb.y2) / 2;
		double midZ = (bb.z1 + bb.z2) / 2;
		GL11.glVertex3d(midX, midY, bb.z2);
		GL11.glVertex3d(bb.x1, midY, midZ);

		GL11.glVertex3d(bb.x1, midY, midZ);
		GL11.glVertex3d(midX, midY, bb.z1);

		GL11.glVertex3d(midX, midY, bb.z1);
		GL11.glVertex3d(bb.x2, midY, midZ);

		GL11.glVertex3d(bb.x2, midY, midZ);
		GL11.glVertex3d(midX, midY, bb.z2);

		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(bb.x2, midY, midZ);

		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(bb.x1, midY, midZ);

		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(midX, midY, bb.z1);

		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(midX, midY, bb.z2);

		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(bb.x2, midY, midZ);

		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(bb.x1, midY, midZ);

		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(midX, midY, bb.z1);

		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(midX, midY, bb.z2);
		//#endif
	}
	
	public static void drawArrow(Vec3d from, Vec3d to)
	{
		double startX = from.x;
		double startY = from.y;
		double startZ = from.z;
		
		double endX = to.x;
		double endY = to.y;
		double endZ = to.z;
		
		GL11.glPushMatrix();
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(startX, startY, startZ);
		GL11.glVertex3d(endX, endY, endZ);
		GL11.glEnd();
		
		GL11.glTranslated(endX, endY, endZ);
		GL11.glScaled(0.1, 0.1, 0.1);
		
		double angleX = Math.atan2(endY - startY, startZ - endZ);
		GL11.glRotated(Math.toDegrees(angleX) + 90, 1, 0, 0);
		
		double angleZ = Math.atan2(endX - startX,
			Math.sqrt(Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2)));
		GL11.glRotated(Math.toDegrees(angleZ), 0, 0, 1);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(0, 2, 1);
		GL11.glVertex3d(-1, 2, 0);
		
		GL11.glVertex3d(-1, 2, 0);
		GL11.glVertex3d(0, 2, -1);
		
		GL11.glVertex3d(0, 2, -1);
		GL11.glVertex3d(1, 2, 0);
		
		GL11.glVertex3d(1, 2, 0);
		GL11.glVertex3d(0, 2, 1);
		
		GL11.glVertex3d(1, 2, 0);
		GL11.glVertex3d(-1, 2, 0);
		
		GL11.glVertex3d(0, 2, 1);
		GL11.glVertex3d(0, 2, -1);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(1, 2, 0);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(-1, 2, 0);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(0, 2, -1);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(0, 2, 1);
		GL11.glEnd();
		
		GL11.glPopMatrix();
	}
}