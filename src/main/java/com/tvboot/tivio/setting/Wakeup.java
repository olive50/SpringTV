package com.tvboot.tivio.setting;

public class Wakeup {
}
/*CREATE TABLE `t_wakeup` (
  `f_terminal_ip` varchar(15) NOT NULL default '',
  `f_media_id` int(11) NOT NULL default '0',
  `f_wakeup_time` datetime NOT NULL default '0000-00-00 00:00:00',
  `f_permanent` enum('yes','no') NOT NULL default 'no',
  `f_status` enum('disabled','programmed','triggered','error','running','confirmed','alert') NOT NULL default 'disabled',
  `f_monitoring_notification` enum('none','set','result') NOT NULL default 'none',
  PRIMARY KEY  (`f_terminal_ip`,`f_wakeup_time`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Wakeup records for Wakeup service';*/