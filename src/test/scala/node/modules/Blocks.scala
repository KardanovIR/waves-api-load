package node.modules

import io.gatling.core.Predef._
import node._
import node.scenarios.Scenarios.blocksH

import scala.concurrent.duration._

class Blocks extends Simulation {

  setUp(
    blocksH
      .inject(
        constantUsersPerSec(usersPerSec) during (testDuration minute)
      )
      .protocols(httpConf))
}
