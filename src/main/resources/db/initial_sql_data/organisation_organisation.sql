INSERT INTO organisation_organisation (organisation_id, parent_organisation_id)
VALUES ((SELECT id FROM organisation WHERE short_name = 'LV_BE'),
        (SELECT id FROM organisation WHERE short_name = 'NOR'));
INSERT INTO organisation_organisation (organisation_id, parent_organisation_id)
VALUES ((SELECT id FROM organisation WHERE short_name = 'LV_BB'),
        (SELECT id FROM organisation WHERE short_name = 'NOR'));
INSERT INTO organisation_organisation (organisation_id, parent_organisation_id)
VALUES ((SELECT id FROM organisation WHERE short_name = 'LV_MV'),
        (SELECT id FROM organisation WHERE short_name = 'NOR'));
