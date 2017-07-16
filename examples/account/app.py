from flask import Flask
import time 
import account


app = Flask(__name__)
	
@app.route("/")
def main():
   return "Hello World at " + time.time().__str__() + "\n";

if __name__ == "__main__":

	app.run(port=5000, debug=True)

