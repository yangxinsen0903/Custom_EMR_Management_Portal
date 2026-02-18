-- 保证可以重复执行，先删除数据
DELETE FROM config_detail WHERE akey = 'spring.datasource.multconfig' AND application in ('sdp-admin', 'sdp-compose');
DELETE FROM config_detail WHERE akey = 'spring.datasource.tableconfig' AND application in ('sdp-admin', 'sdp-compose');


-- 新增sdp-admin 和 sdp-compose 的多数据源配置
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select akey,
       replace(replace(replace(replace(avalue, '{username}', username), '{pwd}', pwd), '{sdpmsurl}', sdpmsurl), '{sdpurl}', sdpurl) as akey,
       application,
       profile,
       label,
       mwtype
from (
         select 'spring.datasource.multconfig' as akey, '[
        {
            "name": "ds0",
            "url": "{sdpmsurl}",
            "username": "{username}",
            "password": "{pwd}"
        },
        {
            "name": "ds1",
            "url": "{sdpurl}",
            "username": "{username}",
            "password": "{pwd}"
        }
    ]' as avalue, 'sdp-compose' as application ,'test' as profile,'master' as label,'db' as mwtype, '99999999' as rel
     ) newcfg join(
    select  '99999999' as rel,
            (select avalue from config_detail where akey = 'spring.datasource.url' and application = 'sdp-compose') as sdpmsurl,
            replace((select avalue from config_detail where akey = 'spring.datasource.url' and application = 'sdp-compose'), 'sdpms', 'sdp') as sdpurl,
            (select avalue from config_detail where akey = 'spring.datasource.username' and application = 'sdp-compose') as username,
            (select avalue from config_detail where akey = 'spring.datasource.password' and application = 'sdp-compose') as pwd
) val
                  on newcfg.rel = val.rel
union all
select akey,
       replace(replace(replace(replace(avalue, '{username}', username), '{pwd}', pwd), '{sdpmsurl}', sdpmsurl), '{sdpurl}', sdpurl) as akey,
       application,
       profile,
       label,
       mwtype
from (
         select 'spring.datasource.multconfig' as akey, '[
        {
            "name": "ds0",
            "url": "{sdpmsurl}",
            "username": "{username}",
            "password": "{pwd}"
        },
        {
            "name": "ds1",
            "url": "{sdpurl}",
            "username": "{username}",
            "password": "{pwd}"
        }
    ]' as avalue, 'sdp-admin' as application ,'test' as profile,'master' as label,'db' as mwtype, '99999999' as rel
     ) newcfg join(
    select  '99999999' as rel,
            (select avalue from config_detail where akey = 'spring.datasource.url' and application = 'sdp-compose') as sdpmsurl,
            replace((select avalue from config_detail where akey = 'spring.datasource.url' and application = 'sdp-compose'), 'sdpms', 'sdp') as sdpurl,
            (select avalue from config_detail where akey = 'spring.datasource.username' and application = 'sdp-compose') as username,
            (select avalue from config_detail where akey = 'spring.datasource.password' and application = 'sdp-compose') as pwd
) val
                  on newcfg.rel = val.rel;


INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('spring.datasource.tableconfig','[
    {
        "IsSplitTable": "0",  
        "logicTable": "ansiblecontrollog",
        "actualDataNodes": "ds1.ansiblecontrollog"
    },
    {
        "IsSplitTable": "0",  
        "logicTable": "joblist",
        "actualDataNodes": "ds1.joblist"
    },
    {
        "IsSplitTable": "0",  
        "logicTable": "jobresults",
        "actualDataNodes": "ds1.jobresults"
    }
]','sdp-admin','test','master','db');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('spring.datasource.tableconfig','[
    {
        "IsSplitTable": "0",  
        "logicTable": "ansiblecontrollog",
        "actualDataNodes": "ds1.ansiblecontrollog"
    },
    {
        "IsSplitTable": "0",  
        "logicTable": "joblist",
        "actualDataNodes": "ds1.joblist"
    },
    {
        "IsSplitTable": "0",  
        "logicTable": "jobresults",
        "actualDataNodes": "ds1.jobresults"
    }
]','sdp-compose','test','master','db');
