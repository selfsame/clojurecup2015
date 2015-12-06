(ns game.play
	(:use 
    hard.core
    hard.pdf
    hard.life
    game.std
    game.data
    [tween.core :as tween]))


(defn wall? [o] (= (.name (->go o)) "wall"))
(defn entity? [o] (= (.name (->go o)) "entity"))
(defn data? [o] (data (->go o)))



(defn >colorize [o col]
  (mapv
    #((tween/material-color col 0.1
       (fn [o] ((tween/material-color (color 1 1 1) 0.1) o))) 
      (->go %))
    (child-components o UnityEngine.MeshRenderer)))

#_ (CLEAN hard.life/collision-enter)
#_ (CLEAN collide)
(defpdf collide)
(defpdf trigger)
(defpdf damage)
(defpdf remove-health)


(pdf collide [a b])
(pdf collide [a ^wall? b ad bd])

(pdf collide [a ^entity? b ad bd])



(pdf trigger [a b ad ^:door bd]
  (let [dir (:direction bd)
        offset (key-offset dir)
        room-offset (->xyz (v* (key-offset dir) [room-w room-h]))]
    (set! (.enabled (.* b>BoxCollider)) false)
    (swap! game.data/ROOM #(v+ % (key-offset dir)))
    ((tween/position (->v3 0) 0.01 
      (fn [_] 
        (scene :room   (v+ (->xyz (key-offset dir)) (exit-locs (reverse-dir dir))))))
      (the :room))

    ))

(pdf trigger [a b ad ^:trap bd]
  (damage a 1))


(pdf hard.life/collision-enter [a b tag id]
  {tag (is* "entity")}
  (collide a (.gameObject b) (data a) (data (.gameObject b))))

(pdf hard.life/trigger-enter [a b tag id]
  {tag (is* "entity")}
  (trigger a (.gameObject b) (data a) (data (.gameObject b))))


(def >up (tween/position (->v3 0 0.8 0) 0.3 :+ :pow3 {:delay 1}))
(def >down (tween/position (->v3 0 -0.8 0) 1.2 :+  {:delay 5}))
(link! >up >down >up)

(pdf hard.life/start [o tag id]
  {tag (is* "trap")}
  (data! o {:trap true})
  ((assoc >up :delay (+ 1 (rand-int 8))) (child-named o "spikes")))



(defn >damage [o]
  (if (pos? (:damage-cooldown (data o))) 
    (do (>colorize o (color 1 0 0))
        ((tween/scale (->v3 1) 0.2 >damage) o))
    (>colorize o (color 1 1 1)))
  )




(def cooled-down? #(neg? (:damage-cooldown (data %))))

(pdf damage [^data o ^number? n])
(pdf remove-health [^data o])
(pdf damage [^cooled-down? o n] 
  (data! o (assoc (data o) :damage-cooldown 1.2))
  (>damage o)
  (remove-health o n))

