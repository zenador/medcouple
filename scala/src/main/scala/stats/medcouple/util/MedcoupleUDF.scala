package stats.medcouple.util

import org.apache.spark.sql.functions.{udf}

object MedcoupleUDF {

  val mc = udf((xs: Seq[Double]) => {
    Medcouple.calc(xs.toArray)
  })

}
