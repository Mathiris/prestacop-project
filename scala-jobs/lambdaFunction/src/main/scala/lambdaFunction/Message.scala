package lambdaFunction

import play.api.libs.json.Json

case class Message(id: Int,
                   longitude: Float,
                   latitude: Float,
                   altitude: Float,
                   timestamp: String,
                   battery_level: Int,
                   humidity: Float,
                   light_level: Float,
                   is_violation: Boolean,
                   violation_code: Int,
                   image_id: String,
                   car_maker: String)

object Message {
  implicit val messageReads = Json.reads[Message]
  implicit val messageJsonFormat = Json.format[Message]
}