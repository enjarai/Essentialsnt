
<p><a title="Fabric Language Kotlin" href="https://minecraft.curseforge.com/projects/fabric-language-kotlin" target="_blank" rel="noopener noreferrer"><img style="display: block; margin-left: auto; margin-right: auto;" src="https://i.imgur.com/c1DH9VL.png" alt="" width="171" height="50" /></a></p>

# Essentialsn't

A Fabric server mod to handle some of the same stuff as the Essentials plugin,
but without all the bloat. Designed for semi-vanilla SMP servers.

## Commands

As is the most prominent feature in Essentials, this mod provides a bunch of
commands to effectively manage an SMP.

### Management

`/essentialsnt reload`: Reload mod configs
- Permission node: `essentialsnt.commands.essentialsnt`
- Default: `false`

### Messaging

`/msg <player> <message>`: Replacement for the vanilla `/msg` command
- Permission node: `essentialsnt.commands.msg`
- Default: `true`

`/r <message>`: Reply to the most recently sent/received direct message
- Permission node: `essentialsnt.commands.r`
- Default: `true`

`/socialspy`: Toggle snooping in on direct messages
- Permission node: `essentialsnt.commands.socialspy`
- Default: `false`

`/reply <message>`: Alias to `/r`

`/ss`: Alias to `/socialspy`

`/tell <player> <message>`: Alias to `/msg`

`/w <player> <message>`: Alias to `/msg`

### Teleportation

`/spawn`: Teleport to spawn
- Permission node: `essentialsnt.commands.spawn`
- Default: `true`

`/spawn set`: Set a new spawn
- Permission node: `essentialsnt.commands.spawn.set`
- Default: `false`

`/warp <warp>`: Warp to the specified warp point
- Permission node: `essentialsnt.commands.warp`
- Default: `true`

`/warp <warp> set`: Set the location for a new or existing warp
- Permission node: `essentialsnt.commands.warp.set`
- Default: `false`

`/warp <warp> delete`: Delete an existing warp
- Permission node: `essentialsnt.commands.warp.delete`
- Default: `false`

`/warps`: List warps available to the player
- Permission node: `essentialsnt.commands.warps`
- Default: `true`

`/wild`: Teleport to a random location in the world
- Permission node: `essentialsnt.commands.wild`
- Default: `true`

`/stp`: Alias to `/spawn`

`/setspawn`: Alias to `/spawn set`

`/wtp`: Alias to `/warp`

`/setwarp`: Alias to `/warp set`

`/delwarp`: Alias to `/warp delete`

`/listwarps`: Alias to `/warps`

## Configuration

All commands can be enabled and disabled with the use of permission nodes.
Specific values can be changed in the 
config file located at `config/essentialsnt/general.json`.
Formatting for all chat responses can be modified in `messages.json`
in the same folder

## API

The mod also provides some simple APIs for other mods to use,
these are found in the `DelayedTP`, `RandomTP` and `SocialSpy` classes
