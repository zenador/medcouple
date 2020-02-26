package stats.medcouple.util

import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.numerics.{I}

object BreezeHelper extends Serializable {
    
  def where(condition: DenseMatrix[Boolean], x: DenseMatrix[Double], y: DenseMatrix[Double]): DenseMatrix[Double] = {
    // cannot handle NaNs anywhere in condition, x, y; must handle it before using this method
    val boolMask: DenseMatrix[Double] = I(condition)
    val reverseBoolMask: DenseMatrix[Double] = I(!condition)
    // val reverseBoolMask: DenseMatrix[Double] = DenseMatrix.ones[Double](boolMask.rows, boolMask.cols) - boolMask
    (boolMask *:* x) +:+ (reverseBoolMask *:* y)
  }

  def makeMeshGrid(row: DenseVector[Double], column: DenseVector[Double]): (DenseMatrix[Double], DenseMatrix[Double]) = {
    val rowMeshGrid = DenseVector.ones[Double](column.length) * row.t
    val columnMeshGrid = column * DenseVector.ones[Double](row.length).t
    (rowMeshGrid, columnMeshGrid)
  }

  def makeMeshGridInt(row: DenseVector[Int], column: DenseVector[Int]): (DenseMatrix[Int], DenseMatrix[Int]) = {
    val rowMeshGrid = DenseVector.ones[Int](column.length) * row.t
    val columnMeshGrid = column * DenseVector.ones[Int](row.length).t
    (rowMeshGrid, columnMeshGrid)
  }

  // convenience wrapper, use the mesh grids directly if you are doing multiple operations with the same vectors

  def outerSum(row: DenseVector[Double], column: DenseVector[Double]): DenseMatrix[Double] = { 
    val rowMeshGrid = DenseVector.ones[Double](column.length) * row.t
    val columnMeshGrid = column * DenseVector.ones[Double](row.length).t
    rowMeshGrid + columnMeshGrid
  }

}
