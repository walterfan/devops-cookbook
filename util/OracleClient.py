import cx_Oracle
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class OracleClient:
    '''
    Query Oracle need oracle instant client , and set oracle home firstly
    '''

    def __init__(self, *args, **kwargs):

        cfg = dict(kwargs)
        self.host = cfg.get('host')
        self.port = cfg.get('port')
        self.name = cfg.get('name')
        self.username = cfg.get('username')
        self.password = cfg.get('password')

        self.conn = None
        self.connect()


    def connect(self):
        self.conn = cx_Oracle.connect(self.username, self.password, cx_Oracle.makedsn(self.host, self.port, self.name));

    def close(self):
        if self.conn:
            self.conn.close()
            self.conn = None

    def execute(self, sql):
        cur = self.conn.cursor()

        cur.execute(sql)
        cur.close()

    def queryOne(self, sql):
        cur = self.conn.cursor()
        print("query: %s" % sql)
        cur.execute(sql)
        row = cur.fetchone()
        logger.debug(row)
        cur.close()
        return row

    def queryAll(self, sql):
        cur = self.conn.cursor()
        print("query: %s" % sql)
        cur.execute(sql)
        rows = cur.fetchall()
        logger.debug(rows)
        cur.close()
        return rows

    def __del__(self):
        self.close()

