package stats.medcouple

import org.apache.spark.sql.{SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import stats.medcouple.util.MedcoupleUDF.mc

object TestSpark extends Serializable {

  def run(spark: SparkSession) = {
    import spark.implicits._

    val df = Seq(
      Tuple1(0D),
      Tuple1(1D),
      Tuple1(2D),
      Tuple1(2D),
      Tuple1(3D)
    ).toDF("values")

    df
      .groupBy()
      .agg(mc(collect_list(col("values"))).as("mc"))
      .show(false)
  }

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("app")
      .getOrCreate()
    try { run(spark) }
    finally { spark.stop() }
  }

}
