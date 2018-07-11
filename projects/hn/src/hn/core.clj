(ns hn.core
  (:require [clojure.string :as s])
  (:import [org.openqa.selenium By WebDriver WebElement]
           [org.openqa.selenium.chrome ChromeDriver])
  (:gen-class))

(defn split-on-space
  [str]
  (s/split str #" "))

(defn format-names-count
  [titles]
  (let [splits (map split-on-space titles)
        correct-cap (fn [word] (if (not= word "and") (s/capitalize word) word)) ]
    splits
    (map #(hash-map :name   (->> %
                                 pop
                                 (map correct-cap)
                                 (s/join " " ))
                    :number (last %))
         splits)))

(format-names-count (list "abstract 15833" "advertisement 164" "allegorical painting 1157"))

(defn print-genres-summaries
  [{:keys [titles]}]
  (println (map #(str (:name %) ": " (:number %) " artists \n" ) titles)))

(defn scrape
  []
  (let [driver (doto (ChromeDriver.)
                     (.get "http://www.wikiart.org/en/paintings-by-genre"))]

    (let [elements (.findElements driver (By/cssSelector ".dictionaries-list .dottedItem a"))

          titles (map #(.getText %) elements)
          links-to-genre (map #(.getAttribute % "href" ) elements)]

      ;(println titles)
      (print-genres-summaries {:titles (format-names-count titles)})
      #_(doseq [title titles]
        (println title))

      )

    ;; closes the browser
    (.quit driver)))

(defn -main
  [& args]
  (scrape))


;; Genre: XX artists
;; Paragraph about Genre
;; Artists Include:
