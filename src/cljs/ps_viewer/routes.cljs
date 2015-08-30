(ns ps-viewer.routes
  (:require [colinkahn.flux.dispatcher :as dispatcher]
            [ps-viewer.handlers]
            [bidi.router :as br])
  (:require-macros [colinkahn.flux.dispatcher :refer [defhandler]]))

(def routes ["/" [[["monitor/" :id] :monitor-single]
                  ["monitor" :monitor]
                  [true :monitor]]])

(def router (br/start-router!
              routes
              {:on-navigate
               #(dispatcher/dispatch {:type "receive-location-change"
                                      :location %})
               :default-location {:handler :monitor}}))

(defn nav! [handler & {:as params}]
  (br/set-location! router {:handler handler :route-params params}))
