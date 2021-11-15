#!/usr/bin/python3

import sqlite3

db = sqlite3.connect("multichats.db")
cursor = db.cursor()


cursor.execute("ALTER TABLE Users RENAME TO Users_old")
cursor.execute("""
                CREATE TABLE Users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid varchar(36) NOT NULL,
                    groupId INT NOT NULL,
                    permissionLevel INT NOT NULL,
                    isPrimary BOOL,
                    FOREIGN KEY (groupId)
                    REFERENCES Groups (id)
                       ON UPDATE CASCADE
                       ON DELETE CASCADE
                )
                """)
cursor.execute("SELECT * FROM Users_old")
for row in cursor.fetchall():
    print(row)
    cursor.execute("SELECT id FROM Groups WHERE name=?", [row[2]])
    #print(cursor.fetchone()[0])

    row = list(row)
    row[2] = cursor.fetchone()[0]

    cursor.execute('''
                INSERT INTO
                    Users
                VALUES
                    (?,?,?,?,?)
                    ''', row)


db.commit()
db.close()
