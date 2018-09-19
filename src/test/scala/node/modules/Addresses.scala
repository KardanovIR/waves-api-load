package node.modules

import java.util.concurrent.ThreadLocalRandom

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import node._

import scala.concurrent.duration._
import scala.util.Random

class Addresses extends Simulation {
  private def random = ThreadLocalRandom.current

  val scn =
    scenario("node.modules.Addresses Handlers")
      .exec(http("HOME")
        .get("/"))
      .exec(http("GET /addresses")
        .get("/addresses")
        .check(regex("\"(.*?)\"").findAll.saveAs("addresses")))
      .exec(http("POST /addresses")
        .post("/addresses")
        .check(jsonPath("$.address").saveAs("address")))
      .pause(2)
      .exec(session => {
        val addresses = session("addresses").as[List[String]]
        val address = addresses(Random.nextInt(addresses.length)).toString
        val pk = Base58.encode(
          Array.fill[Byte](32)(random.nextInt(Byte.MaxValue).toByte))
        session.set("address", address)
        session.set("publicKey", pk)
      })
      .exec(http("GET /addresses/balance/{address}")
        .get("/addresses/balance/${address}"))
      .exec(http("GET /addresses/balance/details/{address}")
        .get("/addresses/balance/details/${address}"))
      .exec(http("GET /addresses/effectiveBalance/{address}")
        .get("/addresses/effectiveBalance/${address}"))
      .exec(http("GET /addresses/seed/{address}")
        .get("/addresses/seed/${address}"))
      .exec(http("GET /addresses/data/{address}")
        .get("/addresses/data/${address}"))
      .exec(http("GET /addresses/scriptInfo/{address}")
        .get("/addresses/scriptInfo/${address}"))
      .exec(http("GET /addresses/validate/{address}")
        .get("/addresses/validate/${address}")
        .check(jsonPath("$.valid").exists))
      .exec(http("GET /addresses/publicKey/{publicKey}")
        .get("/addresses/publicKey/${publicKey}")
        .check(jsonPath("$.address").exists))
      .exec(http("DELETE /addresses/{address}")
        .delete("/addresses/${address}")
        .check(jsonPath("$.deleted").exists))

  setUp(
    scn
      .inject(
        constantUsersPerSec(usersPerSec) during (testDuration minute)
      )
      .protocols(httpConf))
}
