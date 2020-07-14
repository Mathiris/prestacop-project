package fr.data.spark

import com.amazonaws.regions.Regions
import jp.co.bizreach.kinesis
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions._
import jp.co.bizreach.kinesis.spark._
import org.apache.spark.metrics.source
import org.mortbay.util.ajax.JSON.Source


object DataFrame {

  def main(args: Array[String]): Unit = {
    println("Hello, world!")

    val spark = org.apache.spark.sql.SparkSession.builder
      .master("local")
      .appName("Spark CSV Reader")
      .getOrCreate;

    val df = spark.read
      .format("csv")
      .option("header", "true") //first line in file has headers
      .option("mode", "DROPMALFORMED")
      .load("Test_30_rows_kinesis.csv")

    val df_clean = df.select("Issue Date", "Violation Code", "Vehicle Make", "Violation Time")
    df_clean.show(10)

    var test_split_date = df.select(split(col("Issue Date"),"/").getItem(0).as("Day"),
      split(col("Issue Date"),"/").getItem(1).as("Month"),
      split(col("Issue Date"),"/").getItem(2).as("Year"))
    //test_split_date.show(10)

    var test_split_hour = df.select(
      when(substring(col("Violation Time"),-1, 1).equalTo("A"),substring((col("Violation Time")),0,2))
        .when(substring(col("Violation Time"),0,2).equalTo("12"), "00")
        .otherwise((substring(col("Violation Time"),0,2).cast("Int") + 12).cast("String"))
        .as("hour")
    )

    var test_split_minute = df.select(substring(col("Violation Time"),3,2).as("minute"))

    var df_final = df.select(
      col("Violation Code").as("violation_code"),
      col("Vehicle Make").as("car_maker"),
      split(col("Issue Date"),"/").getItem(0).as("Day"),
      split(col("Issue Date"),"/").getItem(1).as("Month"),
      split(col("Issue Date"),"/").getItem(2).as("Year"),
      when(substring(col("Violation Time"),-1, 1).equalTo("A"),substring((col("Violation Time")),0,2))
        .when(substring(col("Violation Time"),0,2).equalTo("12"), "00")
        .otherwise((substring(col("Violation Time"),0,2).cast("Int") + 12).cast("String"))
        .as("Hour"),
      substring(col("Violation Time"),3,2).as("Minute")

    ).select(
      concat(
        col("Year"),lit("-"),
        col("Month"),lit("-"),
        col("Day"),lit("T"),
        col("Hour"),lit(":"),
        col("Minute"),lit(":00.000Z")
      ).as("timestamp"),
      col("violation_code"),
      col("car_maker"))


    df_final=df_final.withColumn("id",lit(0))
      .withColumn("longitude",lit(0))
      .withColumn("latitude",lit(0))
      .withColumn("altitude",lit(0))
      .withColumn("battery_level",lit(0))
      .withColumn("humidity",lit(0))
      .withColumn("light_level",lit(0))
      .withColumn("is_violation",lit(true))
      .withColumn("image_id",lit("None"))


    df_final=df_final.select("id","longitude","latitude","altitude","timestamp","battery_level","humidity","light_Level","is_violation","violation_code","image_id","car_maker")
    df_final.show(10)
    //df_final.printSchema()
    val jsondata = df_final.toJSON
    df_final.coalesce(1).write.json("./dataset.json")
    //jsondata.show(10)

    //val rdd: RDD[Row] =df_final.rdd
    //val rows: org.apache.spark.rdd.RDD[org.apache.spark.sql.Row] = df_final.rdd

    val rdd: RDD[String]=jsondata.rdd//.map(Json.parse(_).toString())
    rdd.saveToKinesis(
    streamName = "spark-drone-message-stream",
      region     = Regions.EU_WEST_3,
      chunk      = 30
    )
     spark.stop
  }

}