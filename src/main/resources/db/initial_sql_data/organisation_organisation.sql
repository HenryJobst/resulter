INSERT INTO ORGANISATION_ORGANISATION (organisation_id, PARENT_ORGANISATION_ID)
VALUES ((SELECT id FROM organisation WHERE short_name = 'LV_BE'),
        (SELECT id FROM organisation WHERE short_name = 'NOR'));
INSERT INTO ORGANISATION_ORGANISATION (organisation_id, PARENT_ORGANISATION_ID)
VALUES ((SELECT id FROM organisation WHERE short_name = 'LV_BB'),
        (SELECT id FROM organisation WHERE short_name = 'NOR'));
INSERT INTO ORGANISATION_ORGANISATION (organisation_id, PARENT_ORGANISATION_ID)
VALUES ((SELECT id FROM organisation WHERE short_name = 'LV_MV'),
        (SELECT id FROM organisation WHERE short_name = 'NOR'));
