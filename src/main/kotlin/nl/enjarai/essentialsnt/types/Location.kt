package nl.enjarai.essentialsnt.types

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import nl.enjarai.essentialsnt.Essentialsnt.SERVER
import kotlin.math.floor

class Location(pos: Vec3d, dim: ServerWorld) {
    var position: Array<Int>
    var dimension: String

    init {
        position = arrayOf(floor(pos.x).toInt(), floor(pos.y).toInt(), floor(pos.z).toInt())
        dimension = dim.registryKey.value.toString()
    }

    fun teleportHere(player: ServerPlayerEntity) {
        val dim: ServerWorld =
            SERVER.getWorld(RegistryKey.of(Registry.WORLD_KEY, Identifier.tryParse(dimension))) ?: return
        player.teleport(
            dim, position[0] + 0.5, position[1].toDouble(), position[2] + 0.5,
            player.yaw, player.pitch
        )
    }
}