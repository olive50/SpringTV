package com.tvboot.tivio.ads;

public class Ads {
}
/*
DROP TABLE IF EXISTS `ads`;
CREATE TABLE `ads` (
  `ID_ad` int(11) NOT NULL auto_increment,
  `name` varchar(64) NOT NULL default '',
  `filename` text NOT NULL,
  `duration` int(11) NOT NULL default '10',
  `url` text NOT NULL,
  `f_order` tinyint(3) unsigned NOT NULL default '0',
  `f_group_id` int(10) NOT NULL default '0',
  PRIMARY KEY  (`ID_ad`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Ads descriptions';*/
