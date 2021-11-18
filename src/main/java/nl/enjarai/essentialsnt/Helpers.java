package nl.enjarai.essentialsnt;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.essentialsnt.types.Location;

import java.util.HashMap;
import java.util.Map;

import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;

public class Helpers {
    public static HashMap<String, Location> getAccessibleWarps(ServerPlayerEntity player) {
        HashMap<String, Location> result = new HashMap<>();
        for (Map.Entry<String, Location> warp : CONFIG.warps.entrySet()) {
            if (Permissions.check(player, "essentialsnt.warps." + warp.getKey(), true)) {
                result.put(warp.getKey(), warp.getValue());
            }
        }
        return result;
    }
}
