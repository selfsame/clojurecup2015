(ns game.world
	(:use 
    hard.core
    hard.pdf
    game.std
    [game.data :as data]
    [tween.core :as tween]))

(def offsets [[1 0][-1 0][0 1][0 -1]])

(defn ->xyz [v] [(first v) 0 (last v)])

(defn neighbors [k m]
  (remove nil? (map #(if (get m (v+ k %)) (v+ k %)) offsets)))

(defn make-doors [M]
  (mapv 
    (fn [k] 
      (let [nbs (take (inc (rand-int 3)) (neighbors k @M))]
        (swap! M update-in [k] #(concat % nbs))
        (mapv (fn [n] 
          (swap! M update-in [n] #(cons k %))) nbs)))
    (keys @M))
  (swap! M #(into {} (map (fn [[k v]] [k (set v)]) %)))
  @M)

(defn gen-dungeon [n]
  (let [nodes
    (loop [rooms #{[0 0]}]
      (if (< n (count rooms)) rooms
        (recur (conj rooms 
          (mapv + (rand-nth (vec rooms)) 
                  (rand-nth offsets))))))
        rooms (atom (into {} (map vector nodes (repeat '()))))]
    (make-doors rooms)))


(defn build-map [M]
  (let [holder (clone! :empty)]
    (mapv 
      (fn [[k v]] 
        (parent! (clone! :map-room (->xyz k)) holder)
        (mapv       
          #(parent! 
            (clone! :map-door 
              (v+ (->xyz k) 
                  (v* (v- (->xyz %)
                          (->xyz k)) 
                      0.4)))
              holder)
            v))
      @M)
    holder))


(comment 
(defn gen [o] 
  (build-level (inc (rand-int 40)))
  ((tween/position (->v3 0 0 0) 1.0 gen) o))
(gen (the demo)))

(defn build-room [loc]
  (let [room-parts (clone! :empty)]
    (vec (for 
      [x (range data/room-w) z (range data/room-h)
        :when (and (or (#{(dec data/room-h) 0} z)
                       (#{(dec data/room-w) 0} x)))
        :let [wall (clone! :wall [x 0 z])]]
      (parent! wall room-parts)))
    room-parts))