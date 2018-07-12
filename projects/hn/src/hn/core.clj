(ns hn.core
  (:require [clojure.string :as s])
  (:import [org.openqa.selenium By WebDriver WebElement]
           [org.openqa.selenium.chrome ChromeDriver])
  (:gen-class))

(def ^:dynamic *driver* (doto (ChromeDriver.)
                   (.get "http://www.wikiart.org/en/paintings-by-genre")))

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

(defn pics-list
  [link]
  (binding [*driver* (doto *driver*
                     (.get link))]
    (let [elements (.findElements *driver* (By/cssSelector ".title-block .artwork-name"))
          works (filter (complement s/blank?) (map #(s/trim (.getText %)) elements))]
      (apply vector works))))

(defn print-info!
  [{:keys [name number pics link]}]
  (println (str name ": " number " pictures \n"
                "Images include: " (s/join ", " pics)  "\n"
                "More information at: " link)))

(defn scrape
  []
    (let [elements                (.findElements *driver* (By/cssSelector ".dictionaries-list .dottedItem a"))
          titles-and-links        (map #(hash-map :name (.getText %)
                                                  :link (.getAttribute % "href" ))
                                                  elements)
          formatted-elements      (format-names-count titles-and-links)
          elements-with-pics (map
                                   #(assoc % :pics (pics-list (:link %)))
                                   (take 1 formatted-elements))]


      (doall (map print-info! elements-with-pics))
      (spit "pics.edn" (pr-str elements-with-pics)))

    ;; closes the browser
    (.quit *driver*))

(defn -main
  [& args]
  (scrape))
