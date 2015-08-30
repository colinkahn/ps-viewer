(ns ps-viewer.utils
  (:require [clojure.string :as str]
            [clojure.java.shell :as shell]))

(defn ps->ppid-grps []
  (into {}
        (map
          (fn [[ppid v]] [ppid (into #{}  (map :pid v))])
          (group-by :ppid
                    (map (fn [l]
                           (zipmap [:pid :ppid]
                                   (clojure.string/split (clojure.string/trim l)
                                                         #"\s+")))
                         (clojure.string/split
                           (:out (shell/sh "ps" "-eo pid=,ppid="))
                           #"\n"))))))

(defn exec-ps []
  (str/split (:out (shell/sh "ps" "aux")) #"\n"))

(defn ps-columns [s]
  (map #(-> % str/lower-case keyword)
       (str/split s #"\s+")))

(defn ps-rows [rows cols]
  (let [c (count cols)
        split #(str/split % #"\s+" c)]
    (map #(zipmap cols (split %)) rows)))

(defn ps->clj []
  (let [[cols & rows] (exec-ps)
        cols (ps-columns cols)]
    {:cols cols
     :rows (ps-rows rows cols)
     :grps (ps->ppid-grps)}))
