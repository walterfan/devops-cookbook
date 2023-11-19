#!/usr/bin/env python3
import sys
import argparse
import os
import json
import time
import subprocess
import flask
from flask_httpauth import HTTPBasicAuth
from flask import make_response
from flask import Flask
from flask import Response
from flask import request
from werkzeug.exceptions import NotFound, ServiceUnavailable

from loguru import logger

logger.add(sys.stdout,
           format="{time} {message}",
           filter="client",
           level="INFO")

DEFAULT_PORT = 9443

app = Flask(__name__)

current_path = os.path.dirname(os.path.realpath(__file__))

COMMAND_JSON_FILE = "{}/commands.json".format(current_path)
COMMANDS_API_PATH = '/api/v1/commands'
COMMANDS_WHITE_LIST = ['docker', 'kill']
REQUEST_VARS_KEY = 'per_request_vars'
REQUEST_VARS_ITEMS = ['tracking_id', 'user_id']
TRACKING_ID = "tracking_id"

executed_commands = []

def set_my_request_var(name, value):
    if REQUEST_VARS_KEY not in flask.g:
        flask.g.per_request_var = {}
    flask.g.per_request_var[name] = value

def get_my_request_var(name):
    if REQUEST_VARS_KEY not in flask.g:
        return ''
    return flask.g.per_request_var.get(name, '')

@app.after_request
def per_request_callbacks(response):
    values = flask.g.get(REQUEST_VARS_KEY, {})
    for item_name in REQUEST_VARS_ITEMS:
        if item_name in values:
            values.pop(item_name)

    return response

def read_data():
    start = time.time()

    if not os.path.exists(COMMAND_JSON_FILE):
        save_data(executed_commands)

    with open(COMMAND_JSON_FILE) as json_fp:
        return json.load(json_fp)
    logger.info("read_data: %d, %s", time.time() - start, get_my_request_var(TRACKING_ID))

def save_data(commands):
    start = time.time()

    with open(COMMAND_JSON_FILE, "w") as json_fp:
        json.dump(commands, json_fp, sort_keys=True, indent=4)
    logger.info("save_data: %d, %s", time.time() - start, get_my_request_var(TRACKING_ID))

def execute_command(name, args):
    cmd_with_args = [ name ]
    if ' ' in args:
        cmd_with_args + [x.strip() for x in args.split(' ')]
    else:
        cmd_with_args.append(args.strip())

    result = subprocess.run(cmd_with_args,
        stdout = subprocess.PIPE,
        stderr = subprocess.PIPE,
        universal_newlines = True
    )
    return result

def generate_response(arg, contentType="application/json" , response_code=200):
    response = make_response(json.dumps(arg, sort_keys = True, indent=4))
    response.headers['Content-type'] = contentType
    response.status_code = response_code
    return response

@app.route('/', methods=['GET'])
def checkhealth():
    return generate_response({ "status": "OKOKOK", 
                              "url": f"{request.base_url}api/v1/commands"})

@app.route(COMMANDS_API_PATH, methods=['GET'])
def list_command():
    trackingId = request.headers.get(TRACKING_ID, "")
    logger.info("list command: trackingId=".format(trackingId))
    commands = read_data()
    return generate_response(commands)

@app.route(COMMANDS_API_PATH, methods=['POST'])
def create_command():
    command = request.json
    trackingId = request.headers.get(TRACKING_ID, "")
    logger.info("create command: {}, trackingId=".format(command, trackingId))
    name = command.get("name")
    args = command.get("args")
    if name in COMMANDS_WHITE_LIST:
        ret = execute_command(name, args)
        command["result"] = ret.stdout
        if ret.stderr:
            command["result"] = ret.stderr
    else:
        logger.warning(f"forbidden command {name}, {args}")
        command["result"] = "error"
        command["error"] = f"{name} is forbidden"

    commands = read_data()
    commands.append(command)
    save_data(commands)
    return generate_response(command)


"""
http --auth walter:pass --json POST http://localhost:5000/api/v1/commands name=docker args=ps 
"""
if __name__ == "__main__":

    parser = argparse.ArgumentParser()

    parser.add_argument('-p', required=True, action='store', dest='port', default=5000,  help='port')
    parser.add_argument('-d', required=True, action='store', dest='debug', default=True,  help='specify debug flag')
 
    args = parser.parse_args()

    app.run(port=args.port, debug=args.debug)