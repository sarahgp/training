(ns training.exercises.ex03-banned-words
  (:require [clojure.string :as str]
             [clojure.set :as set]
             [training.exercises.ex01-total-word-count :refer [split-on-space]]))

;; Your mission is to write a function that takes a string and returns
;; true if it contains banned words

(def banned-words #{"bananas" "banter" "bantam"})
(def not-empty? (complement empty?))

(defn banned?
  [check]
  (not-empty? (set/intersection banned-words (set (split-on-space check)))))

(banned? "bantam monster")
(banned? "tiger monster")
