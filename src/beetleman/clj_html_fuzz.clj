(ns beetleman.clj-html-fuzz
  (:require
   [compojure.core :refer [context defroutes GET POST]]
   [hiccup2.core :as h]
   [selmer.parser :as selmer]
   [mount.core :as mount :refer [defstate]]
   [org.httpkit.server :as hk-server]
   [ring.middleware.params :refer [wrap-params]]
   [clojure.java.io :as io]))

(defstate db
  :start (atom {::hiccup []
                ::selmer []}))

(defn add-itm [type name color image]
  (let [itm {:name  name
             :color color
             :image image}]
    (get (swap! db update type conj itm) type)))

(defn all-itm [type]
  (get @db type))

(defn hiccup-list [itm-list]
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
          (for [{:keys [name color image]} itm-list]
            [:p {:style (str "color:" color)}
             [:img {:src image}]
             name])]])))

(defn selmer-list [itm-list]
  (selmer/render-file (io/resource "beetleman/selmer.tmpl")
                      {:itms itm-list}))

(defroutes app
  (context "/hiccup" []
    (POST "/" [name color image]
      (-> (add-itm ::hiccup name color image)
          (hiccup-list)))
    (GET "/" []
      (hiccup-list (all-itm ::hiccup))))
  (context "/selmer" []
    (POST "/" [name color image]
      (-> (add-itm ::selmer name color image)
          (selmer-list)))
    (GET "/" []
      (selmer-list (all-itm ::selmer)))))

(defstate server
  :start (hk-server/run-server (-> #'app
                                   wrap-params)
                               {:port 8080
                                :legacy-return-value? false})
  :stop (hk-server/server-stop! server))

(comment
  (mount/start))
