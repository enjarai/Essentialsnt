package nl.enjarai.essentialsnt;

import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nl.enjarai.essentialsnt.types.Location;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static nl.enjarai.essentialsnt.Essentialsnt.CONFIG;

public class TeleportManager {
    public static void delayedTeleport(ServerPlayerEntity player, Location destination, Integer seconds) {
        Vec3d oldPos = player.getPos();

        if (Permissions.check(player, "essentialsnt.bypass.tpdelay", false)) {
            destination.teleportHere(player);
            return;
        }

        HashMap<String, Text> placeholders = new HashMap<>();
        placeholders.put("duration", new LiteralText(seconds.toString()));
        player.sendMessage(PlaceholderAPI.parsePredefinedText(
                TextParser.parse(CONFIG.messages.teleporting_wait),
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholders
        ), true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (oldPos.isInRange(player.getPos(), 0.1)) {
                    player.sendMessage(TextParser.parse(CONFIG.messages.teleporting), true);

                    destination.teleportHere(player);
                } else {
                    player.sendMessage(TextParser.parse(CONFIG.messages.moved), true);
                }
            }
        }, seconds * 1000L);
    }
}
