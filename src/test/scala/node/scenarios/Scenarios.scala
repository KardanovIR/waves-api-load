package node.scenarios

import java.util.concurrent.ThreadLocalRandom
import io.gatling.core.Predef.scenario
import io.gatling.http.Predef.{http, jsonPath, regex}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import node.{Base58, addresses, nodeAddress}
import io.gatling.http.Predef._
import scala.util.Random

object Scenarios {
  private def random = ThreadLocalRandom.current

  private val address = addresses(Random.nextInt(addresses.length))

  val walletH: ScenarioBuilder = scenario("Wallet Handlers")
    .exec(
      http("GET /wallet/seed")
        .get("/wallet/seed")
        .check(jsonPath("$.seed")))

  val nodeH: ScenarioBuilder = scenario("Node Handlers")
    .exec(
      http("GET /node/status")
        .get("/node/status")
        .check(jsonPath("$.blockchainHeight")))
    .exec(
      http("GET /node/version")
        .get("/node/version")
        .check(jsonPath("$.version")))

  val activationH: ScenarioBuilder = scenario("Activation Handlers")
    .exec(
      http("GET /activation/status")
        .get("/activation/status")
        .check(jsonPath("$.features")))

  val assetsH: ScenarioBuilder = scenario("Assets Handlers")
    .exec(http("GET /assets/balance/{address}")
      .get(s"/assets/balance/$address")
      .check(jsonPath("$..assetId").findAll.saveAs("assets")))
    .pause(2)
    .exec(session => {
      val assets = session("assets").as[Seq[String]]
      val asset = assets(Random.nextInt(assets.length)).toString
      session.set("asset", asset)
    })
    .pause(2)
    .exec(session => {
      val AddressAndAsset = s"/assets/balance/$address/" + session("asset")
        .as[String]
      session.set("AddressAndAsset", AddressAndAsset)
    })
    .pause(2)
    .exec(http("GET /assets/balance/{address}/{assetId}")
      .get("${AddressAndAsset}")
      .check(jsonPath("$.balance")))
    .exec(http("GET /assets/{assetId}/distribution")
      .get("/assets/${asset}/distribution"))
    .exec(http("GET /node/status")
      .get("/node/status")
      .check(jsonPath("$.blockchainHeight").saveAs("blockchainHeight")))
    .pause(2)
    .exec(http("GET /assets/{assetId}/distribution/{height}/limit/{limit}")
      .get("/assets/${asset}/distribution/${blockchainHeight}/limit/999"))
    .exec(http("GET /assets/details/{assetId}")
      .get("/assets/details/${asset}")
      .check(jsonPath("$.name"))
      .check(jsonPath("$.quantity")))

  val consensusH: ScenarioBuilder = scenario("Consensus Handlers")
    .exec(
      http("GET /blocks/first")
        .get("/blocks/first")
        .check(jsonPath("$.signature").saveAs("signature")))
    .exec(http("GET /consensus/generationsignature")
      .get("/consensus/generationsignature")
      .check(jsonPath("$.generationSignature")))
    .exec(
      http("GET /consensus/algo")
        .get("/consensus/algo")
        .check(jsonPath("$.consensusAlgo"))
        .check(regex("FairPoS")))
    .exec(
      http("GET /consensus/basetarget")
        .get("/consensus/basetarget")
        .check(jsonPath("$.baseTarget"))
        .check(jsonPath("$.score")))
    .exec(
      http("GET /consensus/generatingbalance/{address}")
        .get(s"/consensus/generatingbalance/$address")
        .check(jsonPath("$.address"))
        .check(jsonPath("$.balance")))
    .exec(http("GET /consensus/generationsignature/{blockId}")
      .get("/consensus/generationsignature/${signature}")
      .check(jsonPath("$.generationSignature")))
    .exec(http("GET /consensus/basetarget/{blockId}")
      .get("/consensus/basetarget/${signature}")
      .check(jsonPath("$.baseTarget")))

  val leaseH: ScenarioBuilder = scenario("Lease Handlers")
    .exec(
      http("GET /leasing/active/{address}")
        .get(s"/leasing/active/$address"))

  val transactionsH: ScenarioBuilder = scenario("node.modules.Transactions Handlers")
    .exec(
      http("POST /transactions/sign")
        .post("/transactions/sign")
        .body(StringBody(
          s"""{"type": 4,
             |"sender": "$nodeAddress",
             |"recipient": "$nodeAddress",
             |"amount": 100000000,
             |"fee": 100000
             |} """.stripMargin
        ))
        .asJSON
        .check(jsonPath("$").saveAs("signedTransfer")))
    .pause(3)
    .exec(
      http("POST /transactions/broadcast")
        .post("/transactions/broadcast")
        .body(StringBody("${signedTransfer}"))
        .asJSON
        check (jsonPath("$..id").saveAs("idTx")))
    .exec(http("POST /transactions/calculateFee")
      .post("/transactions/calculateFee")
      .body(StringBody("${signedTransfer}"))
      .asJSON)
    .exec(http("POST /transactions/sign/{signerAddress}")
      .post(s"/transactions/sign/$nodeAddress")
      .body(StringBody("${signedTransfer}"))
      .asJSON)
    .exec(http("GET /transactions/address/{address}/limit/{limit}")
      .get(s"/transactions/address/$nodeAddress/limit/100")
      .check(regex(""""id" : "(.*?)",""").findAll.saveAs("ids")))
    .exec(session => {
      val ids = session("ids").as[List[String]]
      val txId = ids(Random.nextInt(ids.length)).toString
      session.set("id", txId)
    })
    .pause(10)
    .exec(http("GET /transactions/info")
      .get("/transactions/info/${idTx}")
      .check(jsonPath("$.signature").exists))
    .exec(http("GET /transactions/unconfirmed/size")
      .get("/transactions/unconfirmed/size"))
    .exec(http("GET /transactions/unconfirmed")
      .get("/transactions/unconfirmed"))

  val blocksH: ScenarioBuilder = scenario("node.modules.Blocks Handlers")
    .exec(http("GET /blocks/height")
      .get("/blocks/height")
      .check(jsonPath("$..height").ofType[Int].saveAs("height")))
    .exec(session => {
      val maxHeight = session("height").as[Int]
      val height = math.max(Random.nextInt(maxHeight), 1)
      session.set("height", height)
    })
    .exec(http("GET /blocks/headers/last")
      .get("/blocks/headers/last")
      .check(jsonPath("$.signature").exists))
    .exec(http("GET /blocks/last")
      .get("/blocks/last")
      .check(jsonPath("$.signature")))
    .exec(http("GET /blocks/first")
      .get("/blocks/first")
      .check(jsonPath("$.signature").saveAs("signature")))
    .exec(http("GET /blocks/at/{height}")
      .get("/blocks/at/${height}"))
    .exec(http("GET /blocks/height/{signature}")
      .get("/blocks/height/${signature}")
      .check(jsonPath("$.height")))
    .exec(http("GET /blocks/seq/{from}/{to}")
      .get("/blocks/seq/2/3")
      .check(jsonPath("$..signature")))
    .exec(http("GET /blocks/headers/seq/{from}/{to}")
      .get("/blocks/headers/seq/2/3")
      .check(jsonPath("$..signature")))
    .exec(http("GET /blocks/signature/{signature}")
      .get("/blocks/signature/${signature}")
      .check(jsonPath("$..signature")))
    .exec(http("GET /blocks/address/{address}/{from}/{to}")
      .get(s"/blocks/address/$address/1/2"))
    // .check(jsonPath("$..signature")))
    .exec(http("GET /blocks/child/{signature}")
      .get("/blocks/child/${signature}")
      .check(jsonPath("$.signature")))
  //      .exec(http("GET /blocks/delay/{signature}/{blockNum}") https://wavesplatform.atlassian.net/browse/NODE-1377
  //        .get("/blocks/delay/${signature}/3")
  //        .check(jsonPath("$.delay")))

  val addressesH: ScenarioBuilder =
    scenario("node.modules.Addresses Handlers")
      .exec(http("GET /addresses")
        .get("/addresses")
        .check(regex("\"(.*?)\"").findAll.saveAs("addresses")))
      .exec(http("GET /addresses/balance/{address}")
        .get(s"/addresses/balance/$address"))
      .exec(http("GET /addresses/balance/{address}/{confirmations}")
        .get(s"/addresses/balance/$address/100"))
      .exec(http("GET /addresses/balance/details/{address}")
        .get(s"/addresses/balance/details/$address"))
      .exec(http("GET /addresses/effectiveBalance/{address}")
        .get(s"/addresses/effectiveBalance/$address"))
      .exec(http("GET /addresses/effectiveBalance/{address}/{confirmations}")
        .get(s"/addresses/effectiveBalance/$address/100"))
      .exec(http("GET /addresses/data/{address}")
        .get(s"/addresses/data/$address"))
      .exec(http("GET /addresses/scriptInfo/{address}")
        .get(s"/addresses/scriptInfo/$address"))
      .exec(http("GET /addresses/validate/{address}")
        .get(s"/addresses/validate/$address")
        .check(jsonPath("$.valid").exists))
      .exec(session => {
        val pk = Base58.encode(
          Array.fill[Byte](32)(random.nextInt(Byte.MaxValue).toByte))
        session
          .set("publicKey", pk)
      })
      .pause(2)
      .exec(http("GET /addresses/publicKey/{publicKey}")
        .get("/addresses/publicKey/${publicKey}")
        .check(jsonPath("$.address").exists))
      .exec(http("POST /addresses")
        .post("/addresses")
        .check(jsonPath("$.address").saveAs("newaddress")))
      .pause(15)
      .exec(session => {
//        println("address " + session.attributes.get("newaddress").get)
        val newA = session.attributes.get("newaddress").get
        val dataReq = s"""{
                         |"version": 1,
                         |"sender": "${newA}",
                         |"data": [
                         |{
                         |"type": "integer",
                         |"key": "bla",
                         |"value": 0
                         |}
                         |],
                         |"fee": 100000
                         |}""".stripMargin
//        println(dataReq)
        session.set("dataReq", dataReq)
      })
      .exec(http("POST /addresses/data")
        .post("/addresses/data")
        .body(StringBody("${dataReq}"))
        .asJSON)
      .pause(3)
      .exec(http("GET /addresses/data/{address}/{key}")
        .get("/addresses/data/${newaddress}/bla")
        .check(jsonPath("$.key").exists))
      .exec(http("GET /addresses/seed/{address}")
        .get("/addresses/seed/${newaddress}"))
      .exec(http("GET /addresses/seq/{from}/{to}")
        .get("/addresses/seq/1/2"))
      .exec(http("POST /addresses/sign/{address}")
        .post("/addresses/sign/${newaddress}")
        .body(StringBody("bla"))
        .check(regex(".*").saveAs("sign")))
      .exec(http("POST /addresses/signText/{address}")
        .post("/addresses/signText/${newaddress}")
        .body(StringBody("bla"))
        .check(regex(".*").saveAs("signText")))
      .pause(2)
      //      .exec(http("POST /addresses/verify/{address}")
      //        .post("/addresses/verify/${newaddress}")
      //        .body(StringBody("$sign"))) https://wavesplatform.atlassian.net/browse/NODE-1370
      //      .exec(http("POST /addresses/verifyText/{address}")
      //        .post("/addresses/verifyText/${newaddress}")
      //        .body(StringBody("$sign"))) https://wavesplatform.atlassian.net/browse/NODE-1370
      .exec(http("DELETE /addresses/{address}")
        .delete("/addresses/${newaddress}")
        .check(jsonPath("$.deleted").exists))

  val notFoundErr =
    scenario("404")
      .exec(
        http("GET /transactions/address/{address}/limit/{limit}")
          .get(s"/transactions/address/${address}/limit/100")
          .check(regex(""""id" : "(.*?)",""").findAll.saveAs("idsall"))
          .check(status is 200))
      .exec(session => {
        val ids = session("idsall").as[List[String]]
        val txId = ids(Random.nextInt(ids.length)).toString
        session.set("id404", txId)
      })
      .pause(10)
      .exec(http("GET /transactions/unconfirmed/info/{id}")
        .get("/transactions/unconfirmed/info/${id404}")
        .check(status is 404))
      .exec(http("GET /alias/by-alias/{alias}")
        .get("/alias/by-alias/testtes")
        .check(status is 404)
        .check(jsonPath("$.message")))
      .exec(http("GET /alias/by-address/{address}")
        .get(s"/alias/by-address/$address"))

}
