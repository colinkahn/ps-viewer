(ns ps-viewer.utils)

(defn json->clj [json] (js->clj (.parse js/JSON json) :keywordize-keys true))
