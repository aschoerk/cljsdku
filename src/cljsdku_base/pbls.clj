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
  ([sudokuArray]
    (let [dim (dim-by-array sudokuArray)]
      (create-pbls dim sudokuArray)))
  ([dim sudokuArray]
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
		        (recur (inc a) e (conj res res1) true)))))))))


(defn pbls-2-poss-mx
  [dim pbls]
  ;(println "pbls: " pbls)
  (let [valnum (* dim dim)
        mx (vec (repeatedly valnum #(vec (repeat (* valnum valnum) false))))
        reducer (fn [mx entry]
          (let [ix (first entry)
                values (last entry)]
            (reduce #(assoc %1 (dec %2) (assoc (%1 (dec %2)) ix true)) mx values)))
        ]
      (reduce reducer mx pbls)
    ))




(defn mx-hidden-single-dim [indexarrays result vals]
  ;(println vals)
  (let  [checkindexarray 
           (fn [result indexarray]
             ;(println indexarray)
					   (if (= 1 (reduce #(if (vals %2) (inc %1) %1) 0 indexarray))
               (do 
               ;(println "found hidden single")
					     (conj result (reduce #(if (vals %2) %2 %1) 0 indexarray)))              
               result))]
    (reduce checkindexarray result indexarrays)))


(defn mx-hidden-singles-per-val
  [dim vals result]
  (let [valnum (* dim dim)
    result1 (mx-hidden-single-dim (row-indexes_c dim) result vals)
    result2 (mx-hidden-single-dim (col-indexes_c dim)  result1 vals)
    result3 (mx-hidden-single-dim (block-indexes_c dim)  result2 vals)]
    result3))

(defn mx-hidden-singles
  [mx]
  (let [dim (dim-by-array (mx 0))
        result #{}]
    (reduce #(mx-hidden-singles-per-val dim %2 %1) result mx)))


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
