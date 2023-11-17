import base64
import json
import datetime
import os, sys
sys.path.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), os.pardir))
import SearchUtil as util
import SearchConfig as cfg
import FileLogger

import requests


logger = util.create_logger("OauthClient")



class OauthConfig:

    def __init__(self, configDict):
        self.accessTokenUrl = configDict.get('accessTokenUrl')
        self.scope = configDict.get('scope')
        self.clientId = configDict.get('clientId')
        self.clientSecret = configDict.get('clientSecret')

class OauthClient:

    def getOauthToken(self, oauthConfig, useSelfContainedToken = False):

        data = oauthConfig.clientId + ':' + oauthConfig.clientSecret

        encoded_data = base64.b64encode(data.encode())
        encoded_str = 'Basic ' + encoded_data.decode()

        # retrieve token from CI #'self_contained_token': True
        #post_data = {'grant_type': 'client_credentials', 'scope': oauthConfig.scope,  }
        post_data = {'grant_type': 'client_credentials', 'scope': oauthConfig.scope, 'self_contained_token': useSelfContainedToken }

        response = requests.post(oauthConfig.accessTokenUrl,
                                 headers={'Authorization': encoded_str},
                                 data=post_data)

        if response.status_code >= 200 and response.status_code < 300:
            token_content = json.loads(response.text)
            logger.info("token content is {}".format(token_content))
            return token_content['access_token']
        else:
            logger.error("get apptoken error, response code is %d" % ( response.status_code))
            return ""

    def get(self, accessToken, url, params={}, tokenPrefix = 'Bearer '):

        response = requests.get(url,
                                headers={
                                         'Content-Type': 'application/json',
                                         'Authorization': accessToken
                                         },
                                verify=False)

        content = ""
        if response.status_code >= 200 and response.status_code < 300:
            content = response.text
            #logger.info(str(json.dumps(results, indent=4, sort_keys=True)))
        else:
            logger.error("get %s error, response code is %d: %s" % (url, response.status_code, response.text))

        return response.status_code, content





