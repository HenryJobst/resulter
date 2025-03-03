select * from person_race_result prr
where prr.person_id not in (select id from person p where p.id = prr.person_id)
order by result_list_id, class_result_short_name, position;
