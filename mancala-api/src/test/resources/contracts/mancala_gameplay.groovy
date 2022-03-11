package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "player plays his/her turn"

    request {
        url "/mancala/v1/gameplay"
        method POST ()
        headers {
            contentType(applicationJson())
        }
        body(
                ["gameId"                     : "3d7cdee7-b2f7-4b63-9d5f-55f262f6737f",
                 "gamePlayStatus"             : "IN_PROGRESS",
                 "mancalaBoard"               : [
                         [
                                 "mancalaGameIndex": 0,
                                 "stones"          : 0
                         ],
                         [
                                 "mancalaGameIndex": 1,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 2,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 3,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 4,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 5,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 6,
                                 "stones"          : 1
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
                 "selectedStoneContainerIndex": 0
                ]
        )
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                ["gameId"                     : "3d7cdee7-b2f7-4b63-9d5f-55f262f6737f",
                 "gamePlayStatus"             : "IN_PROGRESS",
                 "mancalaBoard"               : [
                         [
                                 "mancalaGameIndex": 0,
                                 "stones"          : 0
                         ],
                         [
                                 "mancalaGameIndex": 1,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 2,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 3,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 4,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 5,
                                 "stones"          : 7
                         ],
                         [
                                 "mancalaGameIndex": 6,
                                 "stones"          : 1
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
                 "selectedStoneContainerIndex": 0
                ]
        )
    }
}
