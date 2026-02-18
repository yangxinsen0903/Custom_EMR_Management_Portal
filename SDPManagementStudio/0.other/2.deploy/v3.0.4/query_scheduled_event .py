#!/usr/bin/python
import math
import socket
import datetime
from dateutil.parser import parse
import json
import redis
from redis.connection import SSLConnection
import requests
from time import sleep
import os
from azure.keyvault.secrets import SecretClient
from azure.identity import DefaultAzureCredential
from azure.identity import ManagedIdentityCredential
import time
import operator

# The URL to access the metadata service
metadata_url ="http://169.254.169.254/metadata/scheduledevents"
# This must be sent otherwise the request will be ignored
header = {'Metadata' : 'true'}
# Current version of the API 
query_params = {'api-version':'2020-07-01'}
keyvault_conf_refresh_time = 0

VM_INFO = None
HOST_NAME = None
KEYVAULT_CONF = None
REDIS_CONF = None
REDIS_POOL = None
REDIS_CONN = None
REFRESH_COUNTER = 0
HEARTBEAT_REDIS_COUNTER = 0
MAX_REFRESH_COUNTER = 600
MAX_HEARTBEAT_COUNTER = 600
MAX_REDIS_EXPIRES = 900
LAST_HEARTBEAT_TIME = None
UPLOAD_EVICTION_COUNTER = 0
EVENT_OBJ = None

def get_vm_info():
    try:
        global VM_INFO
        global HOST_NAME
        if VM_INFO is None: 
            HOST_NAME = socket.gethostname() 
            with open("/etc/clustermessage.json") as infile: 
                VM_INFO = json.load(infile) 
                print(VM_INFO) 

            VM_INFO['hostname'] = HOST_NAME
            print("new vm info")
        return VM_INFO;
    except:
        return None

def get_keyvault_conf():
    global KEYVAULT_CONF
    global REFRESH_COUNTER
    if (REFRESH_COUNTER == 0):
        KEYVAULT_CONF = None

    if (KEYVAULT_CONF is not None):
        return KEYVAULT_CONF

    try:
        VM_INFO["kvkey"] = "redisconf2"
        print("new keyvault conf response json:" + json.dumps(VM_INFO))
        KEYVAULT_CONF = VM_INFO
        return KEYVAULT_CONF
    except:
        return None

def get_redis_conf():
    global REDIS_CONF
    global REFRESH_COUNTER

    if (REFRESH_COUNTER == 0):
        REDIS_CONF = None

    if (REDIS_CONF is not None):
        return REDIS_CONF

    keyvault_conf = get_keyvault_conf()
    if(keyvault_conf is None):
        return None

    try:
        KVUri = keyvault_conf["spotkvuri"]
        clientId = keyvault_conf["opsmiclientid"]
        secretName = keyvault_conf["kvkey"]
        print("spotkvuri:" + KVUri + ", kvkey:" + secretName +", opsmiclientid:" + clientId)
        
        credential = ManagedIdentityCredential(client_id=clientId)
        client = SecretClient(vault_url = KVUri, credential = credential)
        retrieved_secret = client.get_secret(secretName)

        print("Your secret is " + retrieved_secret.value)

        REDIS_CONF = json.loads(retrieved_secret.value)
        return REDIS_CONF
    except Exception as e:
        sleep(30)
        print(repr(e))
        return None

def set_redis(itemKey, itemValue):
    global REDIS_POOL
    global REDIS_CONN
    global REFRESH_COUNTER
    global MAX_REDIS_EXPIRES

    if(REFRESH_COUNTER == 0):
        close_redis_pool()

    if(REDIS_POOL is None):
        redis_conf = get_redis_conf()
        REDIS_POOL = redis.ConnectionPool(connection_class = SSLConnection, host = redis_conf["host"], port = redis_conf["port"], password = redis_conf["password"], db = redis_conf["db"], health_check_interval = 120)
        print("new redis pool host:" + redis_conf["host"] + ", port:" + str(redis_conf["port"]))

    if(REDIS_CONN is None):
        try:
            REDIS_CONN = redis.Redis(connection_pool = REDIS_POOL)
            print("new redis connection host:" + redis_conf["host"] + ", port:" + str(redis_conf["port"]) + ", db:" + str(redis_conf["db"]))
        except:
            close_redis_pool()

    if(REDIS_CONN is None):
        print("not found redis connection host:" + redis_conf["host"] + ", port:" + str(redis_conf["port"]) + ", db:" + str(redis_conf["db"]))
        return
    
    try:
        REDIS_CONN.set(itemKey, itemValue)
        print("redis set itemKey:" + itemKey +", itemValue:" + itemValue)
        REDIS_CONN.expire(itemKey, MAX_REDIS_EXPIRES)
        print("redis expire itemKey:" + itemKey + ", " + str(MAX_REDIS_EXPIRES) + "s")
    except:
        print("redis set error")
    finally:
        close_redis_pool()

def close_redis_pool():
    global REDIS_POOL
    global REDIS_CONN

    if(REDIS_CONN is not None):
        try:
            REDIS_POOL.disconnect()
            print("close redis pool")
        except:
            print("close redis pool error")
        REDIS_CONN = None
        REDIS_POOL = None

def query_scheduled_events():
    print("query_scheduled_events request get url:" + metadata_url)
    resp = requests.get(metadata_url, headers = header, params = query_params)
    data = resp.json()
    print("response json:" + json.dumps(data))
    return data

def confirm_scheduled_event(event_id):     
    print("confirm_scheduled_event event_id:" + str(event_id))
    payload = json.dumps({"StartRequests": [{"EventId": event_id }]})

    print("request post url:" + metadata_url + ",payload:" + payload)
    response = requests.post(metadata_url, 
                            headers= header,
                            params = query_params, 
                            data = payload)  
    print("response status:" + str(response.status_code))  
    return response.status_code

def log(event): 
    # This is an optional placeholder for logging events to your system 
    print(event["Description"])
    return

def string_to_time(timeString):
    return parse(timeString)

def time_to_string(time):
    return time.strftime('%Y-%m-%d %H:%M:%S')

def getTime(timeString):
    if(timeString is None or timeString == ''):
        return datetime.datetime.now()
    
    try:
        date_obj = parse(timeString)
        return date_obj
    except:
        return datetime.datetime.now()

def getTimeString(timeString):
    if(timeString is None or timeString == ''):
        return datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    try:
        date_obj = parse(timeString)
        return date_obj.strftime('%Y-%m-%d %H:%M:%S')
    except:
        return datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')

def query_event(last_document_incarnation): 
    global REFRESH_COUNTER
    global HEARTBEAT_REDIS_COUNTER
    global MAX_REFRESH_COUNTER
    global MAX_HEARTBEAT_COUNTER
    global LAST_HEARTBEAT_TIME
    global UPLOAD_EVICTION_COUNTER
    global EVENT_OBJ
    global HOST_NAME

    get_vm_info()

    if(HOST_NAME is None):
        print('host name is none')
        return

    if(operator.contains(HOST_NAME, '-amb-')):
        print('amberi node skip:' + HOST_NAME)
        sleep(30)
        return

    if(operator.contains(HOST_NAME, '-cor-')):
        print('core node skip:' + HOST_NAME)
        sleep(30)
        return
    
    if(operator.contains(HOST_NAME, '-mst-')):
        print('master node skip:' + HOST_NAME)
        sleep(30)
        return

    print("query_event last_document_incarnation:" + str(last_document_incarnation) + ", REFRESH_COUNTER:" + str(REFRESH_COUNTER) +",HEARTBEAT_REDIS_COUNTER:" + str(HEARTBEAT_REDIS_COUNTER))
    sleep(1)
    payload = query_scheduled_events()
    vm_info = get_vm_info()
    if(vm_info is None):
        print("vm_info is null")
        return

    found_document_incarnation = payload["DocumentIncarnation"]
    if(found_document_incarnation == last_document_incarnation):
        print("return found_document_incarnation:" + str(found_document_incarnation))
        if EVENT_OBJ is not None:
            redisItemKey = 'spot_event:' + vm_info['clusterid'] + ':' + socket.gethostname()
            upload_event_to_redis(redisItemKey)
        else:
            currentTimeNow = getTime(None)
            if(HEARTBEAT_REDIS_COUNTER <= 0 or (currentTimeNow - LAST_HEARTBEAT_TIME).total_seconds() >= MAX_HEARTBEAT_COUNTER):
                LAST_HEARTBEAT_TIME = currentTimeNow
                HEARTBEAT_REDIS_COUNTER = MAX_HEARTBEAT_COUNTER
                redisItemValue = "{\"time\":\"" + getTimeString(None) + "\",\"evictTime\":null,\"remaining\":null}"
                redisItemKey = 'spot_event:' + vm_info['clusterid'] + ':' + socket.gethostname()
                set_redis(redisItemKey, redisItemValue)
            else:
                HEARTBEAT_REDIS_COUNTER -= 1

            if(REFRESH_COUNTER > MAX_REFRESH_COUNTER):
                REFRESH_COUNTER = 0
                HEARTBEAT_REDIS_COUNTER = 0;
            else:
                REFRESH_COUNTER += 1
        return found_document_incarnation
    
    set_redis_flag = 0
    EVENT_OBJ = None
    for event in payload["Events"]:
        if (event["EventType"] == "Preempt"):
            EVENT_OBJ = event
            redisItemKey = 'spot_event:' + vm_info['clusterid'] + ':' + socket.gethostname()
            upload_event_to_redis(redisItemKey)
            set_redis_flag = 1
        else: 
            log(event)
    
    if(set_redis_flag == 0):
        currentTimeNow = getTime(None)
        if(HEARTBEAT_REDIS_COUNTER <= 0 or (currentTimeNow - LAST_HEARTBEAT_TIME).total_seconds() >= MAX_HEARTBEAT_COUNTER):
            LAST_HEARTBEAT_TIME = currentTimeNow
            HEARTBEAT_REDIS_COUNTER = MAX_HEARTBEAT_COUNTER
            redisItemValue = "{\"time\":\"" + getTimeString(None) + "\",\"evictTime\":null,\"remaining\":null}"
            redisItemKey = 'spot_event:' + vm_info['clusterid'] + ':' + socket.gethostname()
            set_redis(redisItemKey, redisItemValue)
        else:
            HEARTBEAT_REDIS_COUNTER -= 1

    print("Processed events from document: " + str(found_document_incarnation))

    if(REFRESH_COUNTER > MAX_REFRESH_COUNTER):
        REFRESH_COUNTER = 0
        HEARTBEAT_REDIS_COUNTER = 0;
    else:
        REFRESH_COUNTER += 1
    return found_document_incarnation

def upload_event_to_redis(redisItemKey):
    global UPLOAD_EVICTION_COUNTER
    global EVENT_OBJ
    try:
        if UPLOAD_EVICTION_COUNTER <= 0:
            UPLOAD_EVICTION_COUNTER = 5
            print("Preempt, NotBefore:" + EVENT_OBJ["NotBefore"])
            timeEvent = string_to_time(EVENT_OBJ["NotBefore"])
            GMT_FORMAT =  '%a, %d %b %Y %H:%M:%S GMT'
            timeNow = string_to_time(datetime.datetime.utcnow().strftime(GMT_FORMAT))
            print("timeNow:" + time_to_string(timeNow))
            print("timeEvent:" + time_to_string(timeEvent))
            remaining = math.floor((timeEvent - timeNow).total_seconds())
            print('remaining>>', remaining)
            redisItemValue = "{\"time\":\"" + time_to_string(timeNow) + "\",\"evictTime\":\"" + time_to_string(timeEvent) + "\",\"remaining\":" + str(remaining) + "}"
            set_redis(redisItemKey, redisItemValue)
        else:
            UPLOAD_EVICTION_COUNTER -= 1
    except Exception as e:
        print(e)

def main():
    global LAST_HEARTBEAT_TIME
    # This will track the last set of events seen 
    last_document_incarnation = "-1"
    hostname = socket.gethostname()
    print("hostname:" + hostname)
    LAST_HEARTBEAT_TIME = getTime(None)
    print("time:" + getTimeString(LAST_HEARTBEAT_TIME))

    while_loop = True
    while while_loop == True:            
        try:    
            last_document_incarnation = query_event(last_document_incarnation)
        except Exception as e:
            print(e)
            print('error')
    
    # input_text = "\
    #     Press 1 to poll for new events \n\
    #     Press 2 to exit \n "
    # program_exit = False 

    # while program_exit == False:
    #     user_input = input(input_text)    
    #     if (user_input == "1"):            
    #         try:            
    #             last_document_incarnation = query_event(last_document_incarnation)
    #         except:
    #             print('error')
    #     elif (user_input == "2"):
    #         program_exit = True       

if __name__ == '__main__':
    main()