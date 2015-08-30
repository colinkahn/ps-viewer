(ns ps-viewer.state
  (:require
    [reagent.core :as reagent :refer [atom]]
    [colinkahn.flux.dispatcher :as dispatcher]))

;; Define state
(def state
  (dispatcher/set-state! (atom {:ps-ns {:search ""
                                        :sort-column :%cpu
                                        :sort-reversed true}})))
