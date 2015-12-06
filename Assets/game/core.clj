(ns game.core
	(:require arcadia.core)
	(:use 
    clojure.pprint
		hard.core
    hard.life
    hard.pdf
    hard.input
    game.std
    game.data
    game.menu
    game.play
    game.world))




(defn player-input [o]
  (let [[x z](joy)]
    (position! o (v+ (->v3 o) (v* [x 0 z] 0.1)))))

(defn player [pos]
  (let [o (clone! :entity)]
    (parent! (clone! :player) o)
    (position! o pos)
    (route-update o player-input) o))


(defn draw-ui []
  (let [minimap (game.world/build-map DUNG)
        target (the top-left-ui)]
    (position! minimap (->v3 target))
    (local-scale! minimap (->v3 0.2))
    (parent! minimap target)
    (rotation! minimap (rotation target))))



(pdf game.std/scene [k] 
  {k (is* :intro)}
  (clear-cloned!)
  (clone! :iso-camera)
  (clone! :sun)
  (clone! :Plane)
  (parent! (the Plane) (the iso-camera)))

(pdf game.std/scene [k]
  {k (is* :new-game)}
  (reset! LEVEL 0)
  (scene :new-level))

(pdf game.std/scene [k]
  {k (is* :new-level)}
  (swap! LEVEL inc)
  (reset! DUNG (game.world/gen-dungeon (+ 10 @LEVEL)))
  (reset! ROOM [0 0])
  (reset! KNOWN #{})
  (scene :room @ROOM))

(pdf game.std/scene [k loc]
  {k (is* :room)}
  (clear-cloned!)
  (clone! :game-camera)
  (clone! :sun)
  (clone! :Plane)
  (draw-ui)
  (let [exits [-1 1]]
    (game.world/build-room loc)
    (player [7 0 4])
  ))



(pdf game.std/scene [k]
  {k (is* :inventory)}
  :inventory-scene)

(scene :new-game)


(scene :intro)
(game.menu/credits "SELFSAMEGAMES" (color 20 66 45) 
  {:fx :wave 
   :loc [0 0 -3]
   :duration 8
   :callback (fn [_] 
  (game.menu/credits "CLOJURE CUP 2015" (color 88 5 54) 
    {:fx :wave :loc [0 0 -8]
     :duration 4
     :callback (fn [_] (log "done"))}))})

