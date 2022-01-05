CREATE TABLE IF NOT EXISTS `listings`(
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `seller` VARCHAR(36) NOT NULL,
    `material` VARCHAR(255) NOT NULL,
    `price` DECIMAL(10, 2) NOT NULL,
    `quantity` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);