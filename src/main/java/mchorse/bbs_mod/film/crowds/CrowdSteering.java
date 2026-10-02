package mchorse.bbs_mod.film.crowds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CrowdSteering
{
    public static Vec3d getAllowedMotion(ServerWorld world, LivingEntity entity, Vec3d desired, double maxStepHeight, boolean autoJump, boolean flip)
    {
        if (desired.lengthSquared() < 1.0E-10D)
        {
            return Vec3d.ZERO;
        }

        if (canMove(world, entity, desired, maxStepHeight))
        {
            return desired;
        }

        for (int i = 7; i >= 1; i--)
        {
            Vec3d scaled = desired.multiply(i / 8D);
            if (canMove(world, entity, scaled, maxStepHeight))
            {
                return scaled;
            }
        }

        Vec3d xOnly = new Vec3d(desired.x, 0D, 0D);
        Vec3d zOnly = new Vec3d(0D, 0D, desired.z);
        boolean canX = Math.abs(desired.x) > 1.0E-5D && canMove(world, entity, xOnly, maxStepHeight);
        boolean canZ = Math.abs(desired.z) > 1.0E-5D && canMove(world, entity, zOnly, maxStepHeight);

        if (canX && canZ)
        {
            return Math.abs(desired.x) > Math.abs(desired.z) ? xOnly : zOnly;
        }
        if (canX) return xOnly;
        if (canZ) return zOnly;

        if (autoJump && tryAutoJump(world, entity, desired))
        {
            return desired;
        }

        double[] angles = flip
            ? new double[] {25D, -25D, 45D, -45D, 70D, -70D, 105D, -105D, 150D}
            : new double[] {-25D, 25D, -45D, 45D, -70D, 70D, -105D, 105D, -150D};

        for (double angle : angles)
        {
            Vec3d rotated = rotateXZ(desired, angle);
            if (canMove(world, entity, rotated, maxStepHeight))
            {
                return rotated;
            }
        }

        return Vec3d.ZERO;
    }

    private static boolean tryAutoJump(ServerWorld world, LivingEntity entity, Vec3d motion)
    {
        if (!entity.isOnGround())
        {
            return false;
        }

        Vec3d up = new Vec3d(0D, 1.2D, 0D);
        if (!canMove(world, entity, up, 0D))
        {
            return false;
        }

        Vec3d over = up.add(motion);
        if (canMove(world, entity, over, 0D))
        {
            entity.setVelocity(entity.getVelocity().x, 0.5D, entity.getVelocity().z);
            return true;
        }

        return false;
    }

    private static boolean canMove(ServerWorld world, LivingEntity entity, Vec3d motion, double maxStepHeight)
    {
        Box box = entity.getBoundingBox().offset(motion.x, 0D, motion.z);
        if (world.isSpaceEmpty(null, box))
        {
            return true;
        }

        double maxStep = Math.max(0D, Math.min(1.0D, maxStepHeight));
        if (maxStep <= 0D || !entity.isOnGround())
        {
            return false;
        }

        double[] lifts = new double[] {0.0625D, 0.125D, 0.25D, 0.375D, 0.5D, 0.6D, 1.0D, maxStep};
        for (double lift : lifts)
        {
            if (lift <= 0D || lift > maxStep + 1.0E-5D) continue;

            Box lifted = box.offset(0D, lift, 0D);
            if (world.isSpaceEmpty(null, lifted))
            {
                return true;
            }
        }

        return false;
    }

    private static Vec3d rotateXZ(Vec3d vector, double degrees)
    {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3d(vector.x * cos - vector.z * sin, vector.y, vector.x * sin + vector.z * cos);
    }
}
