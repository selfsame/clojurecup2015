(ns game.menu
	(:require arcadia.core)
	(:use
    clojure.pprint
		hard.core
    hard.pdf
    hard.life
    game.std
    [tween.core :as tween]))

(def >char-up (tween/loc-position (->v3 0 0 0.5) :+ 0.5 :pow3))
(def >char-down (tween/loc-position (->v3 0 0 -0.5) :+ 0.5 :pow3))
(tween/link! >char-up >char-down >char-up)

(defn >position* [& args] ((apply tween/position (butlast args)) (last args)))
(defn >color* [& args] ((apply tween/material-color (butlast args)) (last args)))

(defpdf credits)

(pdf credits [s bgcol opts]
  {}
  (>color* bgcol (the Plane))
    (log opts)
    (let [loc (or (:loc opts) [0 0 0])
          delay (or (:delay opts) 2.0) 
      holder (clone! :empty [0 0 0])]
      (vec (map-indexed
        (fn [idx chr]
          (let [text (clone! :menu-text (v+ loc [(* idx 0.5) 0 0]))]
            (set! (.text (.* text>TextMesh)) (str chr))
            ((assoc >char-up :delay (* idx 0.1)) text)
            (parent! text holder)))
        s))
      (>position* (->v3 0 0 -3) :+ delay (the iso-camera))
      (>position* (->v3 0) :+ delay
          (or (:callback opts) (fn [_]))
          holder)))





