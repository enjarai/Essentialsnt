package nl.enjarai.essentialsnt.util

import java.util.*

class Cooldown(private var durationProvider: () -> Int) {
    private val lastTriggered = HashMap<UUID, Long>()

    fun trigger(uuid: UUID): Boolean {
        val currentTime = System.currentTimeMillis()
        if (check(uuid)) {
            lastTriggered[uuid] = currentTime
            return true
        }
        return false
    }

    fun resetCooldown(uuid: UUID) {
        lastTriggered.remove(uuid)
    }

    fun touch(uuid: UUID) {
        lastTriggered[uuid] = System.currentTimeMillis()
    }

    fun check(uuid: UUID): Boolean {
        val triggered = lastTriggered[uuid]
        val currentTime = System.currentTimeMillis()
        return (triggered == null) || (triggered < (currentTime - durationProvider()))
    }
}