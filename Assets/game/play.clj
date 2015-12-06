(ns game.play
	(:use 
    hard.core
    hard.pdf
    hard.life
    game.std
    [tween.core :as tween]))


(defn wall? [o] (= (.name (->go o)) "wall"))
(defn entity? [o] (= (.name (->go o)) "entity"))

(defn >colorize [o col]
  (mapv
    #((tween/material-color col 0.1
       (fn [o] ((tween/material-color (color 1 1 1) 0.1) o))) 
      (->go %))
    (child-components o UnityEngine.MeshRenderer)))

#_ (CLEAN hard.life/collision-enter)
#_ (CLEAN collide)
(defpdf collide)

(pdf collide [a b])
(pdf collide [a ^wall? b]
  (>colorize a (color 0 1 1)))

(pdf collide [a ^entity? b]
  (>colorize a (color 1 0 0)))

(pdf hard.life/collision-enter [a b tag id]
  {tag (is* "entity")}
  (collide a (.gameObject b)))
