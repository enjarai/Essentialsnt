package nl.enjarai.essentialsnt.config

import nl.enjarai.essentialsnt.Essentialsnt.CONFIG_DIR
import java.io.File

class MessagesConfig : ModConfig {
    var spawn = "<dark_aqua>Teleporting to spawn."
    var spawn_set = "<dark_aqua>Spawn set to current location."
    var spawn_delete = "<dark_aqua>Spawn was deleted."
    var spawn_not_exists = "<red>No spawn is set."
    var warp = "<dark_aqua>Teleporting to <yellow>\${name}</yellow>."
    var warp_set = "<dark_aqua>Warp <yellow>\${name}</yellow> set to current location."
    var warp_delete = "<dark_aqua>Warp <yellow>\${name}</yellow> deleted."
    var warp_not_exists = "<red>That warp doesn't exist."
    var warps = "<dark_aqua>You have access to the following warps:"
    var warps_none = "<dark_aqua>You don't have access to any warps."
    var wild = "<dark_aqua>Teleporting you to a random location."
    var wild_cooldown = "<red>You cant use /wild right now, please wait 10 minutes."
    var wild_error = "<red>Error, can't find a valid location to teleport to."
    var msg = "\${sender} <dark_aqua>-></dark_aqua> \${receiver} <dark_gray>»</dark_gray> <white>\${message}"
    var msg_send = "<dark_aqua>you -></dark_aqua> \${receiver} <dark_gray>»</dark_gray> <white>\${message}"
    var msg_receive = "\${sender} <dark_aqua>-> you</dark_aqua> <dark_gray>»</dark_gray> <white>\${message}"
    var msg_no_player = "<red>Can't find that player."
    var socialspy_enable = "<dark_aqua>Socialspy is now <green>enabled</green>."
    var socialspy_disable = "<dark_aqua>Socialspy is now <red>disabled</red>."
    var teleporting = "<dark_aqua>Teleporting..."
    var teleporting_wait = "<dark_aqua>Teleportation commencing in <yellow>\${duration}</yellow> seconds. Please wait..."
    var moved = "<red>You moved, teleportation cancelled."
    var vote_none = "<red>There is no vote in progress."
    var vote_in_progress = "<red>There is already a vote in progress."
    var vote_start = "<dark_aqua><yellow>\${sender}</yellow> started a vote for <yellow>\${type}</yellow>.\n" +
            "Click here to vote <run_cmd:'/vote yes'><green>[Yes]</green></run_cmd> or <run_cmd:'/vote no'><red>[No]</red></run_cmd>."
    var vote_cast = "<dark_aqua>You voted \${vote}."
    var vote_already_voted = "<red>You already voted."
    var vote_fail = "<dark_aqua>Time vote failed."
    var vote_success = "<dark_aqua>Time vote succeeded."

    override fun getConfigFile(): File {
        return CONFIG_DIR.resolve("messages.json").toFile()
    }
}