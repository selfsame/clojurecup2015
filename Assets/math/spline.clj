(ns math.spline
  (:refer-clojure :exclude [update])
  (:use arcadia.linear)
  (:import [UnityEngine Debug Gizmos Vector3 Color Time]))

;; catmull rom splines

(defmacro pow [x n]
  `(Mathf/Pow ~x ~n))

;; http://www.habrador.com/tutorials/catmull-rom-splines/
;; Vector3 a = 0.5f * (2f * p1);
;; Vector3 b = 0.5f * (p2 - p0);
;; Vector3 c = 0.5f * (2f * p0 - 5f * p1 + 4f * p2 - p3);
;; Vector3 d = 0.5f * (-p0 + 3f * p1 - 3f * p2 + p3);
;; Vector3 pos = a + (b * t) + (c * t * t) + (d * t * t * t);
(defn spline
  ([t ps]
   (let [i (int t)
         t (- t i)]
     (spline t
             (nth ps i)
             (nth ps (+ i 1))
             (nth ps (+ i 2))
             (nth ps (+ i 3)))))
  ([t p0 p1 p2 p3]
   (let [a (v3* (v3* p1 2) 0.5)
         b (v3* (v3- p2 p0) 0.5)
         c (v3* (v3+ (v3* p0 2)
                     (v3* p1 -5)
                     (v3* p2 4)
                     (v3* p3 -1))
                0.5)
         d (v3* (v3+ (v3* p0 -1)
                     (v3* p1 3)
                     (v3* p2 -3)
                     p3)
                0.5)]
     (v3+ a
          (v3* b t)
          (v3* c (pow t 2))
          (v3* d (pow t 3))))))

(defn on-draw-gizmos [points normals]
  {:pre [(= (count points) (count normals))]}
  (let [ps (flatten [(first points) points (last points)])
        ups (flatten [(first normals) normals (last normals)])
        ;; distance (reduce #(Vector3/Distance %1 %2) ps)
        c (- (count ps) 3)
        base-res 50.0
        rail-res 100.0]
        
    ;; base spline
    (set! Gizmos/color Color/black)
    (doseq [p points]
      (Gizmos/DrawSphere p 0.1))
    
    (doseq [i (range (int (dec base-res)))]
      (let [p (spline (* c (/ i base-res)) ps)
            q (spline (* c (/ (inc i) base-res)) ps)]
        (Gizmos/DrawLine p q)))
    
    (doseq [i (range (int (- rail-res 2)))]
      (let [p (spline (* c (/ i rail-res)) ps)
            q (spline (* c (/ (inc i) rail-res)) ps)
            r (spline (* c (/ (+ 2 i) rail-res)) ps)
            up-p (spline (* c (/ i rail-res)) ups)
            up-q (spline (* c (/ (inc i) rail-res)) ups)
            p-q (v3- p q)
            q-p (v3- q p)
            q-r (v3- q r)
            r-q (v3- r q)
            side-p-a (.normalized (Vector3/Cross p-q up-p))
            side-p-b (.normalized (Vector3/Cross q-p up-p))
            side-q-a (.normalized (Vector3/Cross q-r up-q))
            side-q-b (.normalized (Vector3/Cross r-q up-q))
            ]
        ;; sleepers
        (set! Gizmos/color Color/green)
        (Gizmos/DrawRay p side-p-a)
        (set! Gizmos/color Color/red)
        (Gizmos/DrawRay p side-p-b)
        
        ;; rails
        (set! Gizmos/color Color/blue)
        (Gizmos/DrawLine (v3+ p side-p-a)
                         (v3+ q side-q-a))
        (set! Gizmos/color Color/yellow)
        (Gizmos/DrawLine (v3+ p side-p-b)
                         (v3+ q side-q-b))))))

(defn giz [go]
  (on-draw-gizmos (map #(.position %) (.transform go))
                  (map #(.up %) (.transform go))
                  ))

#_ (defn update [go]
     (let [ps (concat points (take 4 points))
           i (Mathf/Repeat (* 0.1 Time/time) (- (count ps) 4))
           j (Mathf/Repeat (* 0.1 (+ 0.01 Time/time)) (- (count ps) 4))
           p (spline i ps)
           q (spline j ps)]
       (set! (.. go transform position)
             p)
       (set! (.. go transform forward)
             (v3- q p))))