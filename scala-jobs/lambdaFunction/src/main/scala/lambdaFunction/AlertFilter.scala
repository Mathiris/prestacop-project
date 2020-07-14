package lambdaFunction

import play.api.libs.json.Json
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.{Record, Result}

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

class AlertFilter {
  def handleRequest(input: KinesisFirehoseEvent): KinesisAnalyticsInputPreprocessingResponse = {
    val results = input.getRecords.asScala.map(record => (record, Json.parse(record.getData.array).as[Message]))
      .map(tuple => {
        val result = new Record()
        result.setRecordId(tuple._1.getRecordId)
        if (tuple._2.is_violation && tuple._2.violation_code == 100) {
          result.setResult(Result.Ok)
        } else {
          result.setResult(Result.Dropped)
        }
        result.setData(tuple._1.getData)
        result
      })

    val event_result = new KinesisAnalyticsInputPreprocessingResponse()
    event_result.setRecords(results.asJava)
    event_result
  }
}
