
package lambdaFunction

import play.api.libs.json.Json
import org.joda.time.format.DateTimeFormat
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.sqs.model.SendMessageBatchRequest
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry
import com.amazonaws.services.sqs.AmazonSQSClientBuilder

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

// This class is not used at the moment
class AlertDetector {
  def handleRequest(input: KinesisEvent) = {
    val records = input.getRecords.asScala.map(record => record.getKinesis.getData)

    val alerts = records.map(record => {
      println(s"Record raw: ${record.array}")
      println(s"Json: ${Json.parse(record.array)}")
      println(s"Message opt parsed: ${Json.parse(record.array).asOpt[Message].getOrElse("Failed to parse as Message")}")
      println(s"Message parsed: ${Json.parse(record.array).as[Message]}")
      Json.parse(record.array).as[Message]
    })
      .filter(message => message.is_violation && message.violation_code == 100)
      .map(message => {
        val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

        println(s"Date format: ${dateTimeFormat.parseMillis(message.timestamp)}")
        val timeMillis = dateTimeFormat.parseMillis(message.timestamp)
        println(s"Id format: ${message.id}-${timeMillis}")
        new SendMessageBatchRequestEntry(s"${message.id}-${timeMillis}", Json.toJson(message).toString)
      })

    val sqs = AmazonSQSClientBuilder.defaultClient
    val send_batch_request = new SendMessageBatchRequest().withQueueUrl("https://sqs.eu-west-3.amazonaws.com/299141443499/drone-alerts.fifo")
      .withEntries(alerts.asJava)
    sqs.sendMessageBatch(send_batch_request)
  }
}
