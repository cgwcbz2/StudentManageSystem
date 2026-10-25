-- Run against an existing studentms database. Safe to rerun.
USE studentms;

SET @has_pass_score = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'studentms' AND TABLE_NAME = 'c' AND COLUMN_NAME = 'pass_score'
);
SET @add_pass_score = IF(@has_pass_score = 0,
  'ALTER TABLE studentms.c ADD COLUMN pass_score TINYINT UNSIGNED NOT NULL DEFAULT 60',
  'SELECT 1');
PREPARE migration_stmt FROM @add_pass_score;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

CREATE TABLE IF NOT EXISTS `assessment_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sctid` int(11) NOT NULL,
  `grade` float NOT NULL,
  `pass_score_snapshot` tinyint unsigned NOT NULL,
  `operator_tid` int(11) DEFAULT NULL,
  `recorded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `source` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_assessment_history_sct_time` (`sctid`, `recorded_at`, `id`),
  CONSTRAINT `assessment_history_ibfk_1` FOREIGN KEY (`sctid`) REFERENCES `sct` (`sctid`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `assessment_history` DROP FOREIGN KEY `assessment_history_ibfk_1`;
ALTER TABLE `assessment_history`
  ADD CONSTRAINT `assessment_history_ibfk_1` FOREIGN KEY (`sctid`) REFERENCES `sct` (`sctid`) ON DELETE RESTRICT;

INSERT INTO `assessment_history` (`sctid`, `grade`, `pass_score_snapshot`, `operator_tid`, `recorded_at`, `source`)
SELECT sct.sctid, sct.grade, c.pass_score, NULL, UTC_TIMESTAMP(), 'initial_import'
FROM `sct` sct INNER JOIN `c` c ON c.cid = sct.cid
WHERE sct.grade IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `assessment_history` ah WHERE ah.sctid = sct.sctid);
