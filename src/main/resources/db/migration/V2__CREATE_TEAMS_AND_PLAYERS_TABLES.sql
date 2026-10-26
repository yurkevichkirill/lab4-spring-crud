-- Таблицы существующего проекта (ЛР №3), которые раньше создавал Hibernate (ddl-auto=update).
-- Теперь схемой управляет Flyway, Hibernate только валидирует её (ddl-auto=validate).
CREATE TABLE teams (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    city         VARCHAR(255),
    founded_year INT,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    CONSTRAINT pk_teams PRIMARY KEY (id),
    CONSTRAINT uk_teams_name UNIQUE (name)
);

CREATE TABLE players (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    position     VARCHAR(255) NOT NULL,
    shirt_number INT,
    birth_date   DATE,
    team_id      BIGINT       NOT NULL,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    CONSTRAINT pk_players PRIMARY KEY (id),
    CONSTRAINT fk_players_team FOREIGN KEY (team_id) REFERENCES teams (id)
);
