select `df` from `terms` where `term` = "b";
select `idf` from `terms` where `term` = "b";
select `contains`.`iddoc`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) as `similar` from `contains`, `made`, `terms` where `contains`.`term` = `terms`.`term` and `made`.`term` = `terms`.`term` and `made`.`idquery` = 1 group by `contains`.`iddoc` order by `similar` desc;
select `iddoc` from `relevant` where `idquery` = 1;