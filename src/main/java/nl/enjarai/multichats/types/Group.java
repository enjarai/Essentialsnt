package nl.enjarai.multichats.types;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import nl.enjarai.multichats.Helpers;
import nl.enjarai.multichats.MultiChats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static nl.enjarai.multichats.MultiChats.CONFIG;
import static nl.enjarai.multichats.MultiChats.DATABASE;

public class Group {
    public final int id;
    public String name;
    public Text displayName;
    public Text displayNameShort;

    public String prefix = null;
    public String formatOverride = null;
    public Vec3d homePos = null;
    public String homeDimension = null;

    public Group(int id, String name) {
        this.id = id;
        this.name = name;
        this.displayName = new LiteralText(name);
        this.displayNameShort = new LiteralText(name);
    }

    // Statics

    public static Group create(String name) {
        return DATABASE.createGroup(name);
    }

    public static Group get(int id) {
        return DATABASE.getGroup(id);
    }

    public static Group get(String name) {
        return DATABASE.getGroup(name);
    }

    public static List<Group> getMemberships(UUID uuid) {
        return DATABASE.getGroups(uuid);
    }

    public static List<Group> getMemberships(UUID uuid, GroupPermissionLevel permissionLevel) {
        return getMemberships(uuid, permissionLevel, false);
    }

    public static List<Group> getMemberships(UUID uuid, GroupPermissionLevel permissionLevel, boolean exact) {
        return DATABASE.getGroups(uuid, permissionLevel, exact);
    }

    public static List<Group> all() {
        return DATABASE.getGroups();
    }

    public static List<String> getMembershipNames(UUID uuid) {
        return DATABASE.getGroupNames(uuid);
    }

    public static List<String> getMembershipNames(UUID uuid, GroupPermissionLevel permissionLevel) {
        return getMembershipNames(uuid, permissionLevel, false);
    }

    public static List<String> getMembershipNames(UUID uuid, GroupPermissionLevel permissionLevel, boolean exact) {
        return DATABASE.getGroupNames(uuid, permissionLevel, exact);
    }

    public static List<String> allNames() {
        return DATABASE.getGroupNames();
    }

    public static Group getPrimaryMembership(UUID uuid) {
        return DATABASE.getPrimaryGroup(uuid);
    }

    public static boolean setPrimary(UUID uuid, Group group) {
        boolean result = DATABASE.changePrimaryGroup(uuid, group);
        Helpers.updatePlayerListEntry(uuid);
        return result;
    }

    public static Group getFromDbResult(ResultSet result) throws SQLException {
        Group group = new Group(result.getInt("id"), result.getString("name"));

        group.displayName = Text.Serializer.fromJson(result.getString("displayName"));
        group.displayNameShort = Text.Serializer.fromJson(result.getString("displayNameShort"));
        group.prefix = result.getString("prefix");
        group.setHome(result.getInt("tpX"), result.getInt("tpY"), result.getInt("tpZ"),
                result.getString("tpDimension"));

        return group;
    }

    // Dynamics

    // Getting values
    
    public String getFormat() {
        return formatOverride == null ? MultiChats.CONFIG.chatFormat : formatOverride;
    }
    
    // Operations

    public void setHome(Vec3d pos, String dimension) {
        homePos = pos;
        homeDimension = dimension;
    }

    public void setHome(int x, int y, int z, String dimension) {
        if (Helpers.areAllEqual(0, x, y, z)) {
            setHome(null, null);
        } else {
            setHome(new Vec3d(x, y, z), dimension);
        }
    }

    public ServerWorld getHomeDim() {
        return MultiChats.SERVER.getWorld(RegistryKey.of(Registry.WORLD_KEY, Identifier.tryParse(homeDimension)));
    }

    public boolean eligibleForHome() {
        return getPrimaryMembers().size() >= CONFIG.membersRequiredForHome;
    }


    public boolean save() {
        return MultiChats.DATABASE.saveGroup(this);
    }

    public boolean delete() {
        return MultiChats.DATABASE.deleteGroup(this);
    }

    public Group refresh() {
        return MultiChats.DATABASE.getGroup(name);
    }

    // Getting members

    public boolean checkAccess(UUID uuid) {
        return MultiChats.DATABASE.getPermissionLevel(this, uuid) != null;
    }

    public boolean checkAccess(UUID uuid, GroupPermissionLevel permissionLevel) {
        GroupPermissionLevel playerLevel = MultiChats.DATABASE.getPermissionLevel(this, uuid);
        return playerLevel != null &&
                playerLevel.dbInt >= permissionLevel.dbInt;
    }

    public boolean checkPrimary(UUID uuid) {
        return MultiChats.DATABASE.checkPrimary(this, uuid);
    }


    public HashMap<UUID, GroupPermissionLevel> getMembers() {
        return MultiChats.DATABASE.getMembers(this);
    }

    public HashMap<UUID, GroupPermissionLevel> getMembers(GroupPermissionLevel permissionLevel) {
        return getMembers(permissionLevel, false);
    }

    public HashMap<UUID, GroupPermissionLevel> getMembers(GroupPermissionLevel permissionLevel, boolean exact) {
        return MultiChats.DATABASE.getMembers(this, permissionLevel, exact);
    }

    public HashMap<UUID, GroupPermissionLevel> getPrimaryMembers() {
        return MultiChats.DATABASE.getPrimaryMembers(this);
    }

    public HashMap<UUID, GroupPermissionLevel> getNonPrimaryMembers() {
        return MultiChats.DATABASE.getNonPrimaryMembers(this);
    }

    // Managing members

    public boolean addMember(UUID uuid) {
        return addMember(uuid, GroupPermissionLevel.MEMBER, false);
    }

    public boolean addMember(UUID uuid, boolean makePrimary) {
        return addMember(uuid, GroupPermissionLevel.MEMBER, makePrimary);
    }

    public boolean addMember(UUID uuid, GroupPermissionLevel permissionLevel) {
        return addMember(uuid, permissionLevel, this.checkPrimary(uuid));
    }

    public boolean addMember(UUID uuid, GroupPermissionLevel permissionLevel, boolean makePrimary) {
        return MultiChats.DATABASE.addUserToGroup(uuid, this, makePrimary, permissionLevel);
    }

    public boolean removeMember(UUID uuid) {
        return MultiChats.DATABASE.removeUserFromGroup(uuid, this);
    }

    public boolean changeOwner(UUID uuid) {
        return MultiChats.DATABASE.changeGroupOwner(this, uuid);
    }
}
