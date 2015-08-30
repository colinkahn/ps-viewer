(ns ps-viewer.getters
  (:require [colinkahn.flux.getters :refer [getter]]))

(def ps-sort-column (getter [:ps-ns :sort-column]))

(def ps-sort-reversed (getter [:ps-ns :sort-reversed]))

(def ps-search (getter [:ps-ns :search]))

(def raw-ps-rows (getter [:ps-ns :rows]))

(def converted-ps-rows (getter (fn [rows]
                                 (map (fn [{user :user
                                            pid :pid
                                            cpu :%cpu
                                            cmd :command}]
                                        {:user user
                                         :pid (js/parseInt pid 10)
                                         :%cpu (js/parseFloat cpu 10)
                                         :command cmd
                                         :search-str
                                         (clojure.string/lower-case
                                           (str user "|" pid "|" cpu "|" cmd))})
                                      rows))
                               [:ps-ns :rows]))
(defn reverse-compare [a b]
  (compare b a))

(defn has-str? [src s]
  "check if source string src contains s"
  (not= (.indexOf src s) -1))

(def ps-rows (getter (fn [rows sort-col sort-reversed search]
                       (let [search (clojure.string/lower-case search)]
                         ((if (empty? search)
                            identity
                            (fn [rows]
                              (filter #(has-str? (:search-str %) search) rows)))
                            (sort-by
                              #(get % sort-col)
                              (if sort-reversed reverse-compare compare)
                              rows))))
                     converted-ps-rows
                     ps-sort-column
                     ps-sort-reversed
                     ps-search))

(def ps-cols (getter #(map keyword %) [:ps-ns :cols]))

(def ps-scroll-top (getter [:ps-ns :scroll-top]))

(def loc-handler (getter [:location-ns :handler]))

(def loc-params (getter [:location-ns :route-params]))

(def mon-sngl-id (getter (fn [handler params]
                                     (when (= handler :monitor-single)
                                       (:id params)))
                         loc-handler
                         loc-params))

(def mon-sngl-ps (getter (fn [id rows]
                           (first (filter #(= (:pid %) id) rows)))
                         mon-sngl-id
                         raw-ps-rows))
