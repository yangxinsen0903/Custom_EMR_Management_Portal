
select '2.1.1' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'c3a0ffd8c02841fe88e4e068868fa0c4' as img_id
from base_image_scripts bis
where img_id ='c3a0ffd8c02841fe88e4e068868fa0c4'
union all
select '2.1.1' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'4112bf6b877f40139d3932d003856ed9' as img_id
from base_image_scripts
where img_id ='4112bf6b877f40139d3932d003856ed9'
union all
select '2.1.1' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'0da6584f68ed43ea88925ed94fc1a33a' as img_id
from base_image_scripts
where img_id ='0da6584f68ed43ea88925ed94fc1a33a'
union all
select '2.1.1' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'8596cf8ea06c11ed922d6045bdc792d8' as img_id
from base_image_scripts
where img_id ='8596cf8ea06c11ed922d6045bdc792d8'
union all
select '2.1.1' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'814dbf06a69b47029ab276208436060b' as img_id
from base_image_scripts
where img_id ='814dbf06a69b47029ab276208436060b';
