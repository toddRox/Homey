(ns rox.todd.util
  (:import java.io.File)
  (:require [clojure.java.io :as io])
  (:require [clojure.walk :as walk])
  )




(defn search [dir, recur, includeFile]
  (let [files (filter #(or (and (.isFile %) (includeFile %)) recur) (.listFiles dir))]
    (cons dir (map (fn [f] (if (and (.isFile f) (includeFile f)) f (search f recur includeFile))) files))
  )
)

(defn clj2java [data]
  (walk/postwalk
   (fn [x]
     (cond
       (map? x) (java.util.HashMap. x)
       (vector? x) (java.util.ArrayList. x)
       (set? x) (java.util.HashSet. x)
       (list? x) (java.util.ArrayList. x)
       (seq? x) (java.util.ArrayList. x)  
       (keyword? x) (name x)
       (symbol? x) (name x)
       :else x))
   data))



(comment
  (search (io/file "~/Pictures/") true (fn [f] true))
  )