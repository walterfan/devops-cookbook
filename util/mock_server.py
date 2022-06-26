import os
import json
import requests
from flask_httpauth import HTTPBasicAuth
from flask import make_response
from flask import Flask
from flask import Response
from flask import request
from werkzeug.exceptions import NotFound, ServiceUnavailable


DEFAULT_PORT = 443

app = Flask(__name__)

current_path = os.path.dirname(os.path.realpath(__file__))
data_path = os.path.join("data")


def read_data(json_file):
    json_fp = open(json_file, "r")
    return json.load(json_fp)

def generate_response(arg, contentType="application/json" , response_code=200):
    response = make_response(json.dumps(arg, sort_keys = True, indent=4))
    response.headers['Content-type'] = contentType
    response.headers['ETag'] = "0fe9603de45d558b3b8706ebc26a20329"
    response.status_code = response_code
    return response

@app.route('/', methods=['GET'])
def checkhealth():
    return generate_response({ "status": "OKOKOK"})


if __name__ == "__main__":
    app.run(port=DEFAULT_PORT, debug=True)
