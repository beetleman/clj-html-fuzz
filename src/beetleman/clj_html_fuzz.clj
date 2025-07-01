(ns beetleman.clj-html-fuzz
  (:require
   [clojure.java.io :as io]
   [compojure.core :refer [context defroutes GET POST]]
   [hiccup2.core :as h]
   [mount.core :as mount :refer [defstate]]
   [org.httpkit.server :as hk-server]
   [ring.middleware.params :refer [wrap-params]]
   [selmer.parser :as selmer]))

(defn hiccup-index
  ([] (hiccup-index nil nil nil))
  ([name color image]
   (str (h/html
         (h/raw "<!DOCTYPE html>")
         [:html
          [:body
           [:h1 "List"]
           [:form {:action "/hiccup" :method "post"}
            [:div
             [:label {:for "name"} "Enter name:"]
             [:input {:type "text" :name "name"}]]
            [:div
             [:label {:for "color"} "Enter color:"]
             [:input {:type "color" :name "color"}]]
            [:div
             [:label {:for "image"} "Enter image:"]
             [:input {:type "text" :name "image"}]]
            [:input {:type "submit" :value "Send Request"}]]
           [:p {:style (str "color:" color)}
            [:img {:src image}]
            name]]]))))

(defn selmer-index
  ([] (selmer-index nil nil nil))
  ([name color image]
   (selmer/render-file (io/resource "beetleman/selmer.tmpl")
                       {:name  name
                        :color color
                        :image image})))

(defroutes app
  (GET "/health" []
    "OK")
  (context "/hiccup" []
    (POST "/" [name color image]
      (hiccup-index name color image))
    (GET "/" []
      (hiccup-index)))
  (context "/selmer" []
    (POST "/" [name color image]
      (selmer-index name color image))
    (GET "/" []
      (selmer-index))))

(defstate server
  :start (hk-server/run-server (-> #'app
                                   wrap-params)
                               {:port 8080
                                :legacy-return-value? false})
  :stop (hk-server/server-stop! server))

(defn -main [& _args]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (mount/stop)
                               (shutdown-agents)
                               (println "Stop"))))
  (println "Start")
  (mount/start))

(comment
  (mount/start)
  (mount/stop))
