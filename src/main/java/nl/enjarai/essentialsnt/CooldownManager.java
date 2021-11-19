package nl.enjarai.essentialsnt;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    private final HashMap<UUID, Long> timers = new HashMap<>();
    public final long duration;

    public CooldownManager(long duration) {
        this.duration = duration * 1000;
    }

    public boolean checkAndTouch(UUID uuid) { // TODO: fix
        long time = System.currentTimeMillis();

        if (timers.get(uuid) != null && timers.get(uuid) + duration < time) {
            return false;
        }
        timers.put(uuid, time);
        return true;
    }

    public void resetCooldown(UUID uuid) {
        timers.remove(uuid);
    }
}
