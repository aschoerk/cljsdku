(ns cljsdku-base.tools
  (:use clojure.test))

(defn index-2-block [dim ix]
;  (println "index-2-block" ix)
  (quot ix (* dim dim)))




  
  
(defn index-2-row [dim ix]
;  (println "index-2-row" ix)
  (let [offset (* dim dim dim)]
   (+ (* (quot ix offset) dim) (mod (quot ix dim) dim))))

(defn index-2-col [dim ix]
;  (println "index-2-col" ix)
  (let [blocksize (* dim dim)] 
    (+ (mod ix dim) (* dim (mod (quot ix blocksize) dim)))))



(defn block-indexes [dim block] 
  ;(println "blockindexes: " block)
  (loop [a 0 e (* dim dim) res []]
;    (println "res: " res)
    (if (>= a e)
      res
      (recur (+ a 1) e (conj res (+ (* block e) a))))))


(defn col-indexes [dim col] 
  ;(println "colindexes:" col)
  (loop [a 0 e (* dim dim) res []]
;    (println "res: " res)
    (if (>= a e)
      res
      (recur (+ a 1) e 
        (conj res 
           (+ 
             (* e (+ (quot col dim) (* dim (quot a dim)))) 
             (+ (* (mod a dim) dim) (mod col dim))))))))


(defn row-indexes [dim row]
    ;(println "rowindexes" row)
    (loop [a 0 e (* dim dim) res []]
;    (println "res: " res)
    (if (>= a e)
      res
      (recur (+ a 1) e 
        (conj res 
           (+ (* e (+ (* dim (quot row dim)) (quot a dim))) (+ (mod a dim) (* dim (mod row dim)))))))))


(defn dim-by-array [array]
  (int (Math/sqrt (Math/sqrt (count array)))))

(defn init-indexes 
  "creates a 3-dim-array, 
    first dim: dimension of sudoku + 2, 
    second dim: row, col, blockindexes
    third dim: the coord of the respective index-array"
  [maxdim]  
  (loop [dim 2 result []]    
    (if (> dim maxdim)
      result
	    (let [maxcoord (* dim dim)
	          create-indexes 
		          (fn [creator]
		            (loop [coord 0 result []]
	               (if (>= coord maxcoord)
	                 result
	               (recur (inc coord) (conj result (creator dim coord))))))]
       (recur (inc dim) 
              (conj result 
                    [(create-indexes row-indexes) 
                     (create-indexes col-indexes) 
                     (create-indexes block-indexes)]))))))
                              


(def indexes (init-indexes 4))  


(defn col-indexes_c 
  ([dim] ((indexes (- dim 2)) 1))
  ([dim col]
  (((indexes (- dim 2)) 1) col)))

(defn block-indexes_c 
  ([dim] ((indexes (- dim 2)) 2))
  ([dim block]
  (((indexes (- dim 2)) 2) block)))

(defn row-indexes_c 
  ([dim] ((indexes (- dim 2)) 0))
  ([dim row]
  (((indexes (- dim 2)) 0) row)))

(defn coord-2-index [dim col row]
  ((col-indexes_c dim col) row))
  

(defn join-indices-at-pos [dim pos]
  (let [a (row-indexes_c dim (index-2-row dim pos))
	      b (apply conj a (col-indexes_c dim (index-2-col dim pos)))]
	        indexes (set (apply conj b (block-indexes_c dim (index-2-block dim pos))))))

(defn join-indices [dim]
  (vec (map #(join-indices-at-pos dim %) (take (* dim dim dim dim) (iterate inc 0)) )))


(def joined-indices [[] [] (join-indices 2) (join-indices 3) (join-indices 4)])
  
(defn join-index-arrays [dim]
  (vec (map #(vec (join-indices-at-pos dim %)) (take (* dim dim dim dim) (iterate inc 0)) )))



(def joined-index-array [[] [] (join-index-arrays 2) (join-index-arrays 3) (join-index-arrays 4)])
  
;(map #(println %) (joined-index-array 3))




(defn check-area
  "indexes describe positions in sudokuArray either horizontally, vertically or block-area. 
   returns true if val can be inserted into this area <==> no value at one of indexes equals val"
  [dim indexes val sudokuArray]
;  (println "checking Area by val" val)
  (loop [i 0 e (* dim dim)]
    (if (>= i e)
      true
      (if (== (sudokuArray (indexes i)) val)
        false
        (recur (inc i) e)))))


;(check-area 3 (row-indexes 3 (index-2-row 3 0)) 1 testsudoku)

(defn single-pos-check 
  "find out which values can be inserted at currentIndex in sudokuArray without directly invalidating the puzzle
   returns an array containing two values: the currentIndex and an array containing all the at this position obviously valid values, 
   value of sudokuArray at position currentIndex must be zero."
  [dim currentIndex sudokuArray]
  (if (not= 0 (sudokuArray currentIndex))
    []    
  (let [rowI (row-indexes_c dim (index-2-row dim currentIndex))
        colI (col-indexes_c dim (index-2-col dim currentIndex))
        blockI (block-indexes_c dim (index-2-block dim currentIndex))
        e (* dim dim)]   
  (loop [val 1 res [] ]
    (if (> val e)
      (if (> (count res) 0)
        (list currentIndex res)
        [])
    (cond 
      (and (check-area dim rowI val sudokuArray)       
           (check-area dim colI val sudokuArray)
           (check-area dim blockI val sudokuArray))
      (do ;(println "possCheck newval: " (inc val) e (conj res val))
       (recur (inc val) (conj res val)))
      :else (recur (inc val) res)))))))


(defn eval-way [way] 
  ;(println way)
  ;(println (last (first (first (first way)))))
  (let [waylist (way 0)
        posslist (map #(let [c (count (last (first %)))] (* c c)) waylist)
        ]
    ;(println posslist)
    (reduce + posslist)))

(eval-way '[(((66 [6 8]) 0))])
(eval-way '[(((66 [6 8]) 0) ((52 [1 4 6]) 0) ((32 [5 9]) 0) ((30 [1 6 9]) 1) ((24 [1 4]) 1) ((16 [1 5]) 1) ((11 [4 5]) 0) 
            ((0 [2 9]) 0) ((68 [1 3]) 0) ((55 [1 2]) 1) ((38 [1 2]) 1) ((21 [2 7]) 1) ((7 [5 7]) 1) ((6 [7 8]) 1))])





