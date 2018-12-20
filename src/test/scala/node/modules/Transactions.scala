package node.modules

import io.gatling.core.Predef._
import node._
import node.scenarios.Scenarios.transactionsH

import scala.concurrent.duration._

class Transactions extends Simulation {

  setUp(
    transactionsH
      .inject(
        constantUsersPerSec(usersPerSec) during (testDuration minute)
      )
      .protocols(httpConf))
}
