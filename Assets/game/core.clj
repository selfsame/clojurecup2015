(ns game.core
  (:import [UnityEngine RenderSettings])
	(:require arcadia.core)
	(:use 
    clojure.pprint
		hard.core
    hard.life
    hard.pdf
    hard.input
    hard.mesh
    game.std
    game.data
    game.menu
    game.world
    game.play
    game.player))







(defn draw-ui []
  (let [lvl (str "LVL " @LEVEL)
        health (apply str (repeat (:health @PLAYER) (:heart icons)))
        max-health (apply str (repeat (:max-health @PLAYER) (:heart icons)))]
    (mapv #(set! (.text (.* %1 >Text)) %2) 
      [(the ui-level)(the ui-health)(the ui-max-health)]
      [lvl health max-health])
    ))

(add-watch PLAYER :ui-watcher
  (fn [k a old new]
    (if (not= (:health old) (:health new))
      (draw-ui))))

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
  (reset! PLAYER {
    :max-health 3
    :health 3
    :visited #{}})
  (scene :new-level))

(pdf game.std/scene [k]
  {k (is* :new-level)}
  (swap! LEVEL inc)
  (set! RenderSettings/ambientIntensity (float (max 0 (* (- 5.0 (* @LEVEL 0.5)) 0.1))))
  (draw-ui)
  (reset! DUNG (game.world/gen-dungeon (+ 10 @LEVEL)))
  (reset! ROOM [0 0])
  (reset! KNOWN #{})
  (scene :room [7 0 4]))

(pdf game.std/scene [k loc]
  {k (is* :room)}

  (clear-cloned!)
  (clone! :game-camera)
  (clone! :underplane) 
  (draw-ui)
  (game.world/build-room @ROOM)
  (game.world/build-floor @ROOM)
  (game.world/fill-room @ROOM)
  (make-player loc)
  (build-map)
  )
(scene :intro)


(scene :new-game)
(scene :new-level)

(comment 
(scene :intro)
(game.menu/credits "SELFSAMEGAMES" (color 20 66 45) 
  {:fx :wave 
   :loc [0 0 -3]
   :duration 8
   :callback (fn [_] 
  (game.menu/credits "CLOJURE CUP 2015" (color 88 5 54) 
    {:fx :wave :loc [0 0 -8]
     :duration 4
     :callback (fn [_] (log "done"))}))}))

