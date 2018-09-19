package node.modules

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import node._

import scala.concurrent.duration._
import scala.util.Random

class Blocks extends Simulation {
  val scn =
    scenario("node.modules.Blocks Handlers")
      .exec(http("HOME")
        .get("/"))
      .exec(http("GET /blocks/height")
        .get("/blocks/height").check(jsonPath("$..height").ofType[Int].saveAs("height")))
      .exec(session => {
        val maxHeight = session("height").as[Int]
        val height = math.max(Random.nextInt(maxHeight), 1)
        session.set("height", height)
      })
      .exec(http("GET /blocks/headers/last")
        .get("/blocks/headers/last").check(jsonPath("$.signature").exists))
      .exec(http("GET /blocks/at/{height}")
        .get("/blocks/at/${height}"))


  setUp(
    scn
      .inject(
        constantUsersPerSec(usersPerSec) during (testDuration minute)
      )
      .protocols(httpConf))
}
