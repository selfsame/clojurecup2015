(ns game.player
	(:use 
    hard.core
    hard.pdf
    hard.life
    hard.input
    game.std
    game.data
    game.play
    game.world
    game.sound
    [tween.core :as tween]))

(defn player? [o] (:player (data o)))

(pdf game.play/remove-health [o n] 
  (let [health (swap! PLAYER update-in [:health] dec)]
    (audio! :hurt2 (->v3 o))
    (when (neg? (:health @PLAYER))
      (defer! 
        (scene :new-game)))))

(def _CACHE (atom nil))

(pdf hard.life/destroy [^data? o tag id])

(pdf hard.life/start [^player? o tag id]
  (data! o (or @_CACHE (data o))))

(pdf hard.life/destroy [^player? o tag id]
  (reset! _CACHE (data o)))

(def >squish (tween/scale (->v3 0.5 -0.5 0.5) :+ 0.1 :pow3))
(link! >squish (tween/scale (->v3 -0.5 0.5 -0.5) :+ 0.3 :pow2))

(def >footer (tween/scale (->v3 1) 0.3 {:delay 0.3}))
(link! >footer >footer)
(link! >footer (fn [o] (audio! (rand-nth [:foot1 :foot2 :foot3 :foot4]) (->v3 o))))

(defn pitfall [o]
  ;TODO fall to next level at random point, stacked falls inc damage taken
  (data! o (assoc (data o) :fallstate (inc (:fallstate (data o)))))
  (audio! :fall (->v3 o))
  (defer! (scene :new-level)
    (scene :room (v+ (->xyz (rand-room-xy)) [0 5 0]))))

(def falling? (fn [d] (pos? (or (:fallstate d) 0))))

(pdf game.play/collide [a b ad bd]
  {ad (and* :player falling?)}
  (damage a (:fallstate ad))
  ((assoc >squish :target (->v3 (v* (:target >squish) (:fallstate ad)))) a)
  (data! a (assoc (data a) :fallstate 0)))

(defn player-update [o]
  (let [[x z] (joy)]
    (data! o (update-in (data o) [:damage-cooldown] #(if (pos? %) (- % UnityEngine.Time/deltaTime) -1)))
    (position! o (v+ (->v3 o) (v* [x 0 z] 0.1)))
    (if (< (Y (->v3 o)) -5)
       (pitfall o))
    (set! (.text (.* (the player-debug) >Text)) (with-out-str (clojure.pprint/pprint (data o))))
    (do-deferred)))

(defn make-player [pos]
  (let [o (clone! :entity)]
    (parent! (clone! :player) o)
    (>footer o)
    (data! o (or @_CACHE {:player true :entity true :damage-cooldown -1 :fallstate 0}))
    (position! o pos)
    (route-update o player-update) o))



