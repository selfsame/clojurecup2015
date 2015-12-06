(ns hard.life
  (:use hard.core hard.pdf)
  (:require arcadia.core))
    
  
  
(defpdf awake)
(defpdf start)
(defpdf destroy)
(defpdf mouse-down)
(defpdf mouse-up)
(defpdf mouse-enter)
(defpdf mouse-over)
(defpdf mouse-exit)
(defpdf mouse-drag)
(defpdf collision-enter)
(defpdf collision-exit)
(defpdf collision-stay)
(defpdf collision-enter-2d)
(defpdf collision-exit-2d)
(defpdf collision-stay-2d)
(defpdf trigger-enter)
(defpdf trigger-exit)
(defpdf trigger-stay)
(defpdf trigger-enter-2d)
(defpdf trigger-exit-2d)
(defpdf trigger-stay-2d)

(pdf awake [a b c])
(pdf start [a b c])
(pdf destroy [a b c])
(pdf mouse-down [a b c])
(pdf mouse-up [a b c])
(pdf mouse-enter [a b c])
(pdf mouse-over [a b c])
(pdf mouse-exit [a b c])
(pdf mouse-drag [a b c])
(pdf collision-enter [x a b c])
(pdf collision-exit [x a b c])
(pdf collision-stay [x a b c])
(pdf collision-enter-2d [x a b c])
(pdf collision-exit-2d [x a b c])
(pdf collision-stay-2d [x a b c])
(pdf trigger-enter [x a b c])
(pdf trigger-exit [x a b c])
(pdf trigger-stay [x a b c])
(pdf trigger-enter-2d [x a b c])
(pdf trigger-exit-2d [x a b c])
(pdf trigger-stay-2d [x a b c])

(arcadia.core/defcomponent Handler [^System.String tag ^System.String id]
  (Awake [this] (awake (->go this) tag id))
  (Start [this] (start (->go this) tag id))
  (OnDestroy [this] (destroy (->go this) tag id))
  (OnMouseDown [this] (mouse-down (->go this) tag id))
  (OnMouseEnter [this] (mouse-enter (->go this) tag id))
  (OnMouseExit [this] (mouse-exit (->go this) tag id))
  ;(OnMouseOver [this] (mouseover (->go this tag id)
  (OnMouseUp [this] (mouse-up (->go this) tag id))
  (OnMouseDrag [this] (mouse-drag (->go this) tag id))
  
  (OnCollisionEnter [this other] (collision-enter (->go this) other tag id))
  (OnCollisionExit [this collider] (collision-exit (->go this) collider tag id))
  (OnCollisionEnter2D [this collider] (collision-enter-2d (->go this) collider tag id))
  (OnCollisionExit2D [this collider] (collision-exit-2d (->go this) collider tag id))

  ; (OnCollisionStay [this collider] (collision-stay (->go this collider tag id)
  ;) (OnCollisionStay2D [this collider] (collision-stay-2d (->go this collider tag id)

  (OnTriggerEnter [this collider] (trigger-enter (->go this) collider tag id))
  (OnTriggerExit [this collider] (trigger-exit (->go this) collider tag id))
  (OnTriggerEnter2D [this collider] (trigger-enter-2d (->go this) collider tag id))
  (OnTriggerExit2D [this collider] (trigger-exit-2d (->go this) collider tag id))
  ; (OnTriggerStay [this collider] (trigger-stay (->go this collider tag id)
  ; (OnTriggerStay2D [this collider] (trigger-stay-2d (->go this collider tag id)
  )
  
    
(arcadia.core/defcomponent Use [^String ns]
  (Awake [this] 
    (use (symbol ns) 'pdf.core)
    (awake (->go this) ns))
  (Start [this] (start (->go this) ns))
  (Update [this] (do-deferred)))

(arcadia.core/defcomponent Update [f]
 (Update [this] (when f (f (->go this)))))

(defn route-update [go f]
  (let [c (.AddComponent go Update)]
    (set! (.f c) f)))