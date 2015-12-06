(ns game.data)

(def room-w 15)
(def room-h 9)

(def LEVEL (atom 0))
(def DUNG (atom nil))
(def ROOM (atom [0 0]))
(def KNOWN (atom #{}))
