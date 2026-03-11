ALTER TABLE curriculum
    ADD CONSTRAINT uc_curriculum_orderinstudy UNIQUE (study_id, order_in_study);
