create table if not exists `material_index`(
	`id` INTEGER NOT NULL AUTO_INCREMENT,
    `material` VARCHAR(255) NOT NULL,
    `category` VARCHAR(255) NOT NULL,
    PRIMARY KEY(`id`)
);