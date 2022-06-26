import os
import time

SLEEP_TIME_IN_MS = 60

def run_cmd(cmd):
    print("run %s" % cmd)
    return os.system(cmd)

print("refer to https://wiki.cisco.com/display/PAN/How+to+block+ports+on+Mac")

cmd1 = "sudo pfctl -ef /etc/pf443.conf"
cmd2 = "sudo pfctl -sr"
cmd3 = "sudo pfctl -d"
cmd4 = "sudo pfctl -ef /etc/pf.conf"


ret = run_cmd(cmd1)
ret = run_cmd(cmd2)

time.sleep(SLEEP_TIME_IN_MS)

ret = run_cmd(cmd3)
ret = run_cmd(cmd4)
