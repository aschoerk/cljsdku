(ns cljsdku-base.pbls
  (:use cljsdku-base.tools)
  )

(defn extractsingles [pbls]
  (vec (filter (fn [poss] (= (count (last poss)) 1)) pbls)))
       
(defn extractdoubles [pbls]
  (vec (filter (fn [poss] (= (count (last poss)) 2)) pbls)))
              
(defn extractnonsingles [pbls]
  (vec (filter (fn [poss] (> (count (last poss)) 1)) pbls)))

(defn pbls-array-test [pbls array info]
  (if (not= (count pbls) (count (filter #(= 0 %) array)))
    (println "discrepancy at" info)))


(defn create-pbls 
  "gets all possible values for each empty (0) places and add these together with the position in the resultarray.
   the resultarray is sorted so the position with the shortest arrays, the least possibilities are at the beginning."
  [dim sudokuArray]
  ;(println "pos-check")
  (loop [a 0 e (* dim dim dim dim) res [] foundNotEmpty false]
    ;(println "pos-check: " a)
    (if (>= a e)
      (if foundNotEmpty
        (vec (sort (fn [a b] (< (count (last a)) (count (last b)))) res))
        (do ;(print-sudoku dim sudokuArray) 
          sudokuArray))
      (do 
        (if (not= 0 (sudokuArray a))
          (recur (inc a) e res foundNotEmpty)
		      (let [res1 (single-pos-check dim a sudokuArray)]
		        (if (empty? res1)
		          (do ;(println "not solution found") 
              nil)
		        (recur (inc a) e (conj res res1) true))))))))



(defn pbls-complexity 
  ([dim array]
	  (let [pbls (create-pbls dim array)
	        res  (if (= nil pbls)
						      0
						      (pbls-complexity pbls))]    
	    ;(println res)
	    res))
  ([pbls]
    ;(println (list? (first pbls)) pbls)
    (if (and (not= nil pbls) (< 0 (count pbls)) (list? (first pbls)))
      (reduce #(+ %1 %2) (map #(count (last %)) pbls))
      0)))

(defn pbls-complexity2
  ([dim array]
	  (let [pbls (create-pbls dim array)
	        res  (if (= nil pbls)
						      0
						      (pbls-complexity2 pbls))]    
	    ;(println res)
	    res))
  ([pbls]
    ;(println (list? (first pbls)) pbls)
    (if (and (not= nil pbls) (< 0 (count pbls)) (list? (first pbls)))
      (reduce #(+ %1 %2) (map #(* (count (last %)) (count (last %))) pbls))
      0)))
