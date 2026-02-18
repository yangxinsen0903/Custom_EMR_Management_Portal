-- 中央站Blob账号
update config_detail set avalue = 'cep1prod1abcops.blob.core.windows.net' where akey = 'sdp.wgetpath';

update base_script set blob_path = replace(blob_path, 'cep1prod1abcdw','cep1prod1abcops');
