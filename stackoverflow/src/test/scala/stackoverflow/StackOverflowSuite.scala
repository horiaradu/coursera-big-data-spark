package stackoverflow

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import java.io.File

@RunWith(classOf[JUnitRunner])
class StackOverflowSuite extends FunSuite with BeforeAndAfterAll {


  lazy val testObject = new StackOverflow {
    override val langs =
      List(
        "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
        "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

    override def langSpread = 50000

    override def kmeansKernels = 45

    override def kmeansEta: Double = 20.0D

    override def kmeansMaxIterations = 120
  }

  lazy val postingsStrings = Seq(
    "1,27233496,,,0,C#",
    "1,23698767,,,9,C#",
    "1,5484340,,,0,C#",
    "2,5494879,,5484340,1,",
    "1,9419744,,,2,Objective-C",
    "1,26875732,,,1,C#",
    "1,9002525,,,2,C++",
    "2,9003401,,9002525,4,",
    "2,9003942,,9002525,1,",
    "2,9005311,,9002525,0,",
    "1,5257894,,,1,Java",
    "1,21984912,,,0,Java"
  )

  override def afterAll(): Unit = {
    StackOverflow.sc.stop()
  }

  test("testObject can be instantiated") {
    val instantiatable = try {
      testObject
      true
    } catch {
      case _: Throwable => false
    }
    assert(instantiatable, "Can't instantiate a StackOverflow object")
  }

  private val stackOverflow = new StackOverflow()

  test("rawPostings") {
    val postingStringsRDD = StackOverflow.sc.parallelize(postingsStrings)
    val postingsRDD = stackOverflow.rawPostings(postingStringsRDD)
    val postings = postingsRDD.collect()
    assert(postings.length == 12)
  }

  test("groupedPostings") {
    val postingStringsRDD = StackOverflow.sc.parallelize(postingsStrings)
    val groupedPostingsRDD = stackOverflow.groupedPostings(stackOverflow.rawPostings(postingStringsRDD))

    val groupedPostings = groupedPostingsRDD.collect()
    assert(groupedPostings.length == 2)
  }

  test("scoredPostings") {
    val postingStringsRDD = StackOverflow.sc.parallelize(postingsStrings)
    val scoredPostingsRDD = stackOverflow.scoredPostings(stackOverflow.groupedPostings(stackOverflow.rawPostings(postingStringsRDD)))

    val scoredPostings = scoredPostingsRDD.collect()
    assert(scoredPostings.length == 2)
  }

  test("vectorPostings") {
    val postingStringsRDD = StackOverflow.sc.parallelize(postingsStrings)
    val vectorPostingsRDD = stackOverflow.vectorPostings(stackOverflow.scoredPostings(stackOverflow.groupedPostings(stackOverflow.rawPostings(postingStringsRDD))))

    val vectorPostings = vectorPostingsRDD.collect()
    assert(vectorPostings.length == 2)
  }
}
