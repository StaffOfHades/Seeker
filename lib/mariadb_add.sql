/*** DOCUMENTS ***/
/* Insert id, text (?, ?) into document */
insert into `documents` (`id`, `text`) values(?, ?);
/* Insert id, term (?, ?) into terms */
insert into `terms` (`id`, `term`) values(?, ?);
/* Insert iddoc, term, tf (?, ?, ?) into contains */
insert into `contains` (`iddoc`, `term`, `tf`) values (?, ?, ?);

/*** PRE-DEFINED QUERIES ***/
/* Insert id, text (?, ?) into queries */
insert into `queries` (`id`, `text`) values(?, ?);
/* Insert id, term (?, ?) into terms */
insert into `terms` (`id`, `term`) values(?, ?);
/* Insert idquery, term, tf (?, ?, ?) into made */
insert into `made` (`idquery`, `term`, `tf`) values (?, ?, ?);
/* Inser iddoc, idquery (?, ?) into relevant */
insert into `relevant` (`iddoc`,`idquery`) values (?, ?);

/*** USER-DEFINED QUERIES ***/
/* Insert text (?) into queries */
insert into `queries` (`text`) values(?);
/* Find if term (?) exists */
select id from terms where term = ?; 
	/* If term is not present, add term (?) */
	insert into `terms` (`term`) values(?);
	/* And get the id */
	select last_insert_id();
/* Insert idquery, term, tf (?, ?, ?) into made */
insert into `made` (`idquery`, `term`, `tf`) values (?, ?, ?);