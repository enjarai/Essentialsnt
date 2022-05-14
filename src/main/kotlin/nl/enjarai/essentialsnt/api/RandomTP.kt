package nl.enjarai.essentialsnt.api

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.essentialsnt.Essentialsnt.GENERAL_CONFIG
import nl.enjarai.essentialsnt.Essentialsnt.LOGGER
import nl.enjarai.essentialsnt.types.Location
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

object RandomTP {
    fun randomTeleport(player: ServerPlayerEntity, world: ServerWorld, minDistance: Int, maxDistance: Int) {
        randomTeleport(player, world, minDistance, maxDistance, object : DelayedTP.TPCallback {})
    }

    fun randomTeleport(
        player: ServerPlayerEntity,
        world: ServerWorld,
        minDistance: Int,
        maxDistance: Int,
        callback: DelayedTP.TPCallback
    ) {
        try {
            val r = Random()
            val lowX = (abs(player.x).roundToInt() + minDistance) * -1
            var highX = abs(player.x.roundToInt() + maxDistance)
            val lowZ = (abs(player.z).roundToInt() + minDistance) * -1
            var highZ = abs(player.z.roundToInt() + maxDistance)
            if (maxDistance == 0) {
                highX = (world.worldBorder.size / 2).toInt()
                highZ = (world.worldBorder.size / 2).toInt()
            }
            var x = r.nextInt(highX - lowX) + lowX
            var y = 50
            var z = r.nextInt(highZ - lowZ) + lowZ
            var maxTries = 700 // fix here if that error shows up lmao
            while (!isSafe(world, x, y, z) && (maxTries == -1 || maxTries > 0)) {
                y++
                if (y >= 120) {
                    x = r.nextInt(highX - lowX) + lowX
                    y = 50
                    z = r.nextInt(highZ - lowZ) + lowZ
                    continue
                }
                if (maxTries > 0) {
                    maxTries--
                }
                if (maxTries == 0) {
                    throw NoValidLocationException()
                }
            }
            DelayedTP.delayedTeleport(
                player, Location(
                    Vec3d(
                        x.toDouble(), y.toDouble(),
                        z.toDouble()
                    ), world
                ), GENERAL_CONFIG.teleport_delay, callback
            )
        } catch (ex: Exception) {
            LOGGER.info("Error executing command.")
            ex.printStackTrace()
        }
    }

    private fun isSafe(world: ServerWorld, newX: Int, newY: Int, newZ: Int): Boolean {
        return if (newX >= world.worldBorder.boundEast || newZ >= world.worldBorder.boundSouth) false else isEmpty(
            world,
            newX,
            newY,
            newZ
        ) &&
                !isDangerBlock(world, newX, newY - 1, newZ)
    }

    private fun isEmpty(world: World, newX: Int, newY: Int, newZ: Int): Boolean {
        return world.isAir(BlockPos(newX, newY, newZ)) && world.isAir(BlockPos(newX, newY + 1, newZ)) &&
                world.isAir(BlockPos(newX + 1, newY, newZ)) && world.isAir(BlockPos(newX - 1, newY, newZ)) &&
                world.isAir(BlockPos(newX, newY, newZ + 1)) && world.isAir(BlockPos(newX, newY, newZ - 1))
    }

    private fun isDangerBlock(world: World, newX: Int, newY: Int, newZ: Int): Boolean {
        for (block in getDangerBlocks()) {
            if (block == world.getBlockState(BlockPos(newX, newY, newZ)).block) {
                return true
            }
        }
        return false
    }

    private fun getDangerBlocks(): Array<Block> {
        return arrayOf(Blocks.LAVA, Blocks.WATER, Blocks.AIR)
    }

    class NoValidLocationException : Throwable()
}