package nl.enjarai.essentialsnt.config

import nl.enjarai.essentialsnt.Essentialsnt.CONFIG_DIR
import nl.enjarai.essentialsnt.types.Location
import java.io.File
import java.util.*

class GeneralConfig : ModConfig {
    var teleport_delay = 3
    var wild_cooldown = 600
    var wild_min_range = 200
    var wild_max_range = 2000

    var spawn: Location? = null

    var warps = HashMap<String, Location>()

    var socialspy_enabled = HashSet<UUID>()

    override fun getConfigFile(): File {
        return CONFIG_DIR.resolve("general.json").toFile()
    }
}