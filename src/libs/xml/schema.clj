(ns libs.xml.schema
  (:require [libs.xml.request :as r])
  (:use [clojure.pprint :only (pprint)]))


;;;; schema requests

(declare xschema)

(defmulti xschema-m (fn [[name [type info] :as schema]] type))

(defmethod xschema-m :val [[name [_ path]]]
  (r/xval name path))

(defmethod xschema-m :attr [[name [_ [path attr-name]]]]
  (r/xattr name path attr-name))

(defmethod xschema-m :seq [[name [_ [path request]]]]
  (r/xseq name path (xschema request)))

(defn xschema [schema]
  (if (map? schema)
    (apply r/xcomp (map xschema-m schema))
    schema))

;;; test

(def schema {:games [:seq [[:skills :games :game]
                           {:game-experience [:val  [:experience]]
                            :game-name       [:attr [[] :name]]
                            :game-version    [:attr [[] :version]]}]]
             
             :sports [:seq [[:skills :sports :sport]
                            {:sport-experience [:val [:experience]]
                             :sport-name       [:attr [[] :name]]}]]
             
             :first-name [:attr [[:personal] :fname]]
             :last-name  [:attr [[:personal] :lname]]})

