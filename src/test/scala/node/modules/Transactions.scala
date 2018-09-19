package node.modules

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import node._

import scala.concurrent.duration._
import scala.util.Random

class Transactions extends Simulation {

  val scn =
    scenario("Transactions Handlers")
      .exec(http("HOME")
        .get("/"))
      .exec(
        http("POST /transactions/sign")
          .post("/transactions/sign")
          .body(StringBody(
            s"""{"type": 4,
             |"sender": "${nodeAddress}",
             |"recipient": "${nodeAddress}",
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
      .exec(http("GET /transactions/address/{address}/limit/{limit}")
        .get(s"/transactions/address/$nodeAddress/limit/100")
        .check(regex(""""id" : "(.*?)",""").findAll.saveAs("ids")))
      .exec(session => {
        val ids = session("ids").as[List[String]]
        val txId = ids(Random.nextInt(ids.length)).toString
        session.set("id", txId)
      })
      .exec(http("GET /transactions/info")
        .get("/transactions/info/${id}")
        .check(jsonPath("$.signature").exists))

  setUp(
    scn
      .inject(
        constantUsersPerSec(usersPerSec) during (testDuration minute)
      )
      .protocols(httpConf))
}
