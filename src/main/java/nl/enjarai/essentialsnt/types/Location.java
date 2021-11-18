package nl.enjarai.essentialsnt.types;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import static nl.enjarai.essentialsnt.Essentialsnt.SERVER;

public class Location {
    public int[] position;
    public String dimension = "";

    public Location(Vec3d pos, ServerWorld dim) {
        position = new int[]{(int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)};
        dimension = dim.getRegistryKey().getValue().toString();
    }

    public void teleportHere(ServerPlayerEntity player) {
        ServerWorld dim = SERVER.getWorld(RegistryKey.of(Registry.WORLD_KEY, Identifier.tryParse(dimension)));
        player.teleport(dim, position[0] + 0.5, position[1], position[2] + 0.5,
                player.getYaw(), player.getPitch());
    }
}
