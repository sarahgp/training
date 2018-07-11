(ns hn.core
  (:require [clojure.string :as s])
  (:import [org.openqa.selenium By WebDriver WebElement]
           [org.openqa.selenium.chrome ChromeDriver])
  (:gen-class))

(def ^:dynamic *driver* nil)

(defn split-on-space
  [str]
  (s/split str #" "))

(defn correct-cap
  [word]
  (if (not= word "and")
    (s/capitalize word)
    word))

(defn break-up-name
  [el]
   (split-on-space (:name el)))

(defn format-names-count
  [elements]
  (map
   (fn [el]
     (let [name-vec (break-up-name el)]
       (assoc
        el
        :number (last name-vec)
        :name (->> name-vec
                   pop
                   (map correct-cap)
                   (s/join " " )))))
    elements))

(defn new-page-and-summary
  [link]
  (binding [*driver* (doto *driver*
                     (.get link))]
    (let [elements (.findElements *driver* (By/cssSelector ".dictionary-description-text"))
          summary (map #(.getText %) elements)]

      (println summary)
      summary)))

(defn print-genres-summaries
  [{:keys [titles]}]
  (println (map #(str (:name %) ": " (:number %) " artists \n" ) titles)))

(defn scrape
  []
  (binding [*driver* (doto (ChromeDriver.)
                     (.get "http://www.wikiart.org/en/paintings-by-genre"))]

    (let [elements          (.findElements *driver* (By/cssSelector ".dictionaries-list .dottedItem a"))
          titles-and-links  (map #(hash-map :name (.getText %)
                                            :link (.getAttribute % "href" ))
                                            elements)
          formatted-elements (format-names-count titles-and-links)
          elements-with-summaries (map
                                   #(assoc % :summary (new-page-and-summary (:link %)))
                                   formatted-elements)

          titles (map #(.getText %) elements)
          links-to-genre (map #(.getAttribute % "href" ) elements)
          ]

      ;; basic element
      ;; create map with formatted name and number and link
      ;; assoc summary with map
      ;; print everything

      (println elements-with-summaries)
      ;(new-page-and-summary driver (first links-to-genre))
      ;(print-genres-summaries {:titles (format-names-count titles)})


      )

    ;; closes the browser
    (.quit driver)))

(defn -main
  [& args]
  (scrape))


;; Genre: XX artists
;; Paragraph about Genre -> replace with featured paintings
;; Artists Include:
