package com.tvboot.tivio.entities;

public class WebLink {
}
/*
DROP TABLE IF EXISTS `t_web_link`;
CREATE TABLE `t_web_link` (
  `f_link_id` int(11) NOT NULL auto_increment,
  `f_url` text NOT NULL,
  `f_image` tinytext NOT NULL,
  `f_order` tinyint(5) unsigned NOT NULL default '0',
  `f_category_id` int(11) NOT NULL default '0',
  `f_enabled` enum('yes','no') NOT NULL default 'yes',
  PRIMARY KEY  (`f_link_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Web Portal links';

--
-- Dumping data for table `t_web_link`
--



LOCK TABLES `t_web_link` WRITE;
INSERT INTO `t_web_link` VALUES (31,'http://www.bbc.com/','bbc.gif',1,1,'yes');
INSERT INTO `t_web_link` VALUES (32,'http://actu.voila.fr/','voila.gif',2,1,'yes');
INSERT INTO `t_web_link` VALUES (36,'http://www.xe.com/ucc/fr/','xe.gif',2,3,'yes');
INSERT INTO `t_web_link` VALUES (39,'http://www.eurosport.fr/','eurosport.gif',3,1,'yes');
INSERT INTO `t_web_link` VALUES (49,'http://monde.lachainemeteo.com/meteo-monde/prevision_meteo_monde.php','meteoconsult.gif',3,3,'yes');
UNLOCK TABLES;*/