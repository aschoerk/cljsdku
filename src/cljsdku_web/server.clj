(ns cljsdku-web.server
  (:require [noir.server :as server])
  (:gen-class))

(server/load-views "src/cljsdku_web/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8081"))]
    (server/start port {:mode mode
                        :ns 'cljsdku-web})))

