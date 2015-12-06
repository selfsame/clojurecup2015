(ns game.data
  (:use seed.core))

(def SEED (rand))
(def LEVEL (atom 0))
(def DUNG (atom nil))
(def ROOM (atom [0 0]))
(def KNOWN (atom #{}))

(def PLAYER (atom {
  :max-health 3
  :health 3
  :visited #{}
  }))

(defn ->xyz [v] [(first v) 0 (last v)])
(defn ->xz [v] [(first v) (last v)])

(def room-w 15)
(def room-h 9)
(def half-w (int (/ (dec room-w) 2)))
(def half-h (int (/ (dec room-h) 2)))

(def exit-locs
  {:e [(dec room-w) 0 half-h] 
   :n [half-w 0 (dec room-h)] 
   :s [half-w 0 0] 
   :w [0 0 half-h]})

(def offset-keys
  {[1 0]  :e 
   [0 -1] :s 
   [0 1]  :n  
   [-1 0] :w })

(def reverse-dir {
   :e :w
   :n :s
   :s :n
   :w :e})

(def key-offset
  (into {} (map (comp vec reverse) offset-keys)))

(def exit-directions
  {:e [0 -90 0] 
   :n [0 180 0] 
   :s [0 0 0] 
   :w [0 90 0]})

(defn ROOM->xy [[X Y]] [(* X room-w)(* Y room-h)])

(defn ->XZ [[x z]]
  [(+ x (* (first @ROOM) room-w))(+ z (* (last @ROOM) room-h))])


(def dir-chars 
{#{:n :s :e :w} "┼"
 #{:n :s :e} "├"
 #{:n :s :w} "┤"
 #{:n :s} "│"
 #{:n :e :w} "┴"
 #{:n :e} "└"
 #{:n :w} "┘"
 #{:n} "☼"
 #{:s :e :w} "┬"
 #{:s :e} "┌"
 #{:s :w} "┐"
 #{:s} "☼"
 #{:e :w} "─"
 #{:e} "☼"
 #{:w} "☼"
 #{} " " }
 )



(def icons
  {:heart '♥
  :star '★
  :star-outline '☆
  :poison '☠
  :box '☐
  :checked '☑
  :crossed '☒
  :newling " "}
  )


(def . '.)
(def l 'l)
(def o 'o)
(def _ '_)
(def u 'u) ;assertive floor
(def M 'M) ;spikes
[
[. . . . . . . . . . . . .] 
[. . . . . . . . . . . . .] 
[. . . . . . . . . . . . .] 
[. . . . . . . . . . . . .] 
[. . . . . . . . . . . . .] 
[. . . . . . . . . . . . .] 
[. . . . . . . . . . . . .]]




(def x6x3 [
[
[. . . . . .]
[. . . . . .]
[. . . . . .]]
[
[. . . u u u]
[. . . u u u]
[. . . . . .]]

[
[. u . u . .]
[. u . u . .]
[. u . u . .]]

[
[. u . u . .]
[u u u u u u]
[. u . u . .]]

[
[. . . . . .]
[. . . l . .]
[. . . . . .]]
[
[. . . . o .]
[. . . . o o]
[. . . . . .]]
[
[. . . . . o]
[. . . . . .]
[. . . . . .]]
[
[. . . . . .]
[. . . . o .]
[. . . . . .]]
[
[. . . o . .]
[. o . o . .]
[. o . . . .]]
[
[. . . . . l]
[. . . . o .]
[. . . . . .]]
[
[. . . . . .]
[_ . . . . .]
[_ _ _ . . .]]
[
[. . . . _ _]
[. . . . . _]
[. . . . . .]]
[
[. . . . . .]
[. . _ _ . .]
[. . . . . .]]
[
[. _ _ _ _ .]
[. . . . . .]
[. . . . . .]]
[
[. . . _ . .]
[. . . _ . .]
[. . . . . .]]

[
[. . . . . .]
[. l . . . .]
[. . . . . .]]
[
[. . _ _ . .]
[. . _ . . _]
[_ . . . _ _]]
[
[. . _ _ . .]
[. . _ . . _]
[_ . . . _ _]]
])

(def x6x3_traps
[
[
[. . . . . .]
[. . . . M .]
[. . . . . .]]
[
[. . . M . .]
[. . . . . .]
[. M . . . .]]
[
[. . . M . .]
[. . . . . M]
[. . . . . .]]
[
[. . . M . M]
[. M . . . .]
[. . . . M .]]
[
[. . . . . .]
[. . M . . .]
[. . M . . .]]
[
[. . . . . .]
[. . . . . .]
[M . M . . .]]
[
[. . . . . .]
[. . M M . .]
[. . . . . .]]
[
[. . M . . .]
[. . . M . .]
[. . . . M .]]
[
[. . . . M .]
[M M . . M .]
[. . . . . .]]])


(defn flip [pat d]
  (cond 
    (#{:v} d)
    (vec (reverse pat))
    (#{:h} d)
    (mapv (comp vec reverse) pat)
    :else pat))

(defn join [d & cols]
  (let []
  (cond 
    (#{:v} d)(vec (apply concat cols))
    (#{:h} d)(apply mapv (comp vec concat) cols))))

(defn merge-pat [a b]
  (mapv 
    (fn [j k] (mapv 
      #(or (if-not (= %2 .) %2) %1)
      j k))
    a b))

(defn show [pat]
  (println (apply str (interpose "\n" pat))))




(defn rand-flip [pat]
  ((apply comp (take (srand-int 3) (sshuffle [#(flip % :v) #(flip % :h)]))) pat))

(comment 
  (->  
    (merge-pat (rand-design )(rand-design ))
    (show)))

(defn rand-quad [ pool iter] (reduce merge-pat (take (inc (srand-int iter)) (repeatedly #(rand-flip (srand-nth pool))))))

(defn rand-design [pool iter]
  (let [p1 (rand-quad pool iter)
        alt1 (rand-quad pool iter)
        [ma mb] (sshuffle [:h :v])

        buffer 
        (fn [d col] 
          (if (= :v d) [(vec (repeat (count (first col)) .))]
            (vec (repeat (count col) [.]))))

        [p2 p3] (sshuffle [p1 (flip p1 ma) alt1])

        h1 (flip (join ma p2 (buffer ma p2) p3 ) (srand-nth [mb ma]))
        [h2 h3] (sshuffle [h1 (flip h1 mb)])
        h2 ( (srand-nth [identity #(flip % ma)]) h2)]
    (join mb h2 (buffer mb h2) h3)))

(defn floor-pattern [room] 
  (seed! [room @LEVEL])
  (let [structure (reduce merge-pat (repeatedly (inc (srand-int 4)) #(rand-design x6x3 3) ))
        traps (reduce merge-pat (repeatedly (inc (srand-int 3)) #(rand-design x6x3_traps 2) ))]
    (merge-pat traps structure)))


(show (reduce merge-pat (repeatedly (inc (srand-int 2)) #(rand-design x6x3_traps 2) )))

(map  #(int  (+ 0.7 (* % 0.1))) (range 30))
(> (* (rand)(rand)(rand)) (/ 1 3))