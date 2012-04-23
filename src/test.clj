(ns test
  ;(:use cljsdku-base.generator)
  ;(:use cljsdku-base.dbaccess)
  (:use cljsdku-base.tools)
  (:use cljsdku-base.checks)
  (:use cljsdku-base.pbls)
  (:use cljsdku-base.solver)
  (:use clojure.test)
  (:use clojure.set)
)



;(println (map #(do (println %) (try (insert-or-update-puzzle %) (catch Exception e))) examples-3d))
(println "doing creation")

;(println (map #(do (println "*********round " % "*******") (auto-gen1 3 3 100) %) (take 1000 (iterate inc 0))))
(def random (java.util.Random.))

(defn myshuffle
  "Return a random permutation of coll"
  {:added "1.2"}
  [coll]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al random)
    (clojure.lang.RT/vector (.toArray al))))

(defn improve2 [array initiallevel]
  (println "improve2" array)
  (let [dim (dim-by-array array)
        places (vec (myshuffle (remove #(= (last %1) 0) (map-indexed #(list %1 %2) array))))         
        ]   
    ;(println "improve by clear" array)
    ;(println "in places: " places)
    (loop [index 0 a array level initiallevel]
      (if (>= index (count places))
        (if (> level initiallevel) (list a level) nil)
	      (let [pair (places index) 
	            testarray (assoc a (first pair) 0)
	            res (single-solution-by-poss dim testarray)
              newlevel (eval-way (res :ways))]
	        (if (and (= 1 (count (res :results))) (<= level newlevel)) ; (res :maxlevel) (res :level))))
             (do 
               ; (println  "way" (res :ways))
               ; (println (thread-name) "in level" level "->" (res :level) "improved to " testarray  )
               ; (when (> newlevel 50) (insert-or-update-puzzle-2 (res :result) testarray newlevel))
	             (recur (inc index) testarray newlevel)) ; (* (res :maxlevel) (res :level))))
	           (recur (inc index) a level)))))))

(defn improve-by-solution [array solution initiallevel]
  (let [dim (dim-by-array array)
        filledplaces (vec (myshuffle (remove #(not= (last %1) 0) (map-indexed #(list %1 %2) array))))         
        ]   
    ;(println "improve by clear" array)
    ;(println "in places: " places)
    (loop [index 0 a array level initiallevel]
      (println "loop" index "level" level)
      (if (>= index (count filledplaces))
        (list a level)
	      (let [pair (filledplaces index) 
	            testarray (assoc array (first pair) (solution (first pair)))
              results (filter #(not= nil %) (map #(do (println %) (improve2 testarray level)) (take 20 (iterate inc 0))))  
	            ]          
           (println "results: " (count results))
           (if (> (count results) 0)
	       	   (recur (inc index) testarray level)
             (recur (inc index) a level))
      )))
    (println "after loop")
    ))




(println "ready doing creation")

; (improve-by-solution [] [] 20)

(println (improve-by-solution 
 [0 0 4 0 0 6 3 0 0 0 0 5 0 1 0 0 0 0 0 0 8 0 0 0 0 0 9 0 0 0 0 0 0 0 2 3 7 0 0 0 0 3 0 5 0 2 0 0 0 0 7 0 0 1 0 0 0 5 0 0 0 0 9 0 4 0 0 7 1 3 0 0 0 0 0 0 8 0 1 0 4]
 [2 1 4 9 5 6 3 8 7 9 3 5 8 1 7 2 6 4 6 7 8 3 4 2 5 1 9 4 6 1 8 9 5 7 2 3 7 9 8 1 2 3 4 5 6 2 3 5 4 6 7 8 9 1 1 3 8 5 4 2 6 7 9 5 4 9 6 7 1 3 8 2 7 2 6 9 8 3 1 5 4]
 91))

