(ns ps-viewer.handlers
  (:require [ps-viewer.state :refer [state]]
            [ps-viewer.getters :as gtr])
  (:require-macros [colinkahn.flux.dispatcher :refer [defhandler]]))

(defhandler :location-ns [action]
  "receive-location-change"
  (:location action))

(comment defhandler :test [action]
  "scroll-ps-list"
  (println (pr-str (-> (:event action) (.. -target -scrollTop)))))

(defhandler :ps-ns [action]
  "scroll-ps-list"
  {:scroll-top (-> (:event action) (.. -target -scrollTop))}
  "receive-raw-ps"
  (let [{rows :rows cols :cols} (:raw-ps action)]
    {:rows rows
     :cols cols})
  "ps-search-changed"
  {:search (-> (:event action) (.. -target -value))}
  "sort-scroll-ps-list"
  (let [col (:col action)
        srt (gtr/ps-sort-column @state)
        rev (gtr/ps-sort-reversed @state)]
    (if (= col srt)
      {:sort-reversed (not rev)}
      {:sort-column col :sort-reversed false})))
