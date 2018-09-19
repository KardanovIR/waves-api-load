import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ExtraInfo

package object node {
  val nodeUrl  = "http://1.devnet.wavesnodes.com:6869"
  val nodeAddress = "3FkBWsgT9T3snZ4ZpzzQCJWQngJBLdDEPfU"

  val usersPerSec = 5
  val testDuration = 1

  val httpConf = http
    .baseURL(nodeUrl)
    .acceptHeader("""application/json, text/plain, */*""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""en-US,en;q=0.5""")
    .header("X-API-Key", "#ride_the_dev")
    .check(status is 200)
    .extraInfoExtractor { extraInfo =>
      List(getExtraInfo(extraInfo))
    }

  private def getExtraInfo(extraInfo: ExtraInfo): String = {
    s"URL: ${extraInfo.request.getUrl}, Request: ${extraInfo.request.getStringData}, Response: ${extraInfo.response.body.string}"
  }
}
