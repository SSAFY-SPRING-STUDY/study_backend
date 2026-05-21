ALTER TABLE study_member ADD COLUMN github_repo_name VARCHAR(255);
ALTER TABLE study DROP COLUMN github_repo_name;
