package nl.enjarai.multichats.database;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import nl.enjarai.multichats.Helpers;
import nl.enjarai.multichats.types.Group;
import nl.enjarai.multichats.types.GroupPermissionLevel;

import java.sql.*;
import java.util.*;

public abstract class AbstractSQLDatabase implements DatabaseHandlerInterface {
    protected Connection CONNECTION;
    protected Statement STATEMENT;

    // protected ExecutorService SERVICE = Executors.newFixedThreadPool(10);
    // protected DatabaseCache CACHE = new DatabaseCache(600, 2);


    protected abstract String getGroupTableCreation();
    protected abstract String getUserTableCreation();

    public void init() throws SQLException {
        STATEMENT.execute(getGroupTableCreation());
        STATEMENT.execute(getUserTableCreation());
    }

    public void closeConnection() {
        try {
            CONNECTION.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Group management

    @Override
    public Group createGroup(String name) {
        try {
            String jsonName = Text.Serializer.toJson(new LiteralText(name));

            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "INSERT INTO Groups VALUES (NULL, ?, ?, ?, NULL, 0, 0, 0, NULL);");
            prepStmt.setString(1, name);
            prepStmt.setString(2, jsonName);
            prepStmt.setString(3, jsonName);

            prepStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return getGroup(name);
    }

    @Override
    public boolean saveGroup(Group group) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement("""
                    UPDATE
                        Groups
                    SET
                        name=?,
                        displayName=?,
                        displayNameShort=?,
                        prefix=?,
                        tpX=?,
                        tpY=?,
                        tpZ=?,
                        tpDimension=?
                    WHERE
                        id=?
                    ;
                    """);
            prepStmt.setString(1, group.name);
            prepStmt.setString(2, Text.Serializer.toJson(group.displayName));
            prepStmt.setString(3, Text.Serializer.toJson(group.displayNameShort));
            prepStmt.setString(4, group.prefix);
            if (group.homePos == null) {
                prepStmt.setInt(5, 0);
                prepStmt.setInt(6, 0);
                prepStmt.setInt(7, 0);
            } else {
                prepStmt.setInt(5, (int) group.homePos.x);
                prepStmt.setInt(6, (int) group.homePos.y);
                prepStmt.setInt(7, (int) group.homePos.z);
            }
            prepStmt.setString(8, group.homeDimension);

            prepStmt.setInt(9, group.id);

            prepStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteGroup(Group group) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement("DELETE FROM Groups WHERE id=?;");
            prepStmt.setInt(1, group.id);
            prepStmt.executeUpdate();

            // prepStmt = CONNECTION.prepareStatement("DELETE FROM Users WHERE groupId=?;");
            // prepStmt.setString(1, group.id);
            // prepStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<Group> getGroups() {
        List<Group> list = new LinkedList<>();
        try {
            String query = "SELECT * FROM Groups;";
            ResultSet result = STATEMENT.executeQuery(query);
            Group group;

            if (result.next()) {
                do {
                    group = Group.getFromDbResult(result);

                    list.add(group);
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public List<Group> getGroups(UUID uuid) {
        List<Group> list = new LinkedList<>();
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Groups INNER JOIN Users ON Groups.id = Users.groupId WHERE uuid=?;");
            prepStmt.setString(1, uuid.toString());

            ResultSet result = prepStmt.executeQuery();
            Group group;

            if (result.next()) {
                do {
                    group = Group.getFromDbResult(result);

                    list.add(group);
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public List<Group> getGroups(UUID uuid, GroupPermissionLevel permissionLevel, boolean exact) {
        List<Group> list = new LinkedList<>();
        try {
            String operation = exact ? "=" : ">=";

            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Groups INNER JOIN Users ON Groups.id = Users.groupId WHERE uuid=? AND permissionLevel"+operation+"?;");
            prepStmt.setString(1, uuid.toString());
            prepStmt.setInt(2, permissionLevel.dbInt);

            ResultSet result = prepStmt.executeQuery();
            Group group;

            if (result.next()) {
                do {
                    group = Group.getFromDbResult(result);

                    list.add(group);
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public List<String> getGroupNames() {
        List<String> list = new LinkedList<>();
        try {
            String query = "SELECT name FROM Groups;";
            ResultSet result = STATEMENT.executeQuery(query);

            if (result.next()) {
                do {
                    list.add(result.getString("name"));
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public List<String> getGroupNames(UUID uuid) {
        List<String> list = new LinkedList<>();
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT name FROM Groups INNER JOIN Users ON Groups.id = Users.groupId WHERE uuid=?;");
            prepStmt.setString(1, uuid.toString());

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                do {
                    list.add(result.getString("name"));
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public List<String> getGroupNames(UUID uuid, GroupPermissionLevel permissionLevel, boolean exact) {
        List<String> list = new LinkedList<>();
        try {
            String operation = exact ? "=" : ">=";

            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT name FROM Groups INNER JOIN Users ON Groups.id = Users.groupId WHERE uuid=? AND permissionLevel"+operation+"?;");
            prepStmt.setString(1, uuid.toString());
            prepStmt.setInt(2, permissionLevel.dbInt);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                do {
                    list.add(result.getString("name"));
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public Group getGroup(int id) {
        Group group;
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Groups WHERE id=? LIMIT 1;");
            prepStmt.setInt(1, id);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                group = Group.getFromDbResult(result);
            } else {
                group = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return group;
    }

    @Override
    public Group getGroup(String name) {
        Group group;
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Groups WHERE name=? LIMIT 1;");
            prepStmt.setString(1, name);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                group = Group.getFromDbResult(result);
            } else {
                group = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return group;
    }

    @Override
    public boolean changeGroupOwner(Group group, UUID uuid) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement("UPDATE Users SET permissionLevel=? WHERE groupId=? AND permissionLevel=?;");
            prepStmt.setInt(1, GroupPermissionLevel.MANAGER.dbInt);
            prepStmt.setInt(2, group.id);
            prepStmt.setInt(3, GroupPermissionLevel.OWNER.dbInt);
            prepStmt.executeUpdate();

            prepStmt = CONNECTION.prepareStatement("UPDATE Users SET permissionLevel=? WHERE uuid=? AND groupId=?;");
            prepStmt.setInt(1, GroupPermissionLevel.OWNER.dbInt);
            prepStmt.setString(2, uuid.toString());
            prepStmt.setInt(3, group.id);
            prepStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // User management

    @Override
    public Group getPrimaryGroup(UUID uuid) {
        Group group;
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Groups INNER JOIN Users ON Groups.id = Users.groupId WHERE uuid=? AND isPrimary=true LIMIT 1;");
            prepStmt.setString(1, uuid.toString());

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                group = Group.getFromDbResult(result);
            } else {
                group = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return group;
    }

    @Override
    public boolean addUserToGroup(UUID uuid, Group group, boolean primary, GroupPermissionLevel permissionLevel) {
        try {
            Group currentPrimary = getPrimaryGroup(uuid);

            removeUserFromGroup(uuid, group);

            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "INSERT OR REPLACE INTO Users VALUES (NULL, ?, ?, ?, ?);");
            prepStmt.setString(1, uuid.toString());
            prepStmt.setInt(2, group.id);
            prepStmt.setInt(3, permissionLevel.dbInt);
            prepStmt.setBoolean(4, primary);

            prepStmt.executeUpdate();

            // If this user already has a primary group, and it's not this one, fix the duplicate with changePrimaryGroup.
            if (primary && currentPrimary != null && !Objects.equals(currentPrimary.name, group.name)) {
                return changePrimaryGroup(uuid, group);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean removeUserFromGroup(UUID uuid, Group group) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "DELETE FROM Users WHERE uuid=? AND groupId=?;");
            prepStmt.setString(1, uuid.toString());
            prepStmt.setInt(2, group.id);

            prepStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean changePrimaryGroup(UUID uuid, Group group) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement("UPDATE Users SET isPrimary=false WHERE uuid=?;");
            prepStmt.setString(1, uuid.toString());
            prepStmt.executeUpdate();

            if (group != null) {
                prepStmt = CONNECTION.prepareStatement("UPDATE Users SET isPrimary=true WHERE uuid=? AND groupId=?;");
                prepStmt.setString(1, uuid.toString());
                prepStmt.setInt(2, group.id);
                prepStmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkPrimary(Group group, UUID uuid) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Users WHERE uuid=? AND groupId=? AND isPrimary=true LIMIT 1;");
            prepStmt.setString(1, uuid.toString());
            prepStmt.setInt(2, group.id);

            ResultSet result = prepStmt.executeQuery();

            return result.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public GroupPermissionLevel getPermissionLevel(Group group, UUID uuid) {
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT permissionLevel FROM Users WHERE uuid=? AND groupId=? LIMIT 1;");
            prepStmt.setString(1, uuid.toString());
            prepStmt.setInt(2, group.id);

            ResultSet result = prepStmt.executeQuery();

            return result.next() ? Helpers.getPermissionLevelFromInt(result.getInt("permissionLevel")) : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public HashMap<UUID, GroupPermissionLevel> getMembers(Group group) {
        HashMap<UUID, GroupPermissionLevel> map = new HashMap<>();
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Users WHERE groupId=?;");
            prepStmt.setInt(1, group.id);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                do {
                    map.put(
                            UUID.fromString(result.getString("uuid")),
                            Helpers.getPermissionLevelFromInt(result.getInt("permissionLevel"))
                    );
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }

    @Override
    public HashMap<UUID, GroupPermissionLevel> getMembers(Group group, GroupPermissionLevel permissionLevel, boolean exact) {
        HashMap<UUID, GroupPermissionLevel> map = new HashMap<>();
        try {
            String operation = exact ? "=" : ">=";

            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Users WHERE groupId=? AND permissionLevel"+operation+"?;");
            prepStmt.setInt(1, group.id);
            prepStmt.setInt(2, permissionLevel.dbInt);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                do {
                    map.put(
                            UUID.fromString(result.getString("uuid")),
                            Helpers.getPermissionLevelFromInt(result.getInt("permissionLevel"))
                    );
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }

    @Override
    public HashMap<UUID, GroupPermissionLevel> getPrimaryMembers(Group group) {
        HashMap<UUID, GroupPermissionLevel> map = new HashMap<>();
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Users WHERE groupId=? AND isPrimary=true;");
            prepStmt.setInt(1, group.id);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                do {
                    map.put(
                            UUID.fromString(result.getString("uuid")),
                            Helpers.getPermissionLevelFromInt(result.getInt("permissionLevel"))
                    );
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }

    @Override
    public HashMap<UUID, GroupPermissionLevel> getNonPrimaryMembers(Group group) {
        HashMap<UUID, GroupPermissionLevel> map = new HashMap<>();
        try {
            PreparedStatement prepStmt = CONNECTION.prepareStatement(
                    "SELECT * FROM Users WHERE groupId=? AND isPrimary=false;");
            prepStmt.setInt(1, group.id);

            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                do {
                    map.put(
                            UUID.fromString(result.getString("uuid")),
                            Helpers.getPermissionLevelFromInt(result.getInt("permissionLevel"))
                    );
                } while (result.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }
}