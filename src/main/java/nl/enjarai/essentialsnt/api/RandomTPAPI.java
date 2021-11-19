package nl.enjarai.essentialsnt.api;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.enjarai.essentialsnt.Essentialsnt;
import nl.enjarai.essentialsnt.types.Location;

import java.util.Random;

import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;

public class RandomTPAPI {
    public static void randomTeleport(ServerPlayerEntity player, ServerWorld world, int minDistance, int maxDistance) throws NoValidLocationException {
        try  {
            Random r = new Random();
            int lowX = ((int)Math.round(Math.abs(player.getX())) + minDistance) * -1;
            int highX = Math.abs((int)Math.round(player.getX()) + maxDistance);
            int lowZ = ((int)Math.round(Math.abs(player.getZ())) + minDistance) * -1;
            int highZ = Math.abs((int)Math.round(player.getZ()) + maxDistance);
            if(maxDistance == 0) {
                highX = (int) (world.getWorldBorder().getSize() / 2);
                highZ = (int) (world.getWorldBorder().getSize() / 2);
            }
            int x = r.nextInt(highX-lowX) + lowX;
            int y = 50;
            int z = r.nextInt(highZ-lowZ) + lowZ;
            int maxTries = 700; // fix here if that error shows up lmao
            while (!isSafe(world, x, y, z) && (maxTries == -1 || maxTries > 0)) {
                y++;
                if(y >= 120) {
                    x = r.nextInt(highX-lowX) + lowX;
                    y = 50;
                    z = r.nextInt(highZ-lowZ) + lowZ;
                    continue;
                }
                if(maxTries > 0){
                    maxTries--;
                }
                if(maxTries == 0) {
                    throw new NoValidLocationException();
                }
            }

            DelayedTPAPI.delayedTeleport(player, new Location(new Vec3d(x, y, z), world), CONFIG.teleport_delay);
        } catch(Exception ex) {
            Essentialsnt.LOGGER.info("Error executing command.");
            ex.printStackTrace();
        }
    }

    public static boolean isSafe(ServerWorld world, int newX, int newY, int newZ) {
        if(newX >= world.getWorldBorder().getBoundEast() || newZ >= world.getWorldBorder().getBoundSouth()) return false;
        return (isEmpty(world, newX, newY, newZ)) &&
                (!isDangerBlock(world, newX, newY - 1, newZ));
    }

    public static boolean isEmpty(World world, int newX, int newY, int newZ) {
        return (world.isAir(new BlockPos(newX, newY, newZ))) && (world.isAir(new BlockPos(newX, newY + 1, newZ))) &&
                (world.isAir(new BlockPos(newX + 1, newY, newZ))) && (world.isAir(new BlockPos(newX - 1, newY, newZ))) &&
                (world.isAir(new BlockPos(newX, newY, newZ + 1))) && (world.isAir(new BlockPos(newX, newY, newZ - 1)));
    }

    public static boolean isDangerBlock(World world, int newX, int newY, int newZ) {
        for (Block block : getDangerBlocks()) {
            if (block.equals(world.getBlockState(new BlockPos(newX, newY, newZ)).getBlock())) {
                return true;
            }
        }
        return false;
    }

    public static Block[] getDangerBlocks() {
        return new Block[] {Blocks.LAVA, Blocks.WATER, Blocks.AIR};
    }

    public static class NoValidLocationException extends Throwable { }
}
