(ns cljsdku.tools
  (:use clojure.test))

(defn index-2-block [dim ix]
;  (println "index-2-block" ix)
  (quot ix (* dim dim)))


(deftest test-index-2-block
  (is (= 0 (index-2-block 3  0)))
  (is (= 0 (index-2-block 3  1)))
  (is (= 1 (index-2-block 3 10)))
  (is (= 8 (index-2-block 3 80)))
  (is (= 1 (index-2-block 3  9)))
  (is (= 0 (index-2-block 3  8)))
  (is (= 1 (index-2-block 3 17)))
  (is (= 2 (index-2-block 3 18)))
  (is (= 8 (index-2-block 3 72)))
  (is (= 7 (index-2-block 3 71)))
  )
  

  
  
(defn index-2-row [dim ix]
;  (println "index-2-row" ix)
  (let [offset (* dim dim dim)]
   (+ (* (quot ix offset) dim) (mod (quot ix dim) dim))))

(deftest test-index-2-row
  (is (= 0 (index-2-row 3  0)))
  (is (= 0 (index-2-row 3  1)))
  (is (= 1 (index-2-row 3  3)))
  (is (= 8 (index-2-row 3 80)))
  (is (= 7 (index-2-row 3 77)))
  (is (= 0 (index-2-row 3 9)))
  (is (= 0 (index-2-row 3 18)))
  (is (= 2 (index-2-row 3 17)))
  (is (= 3 (index-2-row 3 27)))
  (is (= 4 (index-2-row 3 30)))
  )


(defn index-2-col [dim ix]
;  (println "index-2-col" ix)
  (let [blocksize (* dim dim)] 
    (+ (mod ix dim) (* dim (mod (quot ix blocksize) dim)))))

(deftest test-index-2-col
  (is (= 0 (index-2-col 3  0)))
  (is (= 1 (index-2-col 3  1)))
  (is (= 0 (index-2-col 3  3)))
  (is (= 8 (index-2-col 3 80)))
  (is (= 8 (index-2-col 3 77)))
  (is (= 3 (index-2-col 3 9)))
  (is (= 6 (index-2-col 3 18)))
  (is (= 5 (index-2-col 3 17)))
  (is (= 0 (index-2-col 3 27)))
  (is (= 0 (index-2-col 3 30)))
  )

(defn block-indexes [dim block] 
  ;(println "blockindexes: " block)
  (loop [a 0 e (* dim dim) res []]
;    (println "res: " res)
    (if (>= a e)
      res
      (recur (+ a 1) e (conj res (+ (* block e) a))))))

(deftest test-block-indexes
  (is (= [0 1 2 3 4 5 6 7 8] (block-indexes 3 0)))
  (is (= [72 73 74 75 76 77 78 79 80] (block-indexes 3 8))))

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

(deftest test-col-indexes
  (is (= [0 3 6 27 30 33 54 57 60] (col-indexes 3 0)))
  (is (= [20 23 26 47 50 53 74 77 80] (col-indexes 3 8))))

(defn row-indexes [dim row]
    ;(println "rowindexes" row)
    (loop [a 0 e (* dim dim) res []]
;    (println "res: " res)
    (if (>= a e)
      res
      (recur (+ a 1) e 
        (conj res 
           (+ (* e (+ (* dim (quot row dim)) (quot a dim))) (+ (mod a dim) (* dim (mod row dim)))))))))

(deftest test-row-indexes
  (is (= [0 1 2 9 10 11 18 19 20] (row-indexes 3 0)))
  (is (= [60 61 62 69 70 71 78 79 80] (row-indexes 3 8))))

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
                              
(deftest test-init-indexes
  (let [ix (init-indexes 3)]
    (is (= 2 (count ix)))
    (is (= 3 (count (ix 0))))
    (is (= 4 (count ((ix 0) 0))))
    (is (= 4 (count ((ix 0) 1))))
    (is (= 4 (count ((ix 0) 2))))
    (is (= (row-indexes 2 0) (((ix 0) 0) 0)))
    (is (= (col-indexes 2 0) (((ix 0) 1) 0)))
    (is (= (block-indexes 2 0) (((ix 0) 2) 0)))
    (is (= (row-indexes 2 3) (((ix 0) 0) 3)))
    (is (= (col-indexes 2 3) (((ix 0) 1) 3)))
    (is (= (block-indexes 2 3) (((ix 0) 2) 3)))
    (is (= 3 (count (ix 1))))
    (is (= 9 (count ((ix 1) 0))))
    (is (= 9 (count ((ix 1) 1))))
    (is (= 9 (count ((ix 1) 2))))
    ))
 

(def indexes (init-indexes 4))  




(defn col-indexes_c [dim col]
  (((indexes (- dim 2)) 1) col))

(defn block-indexes_c [dim block]
  (((indexes (- dim 2)) 2) block))

(defn row-indexes_c [dim row]
  (((indexes (- dim 2)) 0) row))

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







