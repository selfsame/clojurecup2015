(ns game.world
  (:import [UnityEngine RenderSettings])
	(:use 
    seed.core
    hard.core
    hard.pdf
    game.std
    [game.data ]
    [tween.core :as tween]))

(def offsets [[1 0][-1 0][0 1][0 -1]])

(defn rand-room-xy []
  [(inc (srand-int (dec (dec room-w))))
   (inc (srand-int (dec (dec room-h))))])

(defn rand-light-xy []
  [(max 2 (min  (srand-int room-w) (- room-w 3)))
   (max 2 (min  (srand-int room-h) (- room-h 3)))])

(defn rand-color []
  (color (->vec (.normalized (->v3 (srand)(srand)(srand))))))

(def rand-light-intensity #(+ 2 (* (srand) 6)))


(defpdf procedural)

(pdf procedural [k room]
  {k (is* :lights)}
  (seed! [SEED @LEVEL room])
  (repeatedly (max (if (< @LEVEL 4) 1 0) (srand-int 3))
    (fn [] {
      :xyz (vec (interpose 4 (rand-light-xy))) 
      :color (rand-color) 
      :intensity (rand-light-intensity)})))

(defn light-average [room]
  (let [lights (procedural :lights room) 
        merge-fn (fn ([]) ([col m] 
          {:intensity (+ (:intensity col) (:intensity m))
            :color (v* (:color m) (:intensity m))}))]
      (if (empty? lights) []
        [(assoc 
          (update-in (reduce merge-fn lights) [:color] 
            #(color (->vec (.normalized (->v3 %)))))
        :xyz [7 1 4])])))



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
  (swap! M #(into {} 
    (map (fn [[k v]] 
      [k {
        :exits (set v)
        }]) %)))
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

(defn ->exits [room]
  (mapv offset-keys (map #(v- % room)  (get-in @DUNG [room :exits]))))
((comp dir-chars set)  (->exits @ROOM))

(defn build-map []
  (let [res (for [     
      _x (range 9)
      _z (range 9)
      :let [[x z] (v* (v- [_z _x] 4) [1 -1])
             xz (v+ [x z] @ROOM)
             exitchar (dir-chars (set (->exits xz)))]]
     [(str (if (get @DUNG xz) 
              (if (= xz @ROOM) "♦" " ") " ") (if (= 4 x) "\n"))
      (str exitchar (if (= 4 x) "\n"))] )
    rooms (apply str (mapv first res))
    doors (apply str (mapv last res))]
  (set! (.text (.* (the minimap) >Text)) rooms)
  (set! (.text (.* (the doormap) >Text)) doors)))

(defn place-light [light]
  (when (map? light)
    (let [{:keys [color xyz intensity]} light
          o (clone! :lamp xyz)
          light (.* o>Light)]
      (set! (.color light) color)
      (set! (.intensity light) (float intensity))
      o)))

(defn fill-room [room]
  (let [holder (name! (clone! :empty) :lights)
        data (get @DUNG @ROOM)]
    (mapv 
      (comp #(parent! % holder) 
            place-light)
      (procedural :lights room))
    holder))


(defn build-room [loc]
  (let [room-parts (name! (clone! :empty) :room)
        exits (set (mapv (comp offset-keys #(v- % loc)) (:exits (get @DUNG loc))))
        exit-coords (set (map ->xz (vals exit-locs)))]

    (vec (for 
      [x (range room-w) z (range room-h)
        :when (and (not (exit-coords [x z]))
                   (or (#{(dec room-h) 0} z)
                       (#{(dec room-w) 0} x)))
        :let [wall (clone! :stretch-wall (->xyz [x z]))]]
      (parent! wall room-parts)))
    (mapv
      (comp 
        #(parent! % room-parts)
        (fn [[k v]]
          (let [target v]
            (if (exits k) 
              (let [door (data! (clone! :door target) 
                            {:door true :direction k})
                    neighbor-light (mapv (comp 
                      #(position! %
                        (v+ (->v3 %)
                            (v* (->xyz (ROOM->xy (key-offset k))) 0.75)))
                      place-light) 
                    (filter #(> (:intensity %) 4.0) (light-average (v+ @ROOM (key-offset k)))))]
                (rotate! door (exit-directions k))
                

                door)
              (clone! :stretch-wall target)))))
    exit-locs)
    room-parts))

(def floorcode->k
  {'. :floor
   'u :floor
   'l :column
   'o :block-floor
   '_ :empty
   'M :trap})

(defn build-floor [loc]
  (let [holder (name! (clone! :empty) :floor) 
        pat (floor-pattern loc)]
    (vec (for [z (range (count pat))
          x (range (count (first pat)))
          :let [pos (v+ (->xyz [x z]) [1 0 1])
                k (floorcode->k (get (get pat z) x))]]
      (parent! (clone! k pos) holder)))))