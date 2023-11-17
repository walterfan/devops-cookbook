
import requests
"""
curl 'https://weibo.com/u/1059139255?tabtype=article' \
  -H 'authority: weibo.com' \
  -H 'accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7' \
  -H 'accept-language: en-US,en;q=0.9,zh;q=0.8' \
  -H 'cache-control: max-age=0' \
  -H 'cookie: XSRF-TOKEN=zwK_MMB9vFUXToiMaiW7ncdW; _s_tentry=weibo.com; Apache=3305446595967.576.1697730135019; SINAGLOBAL=3305446595967.576.1697730135019; ULV=1697730135020:1:1:1:3305446595967.576.1697730135019:; login_sid_t=e3bb8cc5cc6b8d9679c2f0dc7e443d5d; cross_origin_proto=SSL; SCF=AraSKgSNwsZ3lqVPUwC9JIcajJAW-nJ74nJmMAWY--WahDu4_HF_VEKbKYEfIdVmWi4RQLFVfxTy9dVbyxwLxMA.; ALF=1700614629; SSOLoginState=1698073856; SUB=_2A25IMv1QDeRhGedO7lsQ8yfOzjmIHXVr3IMYrDV8PUJbkNAGLVHHkW1NXUMWvz0vU5G4gmD42Q4W0puYWVS9JKxl; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhaIUKhVozZFSqYp_ZWJ68a5NHD95Qpeh-4eKe4eo-fWs4Dqcj_i--NiK.0i-2ci--fi-z7iK.Xi--NiKn4i-z4i--ciKLhiKn4i--RiKyhiKn0; PC_TOKEN=cb34df3dd0; WBPSESS=xfaBjA5jkm0TM0f1b_J8bEbZSSZQe1yBuZSN86ooqdulQ5IpSC5VCX3YwbgRyErfVzPWj4upxBzG67Eztk--PKB4t2OOfABmr0H1ZBDKkoTviirKmXjAw6c-v8bLhT5XoejZywlYhVZdZ2gk9rVwTg==' \
  -H 'sec-ch-ua: "Google Chrome";v="117", "Not;A=Brand";v="8", "Chromium";v="117"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "macOS"' \
  -H 'sec-fetch-dest: document' \
  -H 'sec-fetch-mode: navigate' \
  -H 'sec-fetch-site: none' \
  -H 'sec-fetch-user: ?1' \
  -H 'upgrade-insecure-requests: 1' \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36' \
  --compressed
"""

def update_dict(aDict, aPair):
  ret = aPair.split(':')
  aDict[ret[0].strip()] = ret[1].strip()

url = 'https://weibo.com/u/1059139255?tabtype=article'
headers = {}
update_dict(headers, 'authority: weibo.com')
update_dict(headers, 'accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7')
update_dict(headers, 'accept-language: en-US,en;q=0.9,zh;q=0.8')
update_dict(headers, 'cache-control: max-age=0')
update_dict(headers, 'cookie: XSRF-TOKEN=zwK_MMB9vFUXToiMaiW7ncdW; _s_tentry=weibo.com; Apache=3305446595967.576.1697730135019; SINAGLOBAL=3305446595967.576.1697730135019; ULV=1697730135020:1:1:1:3305446595967.576.1697730135019:; login_sid_t=e3bb8cc5cc6b8d9679c2f0dc7e443d5d; cross_origin_proto=SSL; SCF=AraSKgSNwsZ3lqVPUwC9JIcajJAW-nJ74nJmMAWY--WahDu4_HF_VEKbKYEfIdVmWi4RQLFVfxTy9dVbyxwLxMA.; ALF=1700614629; SSOLoginState=1698073856; SUB=_2A25IMv1QDeRhGedO7lsQ8yfOzjmIHXVr3IMYrDV8PUJbkNAGLVHHkW1NXUMWvz0vU5G4gmD42Q4W0puYWVS9JKxl; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhaIUKhVozZFSqYp_ZWJ68a5NHD95Qpeh-4eKe4eo-fWs4Dqcj_i--NiK.0i-2ci--fi-z7iK.Xi--NiKn4i-z4i--ciKLhiKn4i--RiKyhiKn0; PC_TOKEN=cb34df3dd0; WBPSESS=xfaBjA5jkm0TM0f1b_J8bEbZSSZQe1yBuZSN86ooqdulQ5IpSC5VCX3YwbgRyErfVzPWj4upxBzG67Eztk--PKB4t2OOfABmr0H1ZBDKkoTviirKmXjAw6c-v8bLhT5XoejZywlYhVZdZ2gk9rVwTg==')
update_dict(headers, 'sec-ch-ua: "Google Chrome";v="117", "Not;A=Brand";v="8", "Chromium";v="117"')
update_dict(headers, 'sec-ch-ua-mobile: ?0')
update_dict(headers, 'sec-ch-ua-platform: "macOS"')
update_dict(headers, 'sec-fetch-dest: document')
update_dict(headers, 'sec-fetch-mode: navigate')
update_dict(headers, 'sec-fetch-site: none')
update_dict(headers, 'sec-fetch-user: ?1')
update_dict(headers, 'upgrade-insecure-requests: 1')
update_dict(headers, 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36')

params = (
    ('uid', '1059139255'),
    ('page', '2'),
    ('feature', '10'),
)

response = requests.get('https://weibo.com/ajax/statuses/mymblog', headers=headers, params=params)
print(response.text)