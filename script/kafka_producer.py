from confluent_kafka import Producer, Consumer, KafkaError

# Kafka configuration
bootstrap_servers = '192.168.104.58:9092'
group_id = 'walter'
username = 'walter'
password = 'pass1234'
topic = 'waltertest'

# Producer configuration
producer_conf = {
    'bootstrap.servers': bootstrap_servers,
    'sasl.mechanism': 'PLAIN',
    'security.protocol': 'SASL_SSL',
    'sasl.username': username,
    'sasl.password': password,
}

# Consumer configuration
consumer_conf = {
    'bootstrap.servers': bootstrap_servers,
    'group.id': group_id,
    'sasl.mechanism': 'PLAIN',
    'security.protocol': 'SASL_SSL',
    'sasl.username': username,
    'sasl.password': password,
    'auto.offset.reset': 'earliest',  # You can set to 'latest' if you want to start from the latest offset
}

def produce_message(producer, topic, message):
    producer.produce(topic, value=message)
    producer.flush()

def consume_messages(consumer, topic):
    consumer.subscribe([topic])

    while True:
        msg = consumer.poll(1.0)

        if msg is None:
            continue
        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                # End of partition event
                continue
            else:
                print(msg.error())
                break

        print('Received message: {}'.format(msg.value().decode('utf-8')))

if __name__ == "__main__":
    # Create a Kafka producer
    producer = Producer(producer_conf)

    # Produce a message
    message_to_send = "Hello, Kafka!"
    produce_message(producer, topic, message_to_send)
    print('Message sent: {}'.format(message_to_send))

    # Create a Kafka consumer
    consumer = Consumer(consumer_conf)

    # Consume messages
    consume_messages(consumer, topic)
