-- 去掉非通用的配置
delete from ambari_config_item where `key` in (
                                               'fs.azure.account.auth.type',
                                               'fs.azure.account.oauth.provider.type',
                                               'fs.azure.account.oauth2.client.id',
                                               'fs.azure.account.oauth2.msi.endpoint',
                                               'fs.azure.account.oauth2.msi.tenant'
);