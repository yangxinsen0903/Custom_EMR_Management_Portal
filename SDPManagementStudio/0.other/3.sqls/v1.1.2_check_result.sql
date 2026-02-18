select 'ambari_config_item' as table_name,if(count(*)=0,true,false) as check_result,'delete' as action
from sdpms.ambari_config_item where `key` in (
                                              'fs.azure.account.auth.type',
                                              'fs.azure.account.oauth.provider.type',
                                              'fs.azure.account.oauth2.client.id',
                                              'fs.azure.account.oauth2.msi.endpoint',
                                              'fs.azure.account.oauth2.msi.tenant'
    );