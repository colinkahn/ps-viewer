(ns ps-viewer.client
  (:require [reagent.core :as reagent :refer [atom]]
            [colinkahn.ui.scroll-list :as s]
            [colinkahn.flux.getters :refer [getter]]
            [colinkahn.flux.dispatcher :as dispatcher]
            [cljs.core.async :refer [<! >! chan timeout]]
            [ps-viewer.api :as api]
            [ps-viewer.routes :as routes]
            [ps-viewer.handlers]
            [ps-viewer.state :refer [state]]
            [ps-viewer.getters :as gtr])
  (:require-macros [colinkahn.flux.dispatcher :refer [defhandler]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn scroll-list [cols rows positions]
  [:div.psList__container
   {:on-scroll #(dispatcher/dispatch {:type "scroll-ps-list" :event %})}
    [:div.psList__viewport {:style {:height (str (* (count rows) 20) "px")}}
     (for [[i k] positions]
       (let [row (nth rows i)]
       [:div.psList__item {:key k
                           :index i
                           :style (s/transform i 20)
                           :on-double-click #(routes/nav! :monitor-single :id (:pid row))}
        [:div.psList__cell {:key :user} (:user row)]
        [:div.psList__cell {:key :pid} (:pid row)]
        [:div.psList__cell {:key :%cpu} (:%cpu row)]
        [:div.psList__cell.psList__command {:key :command} (:command row)]]))]])

(defn ps-tree-leaf [ppid base-key children open? groups]
  (let [el-key (str base-key ":" ppid)]
    [:div
     [:span {:on-click #(dispatcher/dispatch {:type "toggle-open-group"
                                                         :pid ppid})
                        :class (when children
                                 (if open?
                                   "psTreeLeaf--open"
                                   "psTreeLeaf--closed"))}
      ppid]
     (when children [:button {:on-click #(dispatcher/dispatch {:type "filter-by-ppid"
                                                               :ppid ppid})} ">"])
    (when open?
      [:ul.psTreeBranch
       (for [pid children]
          (let [{open? :open? children :children} (get groups pid)]
            [:li {:key (str el-key ":" pid)}
              [ps-tree-leaf pid el-key children open? groups]]))])]))

(defn ps-tree [groups]
  (let [{open? :open? children :children} (get groups "0")]
    [:ul.psTreeBranch
     [:li [ps-tree-leaf "0" "tree" children open? groups]]]))

(defn sort-classes [col by rev]
  (if (= col by)
    (if rev
      "psSortable psSortable--up"
      "psSortable psSortable--down")
    ""))

(defn sort-col [col]
  (dispatcher/dispatch {:type "sort-scroll-ps-list"
                        :col col}))

(defn monitor []
  (let [new-state @state
        rows (gtr/ps-rows new-state)
        cols (gtr/ps-cols new-state)
        srt (gtr/ps-sort-column new-state)
        rev (gtr/ps-sort-reversed new-state)
        search (gtr/ps-search new-state)
        scroll-top (gtr/ps-scroll-top new-state)
        groups (gtr/ps-groups new-state)
        positions (s/positions 300 20 (count rows) scroll-top)
        filter-ppid (gtr/ps-filter-ppid new-state)]
    [:div.psMonitor
      [:div.psActionBar
       [:input.psActionBar__search
        {:on-change #(dispatcher/dispatch {:type "ps-search-changed"
                                           :event %})
         :value search
         :placeholder "Filter"}]
       (when filter-ppid [:div (str "Filtering by pid " filter-ppid)
                          [:button {:on-click #(dispatcher/dispatch {:type "filter-by-ppid"
                                                                     :ppid nil})}
                           "cancel"]])]
     [:div.psMonitor__body
      [:div.psTree.psMonitor__tree
       [ps-tree groups]]
      [:div.psList.psMonitor__list
        [:div.psList__header
          [:div.psList__cell
          {:class (sort-classes :user srt rev)
            :on-click #(sort-col :user)}
          "USER"]
          [:div.psList__cell
          {:class (sort-classes :pid srt rev)
            :on-click #(sort-col :pid)}
          "PID"]
          [:div.psList__cell
          {:class (sort-classes :%cpu srt rev)
            :on-click #(sort-col :%cpu)}
          "%CPU"]
          [:div.psList__cell.psList__command
          {:class (sort-classes :command srt rev)
            :on-click #(sort-col :command)}
          "COMMAND"]]
        [scroll-list cols rows positions]]]]))

(defn monitor-single []
  (let [new-state @state
        id (gtr/mon-sngl-id new-state)
        ps (gtr/mon-sngl-ps new-state)]
    [:div.psMonitorSingle
     [:dl.psMonitorSingle__list
      [:dt "Started"]
      [:dd (:started ps)]
      [:dt "Process status code"]
      [:dd (:stat ps)]
      [:dt "Real memory usage"]
      [:dd (:rss ps)]
      [:dt "Command"]
      [:dd (:command ps)]
      [:dt "CPU (%)"]
      [:dd (:%cpu ps)]
      [:dt "Memory (%)"]
      [:dd (:%mem ps)]
      [:dt "Terminal"]
      [:dd (:tt ps)]
      [:dt "Process ID number"]
      [:dd (:pid ps)]
      [:dt "Virtual memory size"]
      [:dd (:vsz ps)]
      [:dt "User"]
      [:dd (:user ps)]]]))

(defn app []
  (let [new-state @state
        location-handler (gtr/loc-handler new-state)
        view-map {:monitor [monitor]
                  :monitor-single [monitor-single]}]
    [:main 
     [:div.psGlobalHeader "PS Viewer"]
     [:div.psGlobalPage]
     (get view-map location-handler [monitor])]))

(reagent/render [app] (js/document.getElementById "root"))

(go-loop []
         (let [rows (<! (api/get-ps))]
           (dispatcher/dispatch {:type "receive-raw-ps"
                                 :raw-ps rows}))
         (<! (timeout 5000))
         (recur))
