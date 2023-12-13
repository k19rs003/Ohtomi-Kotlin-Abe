create database db_ohtomi CHARACTER SET utf8mb4;

use db_ohtomi;

CREATE TABLE tbl_users_a ( id INT(11) AUTO_INCREMENT NOT NULL PRIMARY KEY, userId INT(11), count INT(11) NOT NULL DEFAULT 0, newsId INT(11) NOT NULL DEFAULT 0, user VARCHAR(255) UNIQUE KEY, password VARCHAR(255), deviceId VARCHAR(64) NOT NULL UNIQUE KEY, deviceToken VARCHAR(64) UNIQUE KEY, flag tinyint(1) NOT NULL DEFAULT 0, modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tbl_users_a ( id INT(11) AUTO_INCREMENT NOT NULL PRIMARY KEY, userId INT(11), count INT(11) NOT NULL DEFAULT 0, user VARCHAR(255) UNIQUE KEY, password VARCHAR(255), deviceId VARCHAR(64) UNIQUE KEY, deviceToken VARCHAR(255) UNIQUE KEY, flag tinyint(1) NOT NULL DEFAULT 0, modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

MariaDB [db_ohtomi]> desc tbl_users_a;
+-------------+--------------+------+-----+---------------------+-------------------------------+
| Field       | Type         | Null | Key | Default             | Extra                         |
+-------------+--------------+------+-----+---------------------+-------------------------------+
| id          | int(11)      | NO   | PRI | NULL                | auto_increment                |
| userId      | int(11)      | YES  |     | NULL                |                               |
| count       | int(11)      | NO   |     | 0                   |                               |
| user        | varchar(255) | YES  | UNI | NULL                |                               |
| password    | varchar(255) | YES  |     | NULL                |                               |
| deviceId    | varchar(64)  | YES  | UNI | NULL                |                               |
| deviceToken | varchar(255)  | YES  | UNI | NULL                |                               |
| flag        | tinyint(1)   | NO   |     | 0                   |                               |
| modified    | datetime     | NO   |     | current_timestamp() | on update current_timestamp() |
| created     | datetime     | NO   |     | current_timestamp() |                               |
+-------------+--------------+------+-----+---------------------+-------------------------------+
10 rows in set (0.001 sec)

CREATE TABLE tbl_news ( id INT(11) AUTO_INCREMENT NOT NULL PRIMARY KEY, title VARCHAR(500), url VARCHAR(2000), source INT(11), date VARCHAR(64), flag tinyint(1) NOT NULL DEFAULT 0, modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

MariaDB [db_ksu]> desc tbl_news;
+----------+---------------+------+-----+---------------------+-------------------------------+
| Field    | Type          | Null | Key | Default             | Extra                         |
+----------+---------------+------+-----+---------------------+-------------------------------+
| id       | int(11)       | NO   | PRI | NULL                | auto_increment                |
| title    | varchar(500)  | YES  |     | NULL                |                               |
| url      | varchar(2000) | YES  |     | NULL                |                               |
| source   | int(11)       | YES  |     | NULL                |                               |
| date     | varchar(64)   | YES  |     | NULL                |                               |
| flag     | tinyint(1)    | NO   |     | 0                   |                               |
| modified | datetime      | NO   |     | current_timestamp() | on update current_timestamp() |
| created  | datetime      | NO   |     | current_timestamp() |                               |
+----------+---------------+------+-----+---------------------+-------------------------------+
8 rows in set (0.001 sec)
