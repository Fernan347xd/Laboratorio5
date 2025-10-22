CREATE TABLE maintenance
(
    id            INT AUTO_INCREMENT NOT NULL,
    `description` VARCHAR(100)       NULL,
    type          VARCHAR(255)       NULL,
    CONSTRAINT pk_maintenance PRIMARY KEY (id)
);