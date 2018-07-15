drop table if exists apichange_9th;
CREATE TABLE `apichange_9th` (
   `apichange_id` int(11) NOT NULL AUTO_INCREMENT,
   `repository_id` int(11) DEFAULT NULL,
   `website` longtext,
   `commit_time` long DEFAULT NULL,
   `new_file_name` varchar(255) DEFAULT NULL,
   `old_file_name` varchar(255) DEFAULT NULL,
   `old_line_number` int(11) DEFAULT NULL,
   `new_line_number` int(11) DEFAULT NULL,
   `change_type` varchar(255) DEFAULT NULL,
   `old_content` longtext,
   `new_content` longtext,
   `old_mi` longtext,
   `new_mi` longtext,
   `old_complete_class_name` varchar(255) DEFAULT NULL,
   `new_complete_class_name` varchar(255) DEFAULT NULL,
   `old_receiver_name` varchar(255) DEFAULT NULL,
   `new_receiver_name` varchar(255) DEFAULT NULL,
   `old_method_name` varchar(255) DEFAULT NULL,
   `new_method_name` varchar(255) DEFAULT NULL,
   `old_parameter_type` longtext DEFAULT NULL,
   `new_parameter_type` longtext DEFAULT NULL,
   `old_parameter_num` int(8) DEFAULT NULL,
   `new_parameter_num` int(8) DEFAULT NULL,
   `old_parameter_name` longtext DEFAULT NULL,
   `new_parameter_name` longtext DEFAULT NULL,
   `commit_id` varchar(100) DEFAULT NULL,
   `parent_commit_id` varchar(100) DEFAULT NULL,
   `commit_log` longtext,
   `example_id` int(11) DEFAULT NULL,
   `parameter_position` varchar(255) DEFAULT -1,
   PRIMARY KEY (`apichange_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;