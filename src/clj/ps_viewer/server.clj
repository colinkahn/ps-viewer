(ns ps-viewer.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :as resources]
            [ring.util.response :as response]
            [ps-viewer.utils :as utils]
            [cheshire.core :refer [generate-string]])
  (:gen-class))

(defn render-app []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
      "<!DOCTYPE html>
      <html>
        <head>
          <link href=\"https://fonts.googleapis.com/css?family=Inconsolata:400,700\" 
                rel=\"stylesheet\"
                type=\"text/css\">
          <link href=\"css/page.css\" 
                rel=\"stylesheet\"
                type=\"text/css\">
        </head>
        <body>
          <div id=\"root\"></div>
          <script src=\"js/cljs.js\"></script>
        </body>
      </html>"})

(defn handler [request]
  (case (:uri request)
    "/ps" {:status 200
           :headers {"Content-Type" "text/html"}
           :body (generate-string (utils/ps->clj))}
    "/" (render-app)
    (response/redirect "/")))

(def app
  (-> handler
    (resources/wrap-resource "public")))

(defn -main [& args]
  (jetty/run-jetty app {:port 8080}))

