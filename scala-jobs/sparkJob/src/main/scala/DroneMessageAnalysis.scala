import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.DataFrame

object DroneMessageAnalysis {
  def main(args: Array[String]) {
    val spark = SparkSession.builder.appName("Drone Message Analysis").getOrCreate()

    def writeToS3csv(dataframe: DataFrame, directory: String) {
      dataframe.write
        .option("header","true")
        .mode(SaveMode.Overwrite)
        .csv(s"s3://prestacop-analysis-results/${directory}")
    }

    spark.sql("use prestacop")

    val most_common_violations = spark.sql("select violation_code, count(*) as count from records where is_violation = true group by violation_code order by count desc limit 10")
    writeToS3csv(most_common_violations, "most_common_violations")

    val most_noticed_car_makers = spark.sql("select car_maker, count(*) as count from records where is_violation = true group by car_maker order by count desc limit 10")
    writeToS3csv(most_noticed_car_makers, "most_noticed_car_makers")

    val most_active_hours = spark.sql("select date_format(timestamp,'HH') as hour, count(1) as count from records where is_violation = true group by 1 order by hour asc")
    writeToS3csv(most_active_hours, "most_active_hours")

    val most_active_weekdays = spark.sql("select date_format(timestamp,'u') as weekday, count(1) as count from records where is_violation = true group by 1 order by count desc")
    writeToS3csv(most_active_weekdays, "most_active_weekdays")

    spark.stop()
  }
}