package stats.medcouple

import breeze.linalg.{DenseVector}

import stats.medcouple.util.Medcouple

object TestAlgos extends Serializable {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / 1000000 + "ms")
    result
  }

  val testArrs: Seq[Array[Double]] = Seq(
    Array(0D,1D,2D,2D,3D),
    Array(0.2, 0.17, 0.08, 0.16, 0.88, 0.86, 0.09, 0.54, 0.27, 0.14),
    Array.fill(10)(1.0) ++ Array.fill(3)(5.0),
    Array.fill(480)(10.0) ++ (1 to 20).toArray.map{_.toDouble},
    DenseVector.rand(500).toArray
  )

  def main(args: Array[String]): Unit = {
    println(testArrs.map{arr => 
      Medcouple.calcTab(arr)
    }.mkString(", "))
    println(testArrs.map{arr => 
      Medcouple.calcMatrix(arr)
    }.mkString(", "))
    println(testArrs.map{arr => 
      Medcouple.calcLoop(arr)
    }.mkString(", "))

    val nTrials: Int = 200

    time {
      (1 to nTrials).foreach{_ => 
        testArrs.foreach{arr => 
          Medcouple.calcTab(arr)
        }
      }
    }

    time {
      (1 to nTrials).foreach{_ => 
        testArrs.foreach{arr => 
          Medcouple.calcMatrix(arr)
        }
      }
    }

    time {
      (1 to nTrials).foreach{_ => 
        testArrs.foreach{arr => 
          Medcouple.calcLoop(arr)
        }
      }
    }

  }

}
