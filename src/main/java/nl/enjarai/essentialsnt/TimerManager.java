package nl.enjarai.essentialsnt;

import java.util.HashMap;
import java.util.UUID;

public class TimerManager {
    private final HashMap<UUID, Long> lastTriggered = new HashMap<>();
    private final long duration;

    public TimerManager(int durationSecs) {
        this.duration = durationSecs * 1000L;
    }

    public boolean trigger(UUID uuid) {
        Long triggered = lastTriggered.get(uuid);
        long currentTime = System.currentTimeMillis();

        if (triggered == null || triggered < currentTime - duration) {
            lastTriggered.put(uuid, currentTime);
            return true;
        }
        return false;
    }

    public void resetCooldown(UUID uuid) {
        lastTriggered.remove(uuid);
    }

    public void touch(UUID uuid) {
        lastTriggered.put(uuid, System.currentTimeMillis());
    }

    public boolean check(UUID uuid) {
        Long triggered = lastTriggered.get(uuid);
        long currentTime = System.currentTimeMillis();

        return triggered == null || triggered < currentTime - duration;
    }
}
