drop table if exists commits;
CREATE TABLE `commits` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `commit_id` varchar(100) DEFAULT NULL,
   `repository_id` int(11) DEFAULT NULL,
   `website` longtext,
   `parent_commit_id` varchar(100) DEFAULT NULL,
   `commit_log` longtext,
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
drop table if exists apichange;
CREATE TABLE `apichange` (
   `apichange_id` int(11) NOT NULL AUTO_INCREMENT,
   `commit_id` int(11) NOT NULL,
   `file_name` varchar(255) DEFAULT NULL,
   `old_line_number` int(11) DEFAULT NULL,
   `new_line_number` int(11) DEFAULT NULL,
   `change_type` varchar(255) DEFAULT NULL,
   `old_content` longtext,
   `new_content` longtext,
   `old_mi` longtext,
   `new_mi` longtext,
   `old_complete_class_name` varchar(255) DEFAULT NULL,
   `new_complete_class_name` varchar(255) DEFAULT NULL,
   `old_method_name` varchar(255) DEFAULT NULL,
   `new_method_name` varchar(255) DEFAULT NULL,
   `old_parameter_type` longtext DEFAULT NULL,
   `new_parameter_type` longtext DEFAULT NULL,
   `old_parameter_num` int(8) DEFAULT NULL,
   `new_parameter_num` int(8) DEFAULT NULL,
   `old_parameter_name` longtext DEFAULT NULL,
   `new_parameter_name` longtext DEFAULT NULL,
   `parameter_position` int(11) DEFAULT -1,
   PRIMARY KEY (`apichange_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;