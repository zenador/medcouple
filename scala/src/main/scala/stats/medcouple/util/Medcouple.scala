package stats.medcouple.util

import breeze.linalg.{DenseMatrix, DenseVector, reverse}
import breeze.numerics.signum

object Medcouple extends Serializable {

  // use this
  def calc(data: Array[Double]): Double = {
    calcLoop(data)
  }

  // just for example, not optimised
  def calcTab(data: Array[Double]): Double = {
    val sortedData = data.sorted
    val xs = new DenseVector(sortedData)
    val med = sortedMedian(sortedData)
    val zs = reverse(xs - med)
    val zis = zs(zs <:= 0.0).toDenseVector
    val zjs = zs(zs >:= 0.0).toDenseVector
    val same = {
      if (zs(zs :== 0.0).size > 0) {
        val zIndex = reverse(DenseVector.tabulate[Int](zs.length){identity})
        val zisIdx = zIndex(zs <:= 0.0).toDenseVector
        val zjsIdx = zIndex(zs >:= 0.0).toDenseVector
        val iIdx = DenseVector.ones[Int](zjsIdx.length) * zisIdx.t
        val jIdx = zjsIdx * DenseVector.ones[Int](zisIdx.length).t
        DenseMatrix.tabulate(zjs.length, zis.length){case (j, i) => if (jIdx(j, i) == iIdx(j, i)) 0D else Double.NaN}
      } else
        DenseMatrix.fill(zjs.length, zis.length){Double.NaN} // placeholder, not used
    }
    val res = DenseMatrix.tabulate(zjs.length, zis.length){case (j, i) =>
      val zj = zjs(j)
      val zi = zis(i)
      val den = zj - zi
      if (den == 0)
        same(j,i)
      else
        (zj + zi) / den
    }
    // println(res)
    sortedMedian(res.toArray.filter{x => !x.isNaN}.sorted)
  }

  // can try if this is faster (but probably not) only if you have native libraries for breeze installed on your system
  def calcMatrix(data: Array[Double]): Double = {
    val sortedData = data.sorted
    val xs = new DenseVector(sortedData)
    val med = sortedMedian(sortedData)
    val zs = reverse(xs - med)
    val zis = zs(zs <:= 0.0).toDenseVector
    val zjs = zs(zs >:= 0.0).toDenseVector
    val same = {
      if (zs(zs :== 0.0).size > 0) {
        val zIndex = reverse(DenseVector.tabulate[Int](zs.length){identity})
        val zisIdx = zIndex(zs <:= 0.0).toDenseVector
        val zjsIdx = zIndex(zs >:= 0.0).toDenseVector
        val (zisIdxM, zjsIdxM) = BreezeHelper.makeMeshGridInt(zisIdx, zjsIdx)
        signum(zjsIdxM -:- zisIdxM)
      } else {
        DenseMatrix.zeros[Double](zjs.length, zis.length) // placeholder, values are discarded, but matrix is still created and used in where method so don't use NaN
      }
    }
    val (zisM, zjsM) = BreezeHelper.makeMeshGrid(zis, zjs)
    val reg = (zjsM + zisM) / (zjsM - zisM)
    val res = BreezeHelper.where(reg.map(x => x.isNaN()), same, reg.map(x => if (x.isNaN()) 0 else x))
    // println(res)
    sortedMedian(res.toArray.sorted)
  }

  // fastest if you don't have native libraries installed, or perhaps even otherwise
  def calcLoop(data: Array[Double]): Double = {
    val sortedData = data.sorted
    val xs = new DenseVector(sortedData)
    val med = sortedMedian(sortedData)
    val zs = reverse(xs - med)
    val zis = zs(zs <:= 0.0).toDenseVector
    val zjs = zs(zs >:= 0.0).toDenseVector
    val (zjsIdx, zisIdx) = {
      if (zs(zs :== 0.0).size > 0) {
        val zIndex = reverse(DenseVector.tabulate[Int](zs.length){identity})
        val zisIdx = zIndex(zs <:= 0.0).toDenseVector
        val zjsIdx = zIndex(zs >:= 0.0).toDenseVector
        (zjsIdx, zisIdx)
      } else {
        (null, null) // placeholder, not used
      }
    }
    val res = DenseMatrix.zeros[Double](zjs.length, zis.length)
    for (i <- Range(0, zis.length)) {
      for (j <- Range(0, zjs.length)) {
        val zj = zjs(j)
        val zi = zis(i)
        val den = zj - zi
        res(j, i) = {
          if (den == 0)
            if (zjsIdx(j) == zisIdx(i)) 0 else Double.NaN
          else
            (zj + zi) / den
        }
      }
    }
    // println(res)
    sortedMedian(res.toArray.filter{x => !x.isNaN}.sorted)
  }

  def sortedMedian(sortedData: Array[Double]): Double = {
    val n = sortedData.length
    n match {
      case x if (x % 2 == 0) => (sortedData(n / 2 - 1) + sortedData(n / 2)) / 2
      case _ => sortedData((n - 1) / 2)
    }
  }

}
