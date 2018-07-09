(ns training.exercises.ex13-multimethods
  (:require [hiccup.core :as h]
            [clojure.set :as set]))

;; ========================================
;; Multimethods
;; ========================================

(defmulti full-moon-behavior
  (fn [were-creature] (:were-type were-creature)))

(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))

(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

(full-moon-behavior {:were-type :wolf
                     :name "Rachel from next door"})

(full-moon-behavior {:name "Andy the baker"
                     :were-type :simmons})



(def moderator-usernames ["bob" "divya" "grogthor, intergalactic warlord"])
(defmulti moderator? class)
(defmethod moderator? String
  [username] (some #(= % username) moderator-usernames))
(defmethod moderator? clojure.lang.IPersistentMap
  [m] (moderator? (:username m)))



;; can dispatch on any transformation of any or all arguments
;; say what!?
(defmulti enterprise-readiness
  (fn [person briggs-meyer] briggs-meyer))

(defmethod enterprise-readiness ["e" "n" "t" "j"]
  [person briggs-meyer]
  (str (:name person) " is enterprise ready!"))

(defmethod enterprise-readiness ["i" "s" "f" "p"]
  [person briggs-meyer]
  (str (:name person) " is not enterprise ready!"))

(enterprise-readiness {:name "Bubba"} ["i" "s" "f" "p"])



;; ========================================
;; Multimethods are good for data-driven behavior
;; ========================================

;; Hiccup converts Clojure data structures to HTML
(h/html [:input {:value "x"}])
(h/html [:textarea {:value "x"}])
(h/html [:input {:type "radio"}])

(defmulti input (fn [input-type _] input-type))

(defmethod input :textarea
  [_ opts]
  [:textarea (dissoc opts :value) (:value opts)])

(defmethod input :select
  [_ {:keys [options] :as opts}]
  (let [selected-value (:value opts)]
    (into [:select (dissoc opts :options)]
          (mapv (fn [{:keys [name value]}]
                  [:option {:value value
                            :selected (= selected-value value)} name])
                options))))

(defmethod input :default
  [input-type options]
  [:input (assoc options :type input-type)])

(input :select {:value   "HI"
                :options [{:name "Alaska" :value "Ak"}
                          {:name "Hawaii" :value "HI"}]})

(input :text {:value "abc"})

;; Use data to produce a custom form
(map (fn [form-field]
       (input (:type form-field) (dissoc form-field :type)))
     [{:name "Favorite Color"
       :type :textarea
       :value "blue"}
      {:name "Favorite State"
       :type :select
       :value "HI"
       :options [{:name "Alaska" :value "Ak"}
                 {:name "Hawaii" :value "HI"}]}])

;; You try:
;; * Create a method that handles radio buttons
;; opts shape:
;;   {:legend [str]
;;    :checked [str, :id of checked]
;;    :buttons [{ :id :name :display-label }]}
(defmethod input :radio
  [_ {:keys [buttons legend checked]}]
    [:fieldset {:legend legend}
      (mapv (fn [opts]
        (let [value (:id opts)
              display-label (:display-label opts)]
          [:div
            [:input (assoc opts
                     (dissoc opts :display-label)
                     {(:checked (= value checked))
                      :type "radio"})]
            [:label {:for value} display-label]]))
            buttons)])

(input :radio {:legend "Elvis Costello Songs"
               :checked "radio-radio"
               :buttons [{ :id "radio-radio" :name "elvis-buttons" :display-label "Radio, Radio" }
                { :id "everyday" :name "elvis-buttons" :display-label "Everyday I Write the Book" }]})

;; * Create a method that handles check boxes
;; opts shape:
;;   {:legend [str]
;;    :checked [set of :ids of checked]
;;    :buttons [{ :id :name :display-label }]}
(defmethod input :checkbox
  [_ {:keys [buttons legend checked]}]
    [:fieldset {:legend legend}
      (mapv (fn [opts]
        (let [value (:id opts)
              display-label (:display-label opts)]
          [:div
            [:input (assoc opts
                     (dissoc opts :display-label)
                     {:checked (contains? checked value)
                      :type "checkbox"})]
            [:label {:for value} display-label]]))
            buttons)])

(input :checkbox {:legend "Elvis Costello Songs"
               :checked #{"radio-radio" "shipbuilding"}
               :buttons [{ :id "radio-radio" :name "elvis-buttons" :display-label "Radio, Radio" }
                         { :id "everyday" :name "elvis-buttons" :display-label "Everyday I Write the Book" }
                         { :id "shipbuilding" :name "elvis-buttons" :display-label "Shipbuilding"}]})

;; * Create a method that takes :date as its type, and produces
;;   select dropdowns for month, day, and year
(def month-days [["January" 31]
              ["February" 28]
              ["March" 31]
              ["April" 30]
              ["May" 31]
              ["June" 30]
              ["July" 31]
              ["August" 31]
              ["September" 30]
              ["October" 31]
              ["November" 30]
              ["December" 31]])

(def month-map (reduce
   (fn [coll pair] (conj coll pair))
   {}
   month-days))

(def month-names (mapv first month-days))
(identity month-names)

(def months (map
             (fn [a] {:name (nth month-names a) :value (inc a)})
             (range (count month-names))))

(identity months)

(defn days [month] (map
                    (fn [a] {:name a :value a})
                    (range 1 (inc (get month-map month)))))
(days "January")

(defn years [start end] (map
                    (fn [a] {:name a :value a})
                    (range start (inc end))))

(years 1960 2010)

(defmethod input :date
    [_ {:keys [month day year]}]
    [:fieldset (input :select { :value month :options months})
               (input :select { :value day :options (days month)})
               (input :select { :value year :options (years 1999 2018)})])

(input :date {:month "December" :day 30 :year 2000})
