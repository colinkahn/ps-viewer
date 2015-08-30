(ns ps-viewer.api
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! >! chan]]
            [ps-viewer.utils :refer [json->clj]])
  (:require-macros [cljs.core.async.macros :refer  [go go-loop]]
                   [ps-viewer.api :refer [if-success-let]]))

(defn get-ps []
  (let [ps-chan (chan)]
    (go (if-success-let [resp (<! (http/get "/ps"))]
          (>! ps-chan (json->clj (:body resp)))
          (>! ps-chan (js/Error. "ps exploded"))))
    ps-chan))
