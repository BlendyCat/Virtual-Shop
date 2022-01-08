SELECT * FROM `listings` WHERE `material`
in
(SELECT `material` from `material_index` WHERE `category` = ?);