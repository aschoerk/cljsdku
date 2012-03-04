(ns cljsdku.checks
  (:use cljsdku.tools)
  (:use cljsdku.test-data)
  (:use cljsdku.print)
  (:use cljsdku.pbls)
  (:use clojure.set))

(defn check-sudoku [sudoku-array]
  (print-sudoku sudoku-array)
  (let [dim (dim-by-array sudoku-array)]
  (loop [index 0]
    (if (>= index (count sudoku-array))
      true
      ;else
      (let [poss (single-pos-check dim index (assoc sudoku-array index 0))
            pos (first poss)
            val ((last poss) 0)]
        (cond 
          (not= val (sudoku-array index))
            false
          (not= 1 (count (last poss)))
            false
          :else (recur (inc index))))))))

   
(defn check-sudoku-allow-zero [sudoku-array]
  ;(println "to check-allow-zero: " sudoku-array) 
  ;(print-sudoku sudoku-array)
  (let [dim (dim-by-array sudoku-array)]
    (loop [index 0]
      (if (>= index (count sudoku-array))
        (do 
          ;(println "check-sudoku-allow-zero true") 
          true)
        ;else
        (if (= 0 (sudoku-array index))
          (recur (inc index))
          (let [poss (single-pos-check dim index (assoc sudoku-array index 0))
                pos (first poss)
                vals (set (last poss))]
            ;(println poss pos vals) 
            (cond 
              (not= pos index) 
              (do 
                (println "check-sudoku-allow-zero false1") 
                false)            
              (not (contains? vals (sudoku-array index)))
              (do 
                (println "check-sudoku-allow-zero false2") 
                false)
              :else (recur (inc index))))) 
        ))))


(defn check-res-array0 [res]
  (let [array (res :array)
        pbls  (res :pbls)]
    (map #(if (not= 0 (array (first %))) 
            (throw (Exception. "check-res-array0"))) pbls)
    ))

(defn check-res-array2 [newarray oldarray]
  (map #(if (and (not= 0 %2) (not= %1 %2)) 
          (throw (Exception. "check-res-array2"))) newarray oldarray)
  )
    
(defn check-res-covering-pbls [res]
  (let [pblspos (reduce #(union %1 #{(first %2)}) #{} (res :pbls))]
    (map-indexed #(if (and (= 0 %2) (not= %1 (pblspos %1))) 
                    (throw (Exception. "check-res-covering-pbls")))
                 (res :array))))



        
(defn check-res[res oldarray]
  (if (= res nil)
    nil
    ;else
    (do
		  (check-res-array0 res)
		  (check-res-array2 (res :array) oldarray)
		  (check-res-covering-pbls res)
		  (if (not (check-sudoku-allow-zero (res :array))) (throw (Exception. "check-sudoku-newarray")))
		  ;(println oldarray)
		  ;(if (not (check-sudoku-allow-zero (oldarray))) (throw (Exception. "check sudoku oldarray")))
		  res)))

(check-res {:pbls (create-pbls 2 test-2dim-array2) :array test-2dim-array2} test-2dim-array2)
    
