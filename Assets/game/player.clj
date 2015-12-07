(ns game.player
	(:use 
    hard.core
    hard.pdf
    hard.life
    hard.input
    hard.physics
    game.std
    game.data
    game.play
    game.world
    game.sound
    [tween.core :as tween]))

(defn player? [o] (:player (data o)))

(pdf game.play/remove-health [^player? o n] 
  (let [health (swap! PLAYER update-in [:health] dec)]
    (AUDIO! :hurt2)
    (when (neg? (:health @PLAYER))
      (defer! 
        (scene :new-game)))))



(pdf hard.life/destroy [^data? o tag id])


(pdf hard.life/destroy [^player? o tag id]
  (reset! _CACHE (data o)))

(def >squish (tween/scale (->v3 0.5 -0.5 0.5) :+ 0.1 :pow3))
(link! >squish (tween/scale (->v3 -0.5 0.5 -0.5) :+ 0.3 :pow2))

(def >footer (tween/scale (->v3 1) 0.3 {:delay 0.3}))
(link! >footer >footer)
(link! >footer (fn [o] (audio! (rand-nth [:foot1 :foot2 :foot3 :foot4]) (->v3 o))))

(defn pitfall [o]
  (data! o (assoc (data o) :fallstate (inc (:fallstate (data o)))))
  (AUDIO! :fall (->v3 o))
  (defer! (scene :new-level)
    (scene :room (v+ (->xyz (rand-room-xy)) [0 5 0]))))

(def falling? (fn [d] (pos? (or (:fallstate d) 0))))

(pdf game.play/collide [a b ad bd]
  {ad (and* :player falling?)}
  (damage a (:fallstate ad))
  ((assoc >squish :target (->v3 (v* (:target >squish) (:fallstate ad)))) a)
  (data! a (assoc (data a) :fallstate 0)))

(defn >tc [o] 
  ((tween/light-color (color (rand-vec [0.6 0.8] [0.5 0.7][0.2 0.4]))  (* (rand) 0.5)) o)
  ((tween/light-range (float  (+ (* (:torch @PLAYER) 0.1) (rand)(rand) 2.0 ) ) (* (rand) 0.3) >tc) o))

(defn check-lantern []
  (if ((comp pos? number?) (:torch @PLAYER))
      (let [lantern (or (arcadia.core/object-named "lantern") 
            (parent! (clone! :lantern (v+ (->v3 (the player)) [0 1.3 0])) (the player)))]
          (>tc lantern)
          (route-update (the lantern)
            (fn [_] 
              (swap! PLAYER update-in [:torch] #(- (or % 0) UnityEngine.Time/deltaTime))
              (when (neg? (:torch @PLAYER))
                (destroy! (the lantern))))))))



(defn light-torch []
  (if ((comp pos? number?) (:torches @PLAYER))
    (do (swap! PLAYER update-in [:torches] dec)
        (swap! PLAYER update-in [:torch] (fn [_] 58))
        (AUDIO! :torch-light)
        (check-lantern)
        ;(set! (.intensity (.* (the lantern)>Light)) (float 6.0))
        )))



(def max-speed 4.0)

(defn move-player [o x z]
  (let [body (->rigidbody o)
        input (v* [x 0 z] 2)
        [vx vy vz] (->vec (->velocity o))
        mov  (->v3 (v+ (v* [vx 0 vz] 0.9) input [0 vy 0]))
        ratio (/ max-speed (max (.magnitude mov) 0.001))
        mov (if (< ratio 1.0) (V* mov ratio) mov)]
       
  (set! (.velocity body) (->v3 (X mov) vy (Z mov)))))



(defn player-update [o]
  (let [[x z] (joy)]
    (data! o (update-in (data o) [:damage-cooldown] #(if (pos? %) (- % UnityEngine.Time/deltaTime) -1)))
    (cond (key-down? "t") (light-torch)
      (key-down? "escape") (quit))
    (move-player o x z)
    (if (< (Y (->v3 o)) -5) (pitfall o))
    ;(set! (.text (.* (the player-debug) >Text)) (with-out-str (clojure.pprint/pprint [(data o) @PLAYER])))
    (do-deferred)))

(defn make-player [pos]
  (let [o (clone! :entity)]
    (parent! (clone! :player) o)
    ;(>footer o)
    (data! o (or @_CACHE {:player true :entity true :damage-cooldown -1 :fallstate 0 :grounded 1}))
    (position! o pos)
    (check-lantern)
    (route-update o player-update) o))



