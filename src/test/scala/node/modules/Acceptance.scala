package node.modules

import io.gatling.core.Predef._
import node.scenarios.Scenarios._
import node.{baseConf, httpConf, users}

class Acceptance extends Simulation {

  //POST /blocks/checkpoint -- will be deleted soon
  //POST /node/stop -- will not ve used

  setUp(
    addressesH.inject(atOnceUsers(users)).protocols(httpConf),
    blocksH.inject(atOnceUsers(users)).protocols(httpConf),
    transactionsH.inject(atOnceUsers(users)).protocols(httpConf),
    walletH.inject(atOnceUsers(users)).protocols(httpConf),
    nodeH.inject(atOnceUsers(users)).protocols(httpConf),
    activationH.inject(atOnceUsers(users)).protocols(httpConf),
    leaseH.inject(atOnceUsers(users)).protocols(httpConf),
    consensusH.inject(atOnceUsers(users)).protocols(httpConf),
    assetsH.inject(atOnceUsers(users)).protocols(httpConf),
    notFoundErr.inject(atOnceUsers(users)).protocols(baseConf)
  )

}
