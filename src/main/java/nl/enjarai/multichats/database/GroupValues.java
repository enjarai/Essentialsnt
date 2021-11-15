package nl.enjarai.multichats.database;

public enum GroupValues {
    ALL("*", "Group"),
    NAME("name", "String");

    public final String dbName;
    public final String type;

    GroupValues(String dbName, String type) {
        this.dbName = dbName;
        this.type = type;
    }
}
