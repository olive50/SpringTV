package com.tvboot.tivio.language;

public class Translation {
    private int id;
    private String languageId;
    private String text;
}


/*
DROP TABLE IF EXISTS `lib_categorie`;
CREATE TABLE `lib_categorie` (
  `ID_categorie` int(11) NOT NULL default '0',
  `ID_langue` int(11) NOT NULL default '0',
  `libelle` tinytext NOT NULL,
  PRIMARY KEY  (`ID_categorie`,`ID_langue`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `lib_categorie`
--

s
LOCK TABLES `lib_categorie` WRITE;
INSERT INTO `lib_categorie` VALUES (1,1,'Adultes');
INSERT INTO `lib_categorie` VALUES (1,2,'Adults');
INSERT INTO `lib_categorie` VALUES (1,11,'Adultos');
INSERT INTO `lib_categorie` VALUES (1,13,'&#1058;&#1086;&#1083;&#1100;&#1082;&#1086; &#1076;&#1083;&#1103; &#1074;&#1079;&#1088;&#1086;&#1089;&#1083;&#1099;&#1093;');
INSERT INTO `lib_categorie` VALUES (1,14,'Erwachsene');
INSERT INTO `lib_categorie` VALUES (1,20,'&#25104;&#20154;');
INSERT INTO `lib_categorie` VALUES (1,28,'&#25104;&#20154;&#21521;&#12369;');
INSERT INTO `lib_categorie` VALUES (1,29,'&#49457;&#51064;');
INSERT INTO `lib_categorie` VALUES (1,30,'&#25104;&#20154;&#29255;');
INSERT INTO `lib_categorie` VALUES (1,63,'Adulti');
INSERT INTO `lib_categorie` VALUES (1,69,'Volwassen');
INSERT INTO `lib_categorie` VALUES (1,79,'Para adultos');
INSERT INTO `lib_categorie` VALUES (23,1,'S&#233;minaire');
INSERT INTO `lib_categorie` VALUES (23,2,'Seminar');
INSERT INTO `lib_categorie` VALUES (23,11,'Seminario privado');
INSERT INTO `lib_categorie` VALUES (23,13,'&#1047;&#1072;&#1082;&#1088;&#1099;&#1090;&#1099;&#1081; &#1089;&#1077;&#1084;&#1080;&#1085;&#1072;&#1088;');
INSERT INTO `lib_categorie` VALUES (23,14,'Seminare');
INSERT INTO `lib_categorie` VALUES (23,20,'&#31169;&#20154;&#30740;&#35752;&#20250;');
INSERT INTO `lib_categorie` VALUES (23,28,'&#12503;&#12521;&#12452;&#12505;&#12540;&#12488;&#12475;&#12511;&#12490;&#12540;');
INSERT INTO `lib_categorie` VALUES (23,29,'&#54617;&#49845;');
INSERT INTO `lib_categorie` VALUES (23,30,'&#31169;&#20154;&#24231;&#35527;&#26371;');
INSERT INTO `lib_categorie` VALUES (23,63,'Seminari');
INSERT INTO `lib_categorie` VALUES (23,69,'Priv&#233;-seminar');
INSERT INTO `lib_categorie` VALUES (23,79,'Semin&#225;rio Privado');
INSERT INTO `lib_categorie` VALUES (24,1,'Animation');
INSERT INTO `lib_categorie` VALUES (24,2,'Animation');
INSERT INTO `lib_categorie` VALUES (25,1,'Action');
INSERT INTO `lib_categorie` VALUES (25,2,'Action');
INSERT INTO `lib_categorie` VALUES (25,69,'Actie');
INSERT INTO `lib_categorie` VALUES (25,79,'Ac&#231;&#227;o');
INSERT INTO `lib_categorie` VALUES (26,1,'Fantastique');
INSERT INTO `lib_categorie` VALUES (26,2,'Fantasy');
INSERT INTO `lib_categorie` VALUES (26,11,'Fantas&#237;a');
INSERT INTO `lib_categorie` VALUES (26,13,'&#1060;&#1072;&#1085;&#1090;&#1072;&#1089;&#1090;&#1080;&#1082;&#1072;');
INSERT INTO `lib_categorie` VALUES (26,14,'Fantasy');
INSERT INTO `lib_categorie` VALUES (26,20,'&#22855;&#24187;');
INSERT INTO `lib_categorie` VALUES (26,28,'&#12501;&#12449;&#12531;&#12479;&#12472;&#12540;');
INSERT INTO `lib_categorie` VALUES (26,29,'&#54032;&#53440;&#51648;');
INSERT INTO `lib_categorie` VALUES (26,30,'&#22855;&#24187;&#29255;');
INSERT INTO `lib_categorie` VALUES (26,63,'Fantasy');
INSERT INTO `lib_categorie` VALUES (26,69,'Fantasie');
INSERT INTO `lib_categorie` VALUES (26,79,'Fantasia');
INSERT INTO `lib_categorie` VALUES (27,1,'Com&#233;die');
INSERT INTO `lib_categorie` VALUES (27,2,'Comedy');
INSERT INTO `lib_categorie` VALUES (27,11,'Comedia');
INSERT INTO `lib_categorie` VALUES (27,13,'&#1050;&#1086;&#1084;&#1077;&#1076;&#1080;&#1103;');
INSERT INTO `lib_categorie` VALUES (27,14,'Kom&#246;die');
INSERT INTO `lib_categorie` VALUES (27,20,'&#21916;&#21095;');
INSERT INTO `lib_categorie` VALUES (27,28,'&#12467;&#12513;&#12487;&#12451;');
INSERT INTO `lib_categorie` VALUES (27,29,'&#53076;&#47700;&#46356;');
INSERT INTO `lib_categorie` VALUES (27,30,'&#25138;&#21127;&#29255;');
INSERT INTO `lib_categorie` VALUES (27,63,'Commedia');
INSERT INTO `lib_categorie` VALUES (27,69,'Komedie');
INSERT INTO `lib_categorie` VALUES (27,79,'Com&#233;dia');
INSERT INTO `lib_categorie` VALUES (29,1,'Drame');
*/