from kafka import KafkaProducer, KafkaConsumer
import json
import sys

# Kafka configuration
bootstrap_servers = 'localhost:9092'
security_bootstrap_servers = '192.168.104.58:9092'
topic = 'filebeat'
group_id = 'waltertest'
username = 'walter'
password = 'pass1234'
kafka_api_version = (0, 10) 
# Producer configuration
producer_conf = {
    'bootstrap_servers': bootstrap_servers,
}

# Consumer configuration
consumer_conf = {
    'bootstrap_servers': bootstrap_servers,
    'group_id': 'test-group',
    'auto_offset_reset': 'earliest',  # You can set to 'latest' if you want to start from the latest offset
}

# Producer configuration
security_producer_conf = {
    'bootstrap_servers': security_bootstrap_servers,
    'sasl_plain_username': username,
    'sasl_plain_password': password,
    'security_protocol': 'SASL_SSL',
    'sasl_mechanism': 'PLAIN',
    'api_version': kafka_api_version,
}

# Consumer configuration
security_consumer_conf = {
    'bootstrap_servers': security_bootstrap_servers,
    'group_id': 'test-group',
    'auto_offset_reset': 'earliest',  # You can set to 'latest' if you want to start from the latest offset
    'sasl_plain_username': username,
    'sasl_plain_password': password,
    'security_protocol': 'SASL_SSL',
    'sasl_mechanism': 'PLAIN',
    'api_version': kafka_api_version,
}

def produce_message(producer, topic, count):
    for i in range(count):
        message_to_send = {f"key {i}": f"value {i}"}

        print('Message sent: {}'.format(message_to_send))
        
        producer.send(topic, value=json.dumps(message_to_send).encode('utf-8'))
        producer.flush()

def consume_messages(consumer, topic, count):
    consumer.subscribe([topic])
    i = 0
    while i < count:
        messages = consumer.poll(timeout_ms=1000)

        for _, msg_list in messages.items():
            for msg in msg_list:
                print('Received message: {}'.format(msg.value.decode('utf-8')))
                i = i + 1
        

if __name__ == "__main__":

    the_producer_conf = producer_conf
    the_consumer_conf = consumer_conf

    if len(sys.argv) > 1:
        print("security connection")
        the_producer_conf = security_producer_conf
        the_consumer_conf = security_consumer_conf

    count = 10

    # Create a Kafka producer
    producer = KafkaProducer(**the_producer_conf)

    # Produce a message
    
    produce_message(producer, topic, count)

    # Create a Kafka consumer
    consumer = KafkaConsumer(**the_consumer_conf)

    # Consume messages
    consume_messages(consumer, topic, count)
