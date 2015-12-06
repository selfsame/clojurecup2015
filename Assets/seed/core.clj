(ns seed.core
  (:use arcadia.core))

(defn seed! [v] (set! UnityEngine.Random/seed (hash v)))

(defn srand 
  ([] UnityEngine.Random/value)
  ([n] (* n (srand))))

(defn srand-int [n] 
  (int (* (srand) n)))

(defn srand-nth [col] 
  (get col (srand-int (count col))))
