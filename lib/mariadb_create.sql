/*** DOCUMENTS ***/

/* Documens from collection */
CREATE TABLE IF NOT EXISTS `documents` (
	`id` int(8) unsigned NOT NULL AUTO_INCREMENT,
  	`text` text NOT NULL,
  	PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/* Terms from collection and queries */
CREATE TABLE IF NOT EXISTS `terms` (
	`id` int(8) unsigned NOT NULL AUTO_INCREMENT,
  	`term` varchar(32) NOT NULL,
  	`idf` double(20,15) unsigned DEFAULT NULL,
	`df` int(8) unsigned DEFAULT NULL,
  	PRIMARY KEY (`term`),
  	KEY `k_termid` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Relation between documents and terms */
CREATE TABLE IF NOT EXISTS `contains` (
  	`iddoc` int(8) unsigned NOT NULL,
  	`term` varchar(32) NOT NULL,
  	`tf` int(8) NOT NULL,
  	PRIMARY KEY (`iddoc`,`term`),
  	KEY `k_iddoc` (`iddoc`),
  	KEY `k_query_term` (`term`),
  	CONSTRAINT `fk_doc_term`
  		FOREIGN KEY (`term`)
  		REFERENCES `terms` (`term`)
  		ON UPDATE CASCADE,
  	CONSTRAINT `fk_iddoc`
  		FOREIGN KEY (`iddoc`)
  		REFERENCES `documents` (`id`)
  		ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*** QUERIES ***/

/* Pre-defined queries or User-defined queries */
CREATE TABLE IF NOT EXISTS `queries` (
  	`id` int(8) unsigned NOT NULL AUTO_INCREMENT,
  	`text` text NOT NULL,
  	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Relation between queries and terms */
CREATE TABLE IF NOT EXISTS `made` (
  	`idquery` int(8) unsigned NOT NULL,
  	`term` varchar(32) NOT NULL,
  	`tf` int(8) NOT NULL,
  	PRIMARY KEY (`idquery`,`term`),
  	KEY `k_iddoc` (`idquery`),
  	KEY `k_term` (`term`),
  	CONSTRAINT `fk_idquery`
  		FOREIGN KEY (`idquery`)
  		REFERENCES `queries` (`id`)
  		ON UPDATE CASCADE,
  	CONSTRAINT `fk_term`
  		FOREIGN KEY (`term`)
  		REFERENCES `terms` (`term`)
  		ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Relation between pre-defined queries and relevant documents*/
CREATE TABLE IF NOT EXISTS `relevant` (
  	`iddoc` int(8) unsigned NOT NULL,
  	`idquery` int(8) unsigned NOT NULL,
  	PRIMARY KEY (`iddoc`,`idquery`),
  	KEY `k_relevant_iddoc` (`iddoc`),
  	KEY `k_relevant_idquery` (`idquery`),
  	CONSTRAINT `fk_relevant_iddoc`
  		FOREIGN KEY (`iddoc`)
  		REFERENCES `documents` (`id`)
  		ON UPDATE CASCADE,
  	CONSTRAINT `fk_relevant_idquery`
  		FOREIGN KEY (`idquery`)
  		REFERENCES `queries` (`id`)
  		ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;