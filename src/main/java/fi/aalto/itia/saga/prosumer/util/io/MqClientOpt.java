package fi.aalto.itia.saga.prosumer.util.io;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class MqClientOpt {

	private Connection connection;
	private Channel channel;
	private String requestQueueName = "Matlab";
	private String replyQueueName;
	private QueueingConsumer consumer;

	public MqClientOpt(String queueName) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel = connection.createChannel();

		replyQueueName = queueName;

		consumer = new QueueingConsumer(channel);
		channel.queueDeclare(replyQueueName, false, false, false, null);
		channel.basicConsume(replyQueueName, true, consumer);
	}

	public String call(String message) throws Exception {
		String response = null;
		String corrId = java.util.UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrId).replyTo(replyQueueName).build();

		channel.basicPublish("", requestQueueName, props, message.getBytes());

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response = new String(delivery.getBody());
				break;
			}
		}

		return response;
	}

	public void close() throws Exception {
		connection.close();
	}
}
