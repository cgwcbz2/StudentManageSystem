-- Run this once against an existing studentms database.
-- Fresh installations already include the table in studentms.sql.
USE studentms;

CREATE TABLE IF NOT EXISTS `warning_followup` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sctid` int(11) NOT NULL,
  `content` text NOT NULL,
  `operator_tid` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_warning_followup_sctid_created_at` (`sctid`, `created_at`),
  CONSTRAINT `warning_followup_ibfk_1` FOREIGN KEY (`sctid`) REFERENCES `sct` (`sctid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
