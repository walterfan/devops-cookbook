import json
import datetime
import os, sys
import requests
import argparse

import threading

from cassandra import ConsistencyLevel
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
from cassandra.query import SimpleStatement

from cassandra.policies import DCAwareRoundRobinPolicy

class CassandraClient(object):
    __auth_provider = PlainTextAuthProvider( username='test', password='pass')

    __load_balancing_policy = DCAwareRoundRobinPolicy("DC1")

    __global_keyspace = 'ks_walter_global'
    
    __local_keyspace = 'ks_rtc_local_cn'

    __cld_keyspace = 'ks_cld_local_us'

    __instance = None

    @classmethod
    def get(cls):
        if cls.__instance is None:
            cls.__instance = CassandraClient()
        return cls.__instance

    def __init__(self, hosts):
        self.__cluster = Cluster(
            contact_points=hosts,
            port = 9042,
            load_balancing_policy = self.__load_balancing_policy,
            auth_provider = self.__auth_provider,
            connect_timeout = 30)

        self.__session = self.__cluster.connect()
        self.__session.set_keyspace(self.__local_keyspace)
        self.__session.default_timeout = 30
        self.__lock = threading.Lock()

    def __del__(self):
        self.__cluster.shutdown()

    def version(self):
        with self.__lock:
            query = 'select * from rtcdatabase'
            command = self.__session.prepare(query)
            results = self.__session.execute(command)
            for row in results:
                print(row)
            return None


    def get_rtc_pool_usage(self, keyspace='ks_rtc_local_cn', rtcDomainName=None):
        with self.__lock:
            query = "select rtcdomainname, rtcpoolname, rtcpoolusage from rtcpoolinfo"

            if(rtcDomainName):
                query = query + " where rtcdomainname='%s'" % rtcDomainName

            self.__session.set_keyspace(keyspace=keyspace)
            command = self.__session.prepare(query)
            results = self.__session.execute(command)
            self.__mdLogger.printTableTitle(['rtcdomainname', 'rtcpoolname', 'rtcpoolusage'])
            for row in results:
                self.__mdLogger.printTableRow([row[0], row[1], row[2]])
            return None

    def delete_rtc_pool_usage(self, rtc_domain, keyspace='ks_rtc_local_cn'):
        with self.__lock:
            query = "delete from rtcpoolinfo where rtcdomainname=?"
            self.__session.set_keyspace(keyspace=keyspace)
            command = self.__session.prepare(query)
            self.__session.execute(command, [rtc_domain])


    def insert_rtc_pool_usage(self, keyspace='ks_rtc_local_cn', resultset=[]):
        with self.__lock:
            query = "insert into rtcpoolinfo (rtcdomainname, rtcpoolname, rtcpoolusage, lastmodifiedtime) values(?,?,?,?) USING TTL 86400"

            self.__session.set_keyspace(keyspace=keyspace)
            command = self.__session.prepare(query)

            for record in resultset:
                self.__session.execute(command, record)




if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument('--case', action='store', dest='case', help='do a route policy testing')
    parser.add_argument('--space', action='store', dest='space', help='specify the keyspace')
    parser.add_argument('--domain', action='store', dest='domain', help='specify the rtc domain')

    args = parser.parse_args()

    print(args)

    if(args.case == 'usage'):
        rtcDomainList = [
            { 'rtc-us.qa.fanyamin.com': 'ks_rtc_local_us'},
            { 'rtc-emea.qa.fanyamin.com': 'ks_rtc_local_uk'},
            { 'rtc-apac.qa.fanyamin.com': 'ks_rtc_local_cn'}

        ]

        for pair in rtcDomainList:
            for key, value in pair.items():
                print("\n--- %s ---\n" % key)
                get_rtc_pool_usage(key, value)


