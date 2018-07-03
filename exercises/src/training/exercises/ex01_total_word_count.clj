(ns training.exercises.ex01-total-word-count
  (:require [clojure.string :as s]))

;; Given a string, count the total number of words it has
;; Example:

;; (total-words "this has four words")
;; ;=> 4

;; hint: use s/split with the regex #" " to split the string on
;; spaces, like this:
(s/split "test string" #" ")

(defn split-on-space
  [str]
  (s/split str #" "))

(defn total-words
  [x]
  (if (s/blank? x) 0
      (count (split-on-space x))))

;; test cases
(total-words "this is words long")
(total-words "now it is five words")
(total-words "")
