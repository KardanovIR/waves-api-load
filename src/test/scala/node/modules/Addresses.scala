package node.modules

import io.gatling.core.Predef._
import node._
import node.scenarios.Scenarios.addressesH

import scala.concurrent.duration._

class Addresses extends Simulation {
  setUp(
    addressesH
      .inject(
        constantUsersPerSec(usersPerSec) during (testDuration seconds)
      )
      .protocols(httpConf))
}
