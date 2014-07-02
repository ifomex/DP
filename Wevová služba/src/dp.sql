
drop table if exists journeys;
drop table if exists activities;


CREATE TABLE IF NOT EXISTS `journeys` (
  `journey_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_czech_ci NOT NULL,
  `sdate` bigint(20) NOT NULL,
  `edate` bigint(20) NOT NULL,
  PRIMARY KEY (`journey_id`)
);


CREATE TABLE IF NOT EXISTS `activities` (
  `act_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8 NOT NULL,
  `stime` bigint(20) NOT NULL,
  `etime` bigint(20) NOT NULL,
  `pl_name` varchar(50) CHARACTER SET utf8 NOT NULL,
  `address` text CHARACTER SET utf8 NOT NULL,
  `lat` float NOT NULL,
  `lon` float NOT NULL,
  `journey_id` int(11) NOT NULL,
  `category` int(11) NOT NULL,
  PRIMARY KEY (`act_id`)
);