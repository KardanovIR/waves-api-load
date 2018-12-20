import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ExtraInfo

package object node {
  val devnetNodeUrl = "http://1.devnet.wavesnodes.com:6869"
  val devnetNodeAddress = "3FkBWsgT9T3snZ4ZpzzQCJWQngJBLdDEPfU"
  val testnetNodeUrl = "http://testnet-aws-fr-3.wavesnodes.com:6869"

  val usersPerSec = 5
  val testDuration = 1
  val users = 1

  val nodeUrl = testnetNodeUrl
  val apiKey =  ""

  val baseConf = http
    .baseURL(nodeUrl)
    .acceptHeader("""application/json, text/plain, */*""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""en-US,en;q=0.5""")
    .header("X-API-Key", apiKey)
    .extraInfoExtractor { extraInfo =>
      List(getExtraInfo(extraInfo))
    }

  val httpConf = baseConf.check(status is 200)

  val testnetNodeAddress = "3N5Ai7adq5SBXvBYMY5HR5ZEWxTz3KJQbyD"

  val testnetAddresses = Seq(
    "3MwL5afsAvUTAfaj2bpfgaNGAs2tCpaZEGw",
    "3Mx6ozBRPafp1zBnD6m8QThCnEcJ84h77VN",
    "3N6nbrsW2Xru8wKWWLfFGBGMKwUNG1scEQn",
    "3MxP972JBajqkQFSDkDYqdUPTgfQpJ4UuTe",
    "3N9oerPHhj4SHZky18cB6a47hX97kJXhdCk"
  )

  val addresses = testnetAddresses
  val nodeAddress  = testnetNodeAddress

  private def getExtraInfo(extraInfo: ExtraInfo): String = {
    s"URL: ${extraInfo.request.getUrl}, Request: ${extraInfo.request.getStringData}, Response: ${extraInfo.response.body.string}"
  }
}
