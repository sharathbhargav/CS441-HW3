import com.Utilities.HelperUtils
import com.Utilities.HelperUtils.getIntervalTime
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.log_process1
import com.log_process1.pattern
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}

import java.util.Date
import java.util.regex.Pattern

class test1 extends AnyFlatSpec with Matchers{
  val conf: Config = ConfigFactory.load()
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  it should "Match the log pattern defined in the conf file" in {
    val example_msg = "10:19:50.082 [scala-execution-context-global-13] ERROR HelperUtils.Parameters$ - s%]s,+2k|D}K7b/XCwG&@7HDPR8z"

    val pattern = Pattern.compile(conf.getString("log.log_pattern"))
    val matcher = pattern.matcher(example_msg)
    matcher.find() shouldBe(true)
  }

  it should "Test md5 hash value" in {
    val str = "test string"
    log_process1.md5(str) shouldBe("6f8db599de986fab7a21625b7916589c")
  }

  it should "Get date object" in {
    HelperUtils.getDateComponentString("2021-11-03 10:19:50.082") shouldBe("2021-11-03")
  }

 it should "Test range of time successful" in {
   val start_line = "10:19:50.082 [scala-execution-context-global-13] ERROR HelperUtils.Parameters$ - s%]s,+2k|D}K7b/XCwG&@7HDPR8z"//37190082
   val end_line = "10:26:50.082 [scala-execution-context-global-13] ERROR HelperUtils.Parameters$ - s%]s,+2k|D}K7b/XCwG&@7HDPR8z"//37610082
   val t1 = 37190090
   val t2 = 37600082

   log_process1.checkRange(t1,t2,start_line,end_line) shouldBe(true)
 }
  it should "Test range of time failure" in {
    val start_line = "10:19:50.082 [scala-execution-context-global-13] ERROR HelperUtils.Parameters$ - s%]s,+2k|D}K7b/XCwG&@7HDPR8z"//37190082
    val end_line = "10:26:50.082 [scala-execution-context-global-13] ERROR HelperUtils.Parameters$ - s%]s,+2k|D}K7b/XCwG&@7HDPR8z"//37610082
    val t1 = 37190090
    val t2 = 37900082

    log_process1.checkRange(t1,t2,start_line,end_line) shouldBe(false)
  }
}
