(ns libs.xml.request
  (:require [libs.xml.parse :as parse])
  (:use [clojure.pprint :only (pprint)]))


;; TODO predicates

(defn attr-pred-fn [f attr val]
  (fn [xml]
    (f ((:attrs xml) attr) val)))

(defn attr= [attr val]
  (attr-pred-fn = attr val))

;; requests

(defn xattr [name path attr-name]
  (fn [xml]
    {name (parse/attr attr-name (if (and path (not-empty path))
                                  (parse/tag-1 path xml)
                                  xml))}))

(defn xval [name path]
  (fn [xml]
    {name (parse/content-1 (if (and path (not-empty path))
                             (parse/tag-1 path xml)
                             xml))}))

(defn xcomp [& reqs]
  (fn [xml]
    (apply merge (map #(% xml) reqs))))

(defn xseq [name path req]
  (fn [xml]
    {name (map #(req %) (parse/tag path xml))}))

(comment defn req-filter [req filter-fn]
  (fn [path xml]
    (filter filter-fn ((parse/req path) xml))))

;;;; test

(def xml-string
 "<?xml version=\"1.0\"?>\n<cv description=\"test document for xml magic\">\n  <skills>\n    <games>\n      <game name=\"quake\" version=\"3\">\n        <experience>3</experience>\n      </game>\n      <game name=\"portal\" version=\"1\">\n        <experience>3</experience>\n      </game>\n      <game name=\"portal\" version=\"2\">\n        <experience>2</experience>\n      </game>\n      <game name=\"half life\" version=\"1\">\n        <experience>5</experience>\n      </game>\n      <game name=\"half life\" version=\"2\">\n        <experience>3</experience>\n      </game>\n      <game name=\"doom\" version=\"2\">\n        <experience>10</experience>\n      </game>\n    </games>\n    <sports>\n      <sport name=\"rma\">\n        <experience>5</experience>\n      </sport>\n      <sport name=\"basketball\">\n        <experience>10</experience>\n      </sport>\n    </sports>\n  </skills>\n  <personal fname=\"John\" lname=\"Doh\">\n  </personal>\n</cv>\n")

(def xml (parse/parse xml-string))

(def r-g-exp (xval :game-experience [:experience]))
(def r-g-name (xattr :game-name [] :name))
(def r-g-version (xattr :game-version [] :version))
(def r-g (xseq :games
               [:skills :games :game]
               (xcomp r-g-name r-g-version r-g-exp)))

(def r-s-exp (xval :sport-experience [:experience]))
(def r-s-name (xattr :sport-name [] :name))
(def r-s (xseq :sports
               [:skills :sports :sport]
               (xcomp r-s-name r-s-exp)))

(def r-p-fname (xattr :first-name [:personal] :fname))
(def r-p-lname (xattr :last-name [:personal] :lname))

(def r (xcomp r-g r-s (xcomp r-p-fname r-p-lname)))

(def rfull (xcomp (xseq :games [:skills :games :game]
                        (xcomp r-g-name r-g-version r-g-exp))
                  (xseq :sports [:skills :sports :sport]
                        (xcomp r-s-name r-s-exp))
                  (xcomp r-p-fname r-p-lname)))


