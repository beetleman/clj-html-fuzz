(ns beetleman.clj-html-fuzz-test
  (:require
   [beetleman.clj-html-fuzz :as sut]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest]]
   [clojure.walk :as walk]
   [hickory.core :as h]
   [matcher-combinators.test :refer [match?]]))

(let [l (delay (-> (io/resource "beetleman/clj_html_fuzz_payload.txt")
                   io/reader
                   line-seq))]
  (defn payload []
    @l))

(defn cleanup-hiccup [data]
  (walk/postwalk
   (fn [v]
     (cond
       (sequential? v) (into [] (remove #(and (string? %) (str/blank? %))) v)
       (string? v)     (str/replace v  #"\n *"  "")
       :else           v))
   data))

(defn expected-hiccup [action]
  ["<!DOCTYPE html>"
   [:html
    {}
    [:head {}]
    [:body
     {}
     [:h1 {} "List"]
     [:form
      {:action action, :method "post"}
      [:div {} [:label {:for "name"} "Enter name:"] [:input {:name "name", :type "text"}]]
      [:div {} [:label {:for "color"} "Enter color:"] [:input {:name "color", :type "color"}]]
      [:div {} [:label {:for "image"} "Enter image:"] [:input {:name "image", :type "text"}]]
      [:input {:type "submit", :value "Send Request"}]]
     [:p {:style string?}
      [:img {:src string?}]
      string?]]]])

(deftest ^:selmer selmer-test
  (t/testing "injecting name"
    (doseq [name (payload)]
      (let [color "color"
            image "image"]
        (t/is (match?
               (expected-hiccup "/selmer")
               (-> (sut/selmer-index name color image)
                   h/parse
                   h/as-hiccup
                   cleanup-hiccup))))))
  (t/testing "injecting color"
    (doseq [color (payload)]
      (let [name  "name"
            image "image"]
        (t/is (match?
               (expected-hiccup "/selmer")
               (-> (sut/selmer-index name color image)
                   h/parse
                   h/as-hiccup
                   cleanup-hiccup))))))
  (t/testing "injecting image"
    (doseq [image (payload)]
      (let [color "color"
            name  "name"]
        (t/is (match?
               (expected-hiccup "/selmer")
               (-> (sut/selmer-index name color image)
                   h/parse
                   h/as-hiccup
                   cleanup-hiccup)))))))

(deftest ^:hiccup hiccup-test
  (t/testing "injecting name"
    (doseq [name (payload)]
      (let [color "color"
            image "image"]
        (t/is (match?
               (expected-hiccup "/hiccup")
               (-> (sut/hiccup-index name color image)
                   h/parse
                   h/as-hiccup
                   cleanup-hiccup))))))
  (t/testing "injecting color"
    (doseq [color (payload)]
      (let [name  "name"
            image "image"]
        (t/is (match?
               (expected-hiccup "/hiccup")
               (-> (sut/hiccup-index name color image)
                   h/parse
                   h/as-hiccup
                   cleanup-hiccup))))))
  (t/testing "injecting image"
    (doseq [image (payload)]
      (let [color "color"
            name  "name"]
        (t/is (match?
               (expected-hiccup "/hiccup")
               (-> (sut/hiccup-index name color image)
                   h/parse
                   h/as-hiccup
                   cleanup-hiccup)))))))
