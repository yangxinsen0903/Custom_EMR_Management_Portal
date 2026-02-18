UPDATE ambari_config_item
        SET value='Custom'
        WHERE `key` = 'fs.azure.account.auth.type';
UPDATE ambari_config_item
        SET value='com.github.azure.hadoop.custom.auth.MSIHDFSCachedAccessTokenProvider'
        WHERE `key` = 'fs.azure.account.oauth.provider.type';
