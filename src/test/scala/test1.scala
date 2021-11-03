import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}

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
}
