SELECT `material`, COUNT(`material`), MIN(`price`)
FROM `listings`
WHERE `material`
IN (SELECT `material` FROM `material_index` WHERE  `category` = ?)
GROUP BY `material`;
