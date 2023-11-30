from pytz import timezone
from datetime import datetime, timedelta
import sys
from rediscluster import RedisCluster
import redis
from redis.client import Redis
from loguru import logger

logger.add(sys.stderr,
           format="{time} {message}",
           filter="client",
           level="INFO")
logger.add('logs/redis_client_{time:YYYY-MM-DD}.log',
           format="{time} {level} {message}",
           filter="client",
           level="ERROR")

class RedisClient:
    def __init__(self, connection_string, password=None):
        self.startup_nodes = []
        nodes = connection_string.split(',')
        for node in nodes:
            host_port = node.split(':')
            self.startup_nodes.append({'host': host_port[0], 'port': host_port[1]})

        self.password = password
        logger.info(self.startup_nodes)
        self.redis_pool = None
        self.redis_instance = None
        self.redis_cluster = None

    def connect(self):
        if(len(self.startup_nodes) < 2):
            host = self.startup_nodes[0].get('host')
            port = self.startup_nodes[0].get('port')
            if self.password:
                self.redis_pool = redis.ConnectionPool(host=host, port=port, db=0)
            else:
                self.redis_pool = redis.ConnectionPool(host=host, port=port, password = self.password, db=0)

            self.redis_instance = Redis(connection_pool=self.redis_pool, decode_responses=True)
            return self.redis_instance
        #, skip_full_coverage_check=True
        self.redis_cluster = RedisCluster(startup_nodes=self.startup_nodes, password=self.password)
        return self.redis_cluster

def test_hashset():

    client = RedisClient("192.168.56.113:7001,192.168.56.114:7001,192.168.56.115:7001", 'pass1234')
    #client = RedisClient("localhost:6379")
    conn = client.connect()
    #conn.execute_command("CLUSTER INFO")
    rtc_domain = 'rtc-uk.fanyamin.com'
    pool_name = 'tln30'

    conn.set("rtc_domain", rtc_domain)
    logger.info(conn.get("rtc_domain"))
    conn.delete("rtc_domain")
    logger.info(conn.get("rtc_domain"))

    time_interval_s = 60
    current_time = datetime.now(timezone('UTC'))
    last_time_slot = current_time - timedelta(seconds=time_interval_s)
    #"rtc-uk.fanyamin.com_5288119
    pools_last_5min = "{}_{}".format(rtc_domain, int(last_time_slot.timestamp()/time_interval_s))
    #pools_last_5min = "rtc-uk.fanyamin.com_26460264"
    logger.info("key= {} for {}".format(pools_last_5min, last_time_slot.timestamp()))
    values = conn.hgetall(pools_last_5min)
    if values:
        logger.info("{} exists".format(pools_last_5min))
    else:
        logger.info("{} not exists".format(pools_last_5min))

    #conn.hsetnx(pools_last_5min, pool_name + "_success_count",0)
    conn.hsetnx(pools_last_5min, pool_name + "_failure_count", 0)
    conn.hsetnx(pools_last_5min, pool_name + "_satisfied_count", 0)
    conn.hsetnx(pools_last_5min, pool_name + "_tolerating_count", 0)
    conn.hsetnx(pools_last_5min, pool_name + "_frustrated_count", 0)
    conn.expire(pools_last_5min, 300)

    #conn.hincrby(pools_last_5min,pool_name + "_success_count", 1)
    conn.hincrby(pools_last_5min, pool_name + "_failure_count", 1)
    conn.hincrby(pools_last_5min, pool_name + "_frustrated_count", 1)
    conn.hincrby(pools_last_5min, pool_name + "_frustrated_count", 1)
    logger.info("---  hgetall {}---".format(pools_last_5min))
    values = conn.hgetall(pools_last_5min)
    for key, value in values.items():
        logger.info("{}={}".format(key, value))


def test_hashget(prefix, timeslot):
    client = RedisClient("192.168.56.113:7001,192.168.56.114:7001,192.168.56.115:7001", 'pass1234')
    conn = client.connect()
    logger.info("--- test_hashget ---")
    key = "{}_{}".format(prefix, timeslot)
    logger.info("key is " + key)
    values = conn.hgetall(key)
    for key, value in values.items():
        logger.info("{}={}".format(key, value))

def main():
    current_time = datetime.now(timezone('UTC'))
    time_slot = int(current_time.timestamp() / 60)
    prefix = "rtc.qa.fanyamin.com_create_session_apdex"
    print("{}_{}".format(prefix, time_slot))

    test_hashset()
    test_hashget(prefix, time_slot)

if __name__ == "__main__":
    main()
