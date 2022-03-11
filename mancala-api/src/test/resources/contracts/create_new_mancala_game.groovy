package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return new game"

    request {
        url "/mancala/v1/start"
        method POST ()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                ["gameId"                     : "3d7cdee7-b2f7-4b63-9d5f-55f262f6737f",
                 "gamePlayStatus"             : "NEW",
                 "mancalaBoard"               : [
                         [
                                 "mancalaGameIndex": 0,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 1,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 2,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 3,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 4,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 5,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 6,
                                 "stones"          : 0
                         ],
                         [
                                 "mancalaGameIndex": 7,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 8,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 9,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 10,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 11,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 12,
                                 "stones"          : 6
                         ],
                         [
                                 "mancalaGameIndex": 13,
                                 "stones"          : 6
                         ]
                 ],
                 "activePlayer"               : "PLAYER_ONE",
                 "winner"                     : null,
                 "selectedStoneContainerIndex": null
                ]
        )
    }
}