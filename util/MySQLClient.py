
"""
  
  pip install mysql-connector
  
"""

import os
import sys
import argparse


import mysql.connector


class MySQLClient(object):
    def __init__(self, username, password, hostname, dbname):
        self.username = username
        self.password = password
        self.hostname = hostname
        self.dbname = dbname

        self.dbconn = mysql.connector.connect(user=self.username,
                                              password=self.password,
                                              host=self.hostname,
                                              database=self.dbname)
        self.cursor = self.dbconn.cursor()

    def executeSqlFile(self, filename):
        fd = open(filename, 'r')
        sqlFile = fd.read()
        fd.close()

        # all SQL commands (split on ';')
        sqlCommands = sqlFile.split(';')

        # Execute every command from the input file
        for command in sqlCommands:
            # This will skip and report errors
            # For example, if the tables do not yet exist, this will skip over
            # the DROP TABLE commands
            try:
              if command.rstrip() != '':
                self.cursor.execute(command)
            except ValueError as msg:
                print("Command skipped: ", msg)


    def commit(self):
        self.dbconn.commit()

def main(arguments):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)

    parser.add_argument('--sqlfile', action='store', dest='sqlfile', help='do a route policy testing')
    parser.add_argument('--username', action='store', dest='username', help='specify the keyspace')
    parser.add_argument('--password', action='store', dest='password', help='specify the rtc domain')
    parser.add_argument('--hostname', action='store', dest='hostname', default='localhost', help='specify the rtc domain')
    parser.add_argument('--dbname', action='store', dest='dbname', default='scheduler', help='specify the rtc domain')

    args = parser.parse_args()

    print(args)

    if(args.sqlfile and args.username and args.password):
        mysqlClient = MySQLClient(args.username, args.password, args.hostname, args.dbname)
        mysqlClient.executeSqlFile(args.sqlfile)

    #python MySQLClient.py --sqlfile=scheme.sql --username=walter --password=pass1234

if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))