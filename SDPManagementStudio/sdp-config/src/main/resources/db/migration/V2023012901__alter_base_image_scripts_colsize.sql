alter table base_image_scripts modify column script_file_uri varchar(1024);
alter table base_image_scripts modify column extra_vars varchar(1024);