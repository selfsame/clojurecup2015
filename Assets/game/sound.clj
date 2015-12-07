(ns game.sound
	(:use hard.core
        hard.life))

(def audio-clips
  (let [o (clone! :bang)
      clips (mapv #(.clip %) 
        (child-components o UnityEngine.AudioSource))
      res (into {} (mapv (juxt #(keyword (.name %)) identity) clips))]
      (destroy! o)
      res))


(defn audio! 
    ([k] (audio! k (->v3 0) false))
    ([k pos] (audio! k pos false))
    ([k pos kill]
      (let [o (clone! :sound pos)
            clip (get audio-clips k)
            c (.* o>AudioSource)]
      (set! (.clip c) clip)
      (.Play c)
      (if kill 
      (hard.life/route-update o 
        (fn [me] (if-not (.isPlaying (.* (->go me) >AudioSource))
            (destroy! (->go me)))))))))


(defn AUDIO! 
  ([k] (AUDIO! k {})) 
  ([k opts] 
    (when-let [clip (get audio-clips k)]
      (let [_s (first (remove #(.isPlaying %) (child-components (the audios) UnityEngine.AudioSource)))
            source _s]
        (when _s (set! (.clip _s) clip)
        (if (:volume opts) (set! (.volume source) (float (:volume opts))))
        (.Play _s))))))

 