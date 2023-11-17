
"""
  pip install mysql-connector
"""

import os
import sys
import argparse


import mysql.connector


class MySQLClient(object):
    def __init__(self, username, password, hostname, dbname, socket_file="/opt/lampp/var/mysql/mysql.sock"):
        self.username = username
        self.password = password
        self.hostname = hostname
        self.dbname = dbname
        print(f"connect db {dbname} by {username}/{password}@{hostname}")
        if hostname:
            self.dbconn = mysql.connector.connect(user=self.username,
                                              password=self.password,
                                              host=self.hostname,
                                              database=self.dbname)
        else:
            self.dbconn = mysql.connector.connect(user=self.username,
                                              password=self.password,
                                              unix_socket=socket_file,
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
            self.executeSql(self, command)

    def executeSql(self, sql):
        try:
            if sql.rstrip() != '':
                self.cursor.execute(sql)
                result = self.cursor.fetchall()

                for x in result:
                    print(x)
        except ValueError as msg:
            print("Command skipped: ", msg)

    def commit(self):
        self.dbconn.commit()


    def close(self):
        self.cursor.close()
        self.dbconn.close()

def main(arguments):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)

    parser.add_argument('-s', '--sql', action='store', dest='sql', help='specify the sql statement or file')
    parser.add_argument('-u','--username', action='store', dest='username', help='specify the db username')
    parser.add_argument('-p','--password', action='store', dest='password', help='specify the db password')
    parser.add_argument('--hostname', action='store', dest='hostname', help='specify the db hostname')
    parser.add_argument('--socket_file', action='store', help='specify unix socket file')
    parser.add_argument('-n','--dbname', action='store', dest='dbname', default='dvwa', help='specify the db name')

    args = parser.parse_args()

    if(args.sql and args.username and args.password):
        mysqlClient = MySQLClient(args.username, args.password, args.hostname, args.dbname)
        if args.sql.endswith(".sql"):
            mysqlClient.executeSqlFile(args.sql)
        else:
            mysqlClient.executeSql(args.sql)
    else:
        parser.print_help()

if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))