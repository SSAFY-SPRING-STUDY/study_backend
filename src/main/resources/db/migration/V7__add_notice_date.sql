ALTER TABLE notice
    ADD created_at datetime NULL;

ALTER TABLE notice
    ADD updated_at datetime NULL;

ALTER TABLE notice
    MODIFY created_at datetime NOT NULL;

ALTER TABLE notice
    MODIFY updated_at datetime NOT NULL;
