
delete from  config_detail where application ='sdp-compose' and akey  in ('sdp.install.ansible.reduce','sdp.install.ambari.reduce');
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
('sdp.install.ansible.reduce','1','sdp-compose','test','master','db'),
('sdp.install.ambari.reduce','1','sdp-compose','test','master','db');