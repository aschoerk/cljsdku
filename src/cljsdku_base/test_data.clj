(ns cljsdku-base.test-data
  (:use clojure.java.io))

(def test-2dim-array [1 2 3 4 3 4 1 2 2 1 4 3 0 0 0 0])

(def test-2dim-wrong-array [1 2 3 4 3 4 1 2 2 1 4 3 3 0 0 0])

(def test-2dim-array4 [1 2 3 4 3 4 1 2 2 1 4 3 0 0 0 0])

(def test-2dim-array2 [1 2 3 4 0 0 1 2 2 1 4 3 0 0 0 0])

(def test-2dim-array3 [1 2 4 3 0 0 1 2 2 1 3 4 0 0 0 0])


(defn read-examples []
	(with-open [examples-reader  (reader (resource "examples.txt"))]
	  (let [lines (line-seq examples-reader)
         s (transient [])]
     (doseq [l lines] 
       (conj! s l))
     (persistent! s))))

;(count (read-examples))
;(println (count (read-examples)))
(def examples-3d 
  (map 
    #(vec (map (fn [s] (. Integer parseInt s)) (re-seq (re-pattern "[0-9]+") %)))
    (vec (read-examples))))

;(println "examples-3d" (count examples-3d) (nth examples-3d 2))
                       
; (def examples-3d (into (into (into examples-3d-1 examples-3d-2) examples-3d-3) examples-3d-4))
