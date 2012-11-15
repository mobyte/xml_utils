(ns libs.xml.parse
  (:require [clojure.xml :as xml]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml])
  (:use [clojure.pprint :only (pprint)]))


(defn parse [xml-string]
  (xml/parse (java.io.ByteArrayInputStream. (.getBytes xml-string))))

(defn attr [attr xml]
  (get (:attrs xml) attr))

(defn content [xml]
  (:content xml))

(def content-1 (comp first content))

(defn tag-1 [path xml]
  (reduce (fn [current next-tag]
            (first (filter #(= next-tag (:tag %))
                           (content current))))
          xml
          path))

(defn tag [path xml]
  (filter #(= (last path) (:tag %))
          (content (tag-1 (drop-last path) xml))))


