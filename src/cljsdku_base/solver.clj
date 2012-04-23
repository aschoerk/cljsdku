(ns cljsdku-base.solver
  (:use cljsdku-base.tools)
  (:use cljsdku-base.print)
  (:use cljsdku-base.test-data)
  (:use cljsdku-base.checks)
  (:use cljsdku-base.pbls)
  (:use clojure.test)
  (:use clojure.set))
  ;(:use clojure.data))

(defn check-one-pos [dim array pos val]
   (let [index-array ((joined-index-array dim) pos)]
     ;(println index-array)
     (loop [index 0]
       (if (>= index (count index-array))
         true
         ;else
	       (if (= val (array (index-array index)))
	         false
	         (recur (inc index)))))))


(defstruct solveres :found? :result :level :results :ways :maxlevel)

(defn solvres-plus-maxlevel [res maxlevel] 
  (struct solveres (res :found?) (res :result) (res :level) (res :results) (res :ways) (max maxlevel (res :maxlevel)))) 
  
(defn extract-nonrelated-singles [dim pbls sudoku-array]
  ;(println "pbls: " pbls)
  (loop [index 0 array sudoku-array values-done #{}]
    ;(println "index: " index array values-done)
    (if (>= index (count pbls))
        {:newindex index :array array}      
	    ;else
	    (let [current-pos (pbls index)]
	      (if (> (count (last current-pos)) 1)
	        {:newindex index :array array}
	        ;else
	        (let [val ((last current-pos) 0)]
	          (if (contains? values-done val)
              (let [actpos (single-pos-check dim (first current-pos) array)]
                (if (empty? actpos)
                  {:newindex -1 :array array}
                ;else
                (recur (inc index) (assoc array (first current-pos) val) (conj values-done val))))	              
              ;else
	            (recur (inc index) (assoc array (first current-pos) val) (conj values-done val)))))))))
        



(set! *warn-on-reflection* true)

(defn insert-pos [pbls sudoku-array dim pos value]
  ;(println "insert-pos: " pbls sudoku-array pos value)
  (if (not= 0 (sudoku-array pos))
    (throw (Exception. "insert-pos at not empty position"))
    ;else
    (let [newsudoku (assoc sudoku-array pos value)
          pos-filter (fn [ixs] (filter (fn [ix] (= 0 (newsudoku ix))) ixs))
	        indexes ((joined-indices dim) pos) ]
      ;(println "insert-pos 2")
      (loop [index 0 newpbls []]
      ;(println "insert-pos 3")
        (if (>= index (count pbls))
          {:array newsudoku :pbls newpbls }
          (let [actpos (pbls index)]
            ;(println "factpos" (first actpos) "indexes" indexes)
	          (if (not (contains? indexes (first actpos)))
              (recur (inc index) (conj newpbls actpos)) 
              ;else
              (if (= (first actpos) pos)
                (recur (inc index) newpbls )
                (let [newvals (vec (remove #(= % value) (last actpos)))]
                  (if (> (count newvals) 0)
                     (recur (inc index) (conj newpbls (list (first actpos) newvals)))
                     ;else
                     nil
                     ))))))))))
      
        
(println "test-2dim-array2" test-2dim-array2 (create-pbls 2 test-2dim-array2))

(insert-pos 
  (create-pbls 2 test-2dim-array2) test-2dim-array2 2 15 2)

(defn single-poss-mapper [singles sin-ixs all-ixs actpos]
  (let [pos (first actpos)]
	   ;(println "pos" pos)
	   (if (= pos (all-ixs pos))
	     (do
	       ;(println "found in all-ixs")
	       (loop [index 0 curr-pos actpos]
           (let [vals (last curr-pos)]
		         ;(println "singleindex" index "until" (count singles) "curr-pos" curr-pos)
		         (if (>= index (count singles))
		           curr-pos
		           ;else
		           (do 	             
		             ;(println "sinixs" (sin-ixs index) "pos" pos)
		             (if (= pos ((sin-ixs index) pos))                         
		               (do
		                 ;(println "found pos" pos "in" (sin-ixs index)) 
		                 (let [currval ((last (singles index)) 0)
		                       newvals (remove (fn [val] (= val currval)) vals)]
		                   ;(println "newpos:" pos "removed val:" currval "newvals:" newvals)	                       
		                   (recur (inc index) (list pos (vec newvals)))))
		               ; else
		               (do
		                 ;(println "recurring")
		                 (recur (inc index) curr-pos)) ))
		          ))))
        actpos
      )))

(defn single-pos-entries [dim singles sudoku-array]
  (loop [index 0 array sudoku-array]
      ;(println "arrayloop" index array)
      (if (>= index (count singles))
         array
         ;else
         (let [pos (first (singles index))
               currval ((last (singles index)) 0)]
           ;(println "setting at pos" pos currval)
           (if (not= 0 (array pos))
             (println "logical error in single-pos-entries"))
           (if (check-one-pos dim array pos currval)
             (recur (inc index) (assoc array pos currval))
             nil)))))



(defn handle-singles [pbls sudoku-array dim]
  ;(println "handle-singles" pbls)
  (let [singles (extractsingles pbls)]
    ;(println "handle-singles: " singles)
    (if (= 0 (count singles))
      (do ;(println "count singles == 0")
        ;(pbls-array-test pbls sudoku-array "handle-singles")        
        {:array sudoku-array :pbls pbls })
      ;else
      (let [nonsingles (filter (fn [poss] (> (count (last poss)) 1)) pbls)
        sin-ixs (vec (map #((joined-indices dim) (first %)) singles))
        all-ixs (reduce (fn [a b]  (union a b)) #{} sin-ixs)]
        ;(println "singles: " singles  "nonsingles" nonsingles "sin-ixs:"  sin-ixs "all-ixs:"  all-ixs)
        ;(print-sudoku sudoku-array)
        ;(println "singles: " singles)
        (let 
          [respbls 
             (map #(single-poss-mapper singles sin-ixs all-ixs %) nonsingles)
           resarray (single-pos-entries dim singles sudoku-array)
           countinvalid (count (filter #(= 0 (count (last %))) respbls))]
           ;(println "handle-singles res" resarray "pbls "respbls "countinvalid" countinvalid)
           (if (or (= resarray nil) (> countinvalid 0))
             nil
           (recur respbls resarray dim)
           ))))))



(handle-singles  ['(2 [4]) '(5 [4]) '(8 [2 4]) '(11 [4]) '(12 [2 4]) '(4 [3 4]) '(6 [1 2 4]) '(7 [1 2 4]) '(10 [2 3 4]) '(14 [1 2 4]) '(15 [1 2 4])] 
                 [1 2 0 3 0 0 0 0 0 1 0 0 0 3 0 0] 2)
                

(handle-singles (create-pbls 2 test-2dim-array2) test-2dim-array2 2)

(handle-singles (create-pbls 2 test-2dim-array3) test-2dim-array3 2)



(defn iterate-pbls [dim index cur-pbls array res level way]
  "iterate through possibilities starting with first entry, index in value array
   first check if singles are there and fill those recursively out"
  ;(pbls-array-test cur-pbls array "iterate-pbls")   
  (if (< 10 (count (res :results)))
    res
    ;else
  (if (= 0 (count cur-pbls))
    (do 
      ;(println "solution" (count (res :results)) "level:" level array)
      ;(print-sudoku dim array)      
      (struct solveres true array level (conj (res :results) array) (conj (res :ways) way) (max level (res :maxlevel))) ; found
      )         
    (let [sinres (handle-singles cur-pbls array dim)]
      ;(println "cur-pbls" cur-pbls "array" array)
      ;(println "singleres" sinres)
      (if (= sinres nil)
        (do ;(println "ready iterate") 
          (solvres-plus-maxlevel res level)) ; not found                  
        ;else
	      (let 
	         [cur-pbls (vec (sinres :pbls))
	          array (sinres :array)]
          (if (= 0 (count cur-pbls))
            (do 
              ;(println "solution" (count (res :results)) "level by singles:" level array)
              ;(print-sudoku dim array)      
              (struct solveres true array level (conj (res :results) array) (conj (res :ways) way) (max level (res :maxlevel))) ; found
              )  
				        ;else
            (let 
              [actpos (first cur-pbls)
               poss-values (last actpos)]                       
              (if (= index (count poss-values))
                  (solvres-plus-maxlevel res level) ; not found
                ;else
                (do   
                  (if (< level 3)
		                (do
		                  (printf (str "%" (+ level 3) "d: %s (%d)") level (str actpos) index)
		                  (println)))     
                  (let [tmpres (insert-pos cur-pbls array dim (first actpos) (poss-values index)) ]
                    (if (= nil tmpres)
                      (do 
                        (iterate-pbls dim (inc index) cur-pbls array res level way))                          
                      ; now go deeper
                      (let [newres (iterate-pbls dim 0 (tmpres :pbls) (tmpres :array) res (inc level) way)]
                        ; don't forget other possibilities at actpos
                        (iterate-pbls dim (inc index) cur-pbls array newres level way)
                        )))))))))))))
(def iterate-pbls-2)

(def ^:dynamic *maxresults* 50)

(defn iterate-pbls-1 [dim pbls array res level way]
  (if (< *maxresults* (count (res :results)))
    res
    ;else
    (if (= 0 (count pbls))
      (do 
        ;(println "solution" (count (res :results)) "level:" level array)   
        ;(println "  solway" (reverse way))
        (struct solveres true array level (conj (res :results) array)  (conj (res :ways) way) (max level (res :maxlevel))) ; found
        )         
    (let [sinres (handle-singles pbls array dim)]
      (if (= sinres nil)
        (do ;(println "ready iterate") 
          (solvres-plus-maxlevel res level)) ; not found                  
        ;else
	      (let 
	         [cur-pbls (vec (sinres :pbls))
	          array (sinres :array)]
          (if (= 0 (count cur-pbls))
            (do 
              ;(println "solution" (count (res :results)) "level by singles:" level array)
              ;(println "  solway" (reverse way))
              ;(print-sudoku dim array)      
              (struct solveres true array level (conj (res :results) array)  (conj (res :ways) way) (max level (res :maxlevel))) ; found
              )  
            ;else
            (iterate-pbls-2 dim cur-pbls array res level way))))))))

(defn iterate-pbls-2 [dim pbls array orgres level way]
  "pbls contains no singles, first one is to be tested"
  (loop [index 0 res orgres]
		(let 
		  [actpos (first pbls)
		   poss-values (last actpos)
	     newway (conj way (list actpos index))]                       
		  (if (= index (count poss-values))
		      (solvres-plus-maxlevel res level) ; not found
		    ;else
		    (do   
		      (if (< level 0)
		        (do
		          (printf (str "%" (+ level 3) "d: %s (%d)") level (str actpos) index)
		          (println)))     
		      (let [insertres (insert-pos pbls array dim (first actpos) (poss-values index)) ]
		        (if (= nil insertres)
		          (do ; could not insert this possibility, try next one
		            ;(iterate-pbls-2 dim (inc index) pbls array res level way))  
                (recur (inc index) (solvres-plus-maxlevel res level)))
		          ; else could insert, go deeper, now also handle possibly created singles
		          (let [newres (iterate-pbls-1 dim (insertres :pbls) (insertres :array) res (inc level) newway)]
		            ; but don't forget other possibilities at actpos
		            ;(iterate-pbls-2 dim (inc index) pbls array newres level way)
                (recur (inc index) newres)
		            ))))))))
	            
(defn solve-by-poss [dim sudoku-array]
  (let [pbls (vec(sort #(< (count (last %1)) (count (last %2)))(create-pbls dim sudoku-array)))]
      (try
        (iterate-pbls-1 dim pbls sudoku-array (struct solveres false nil -1 [] [] -1) 0 ()) 
        (catch Exception e (println "error: " e)))       
    ))

(defn single-solution-by-poss [dim sudoku-array]
  (binding [*maxresults* 1]
    (solve-by-poss dim sudoku-array)))
						

; (solve-by-poss 2 test-2dim-array3)
  
(solve-by-poss 2 [1 0 0 3 0 0 0 0 0 1 0 0 0 3 2 0]) 

(solve-by-poss 2 [1 0 0 3 0 0 0 0 0 1 0 0 0 3 0 0]) 
                    
  
(defn solve [dim sudokuArray res level]
  (let [pbls (create-pbls dim sudokuArray)]
    (if (empty? pbls)
      res     
      (if (integer? (first pbls))
        (do
          (println "level: " level "found(" (count (res :results)) "): " sudokuArray)
          (struct solveres true sudokuArray level (conj (res :results) sudokuArray)) (max level (res :maxlevel)))          
        ; else
        (do 
          ;(print-sudoku dim sudokuArray)
			    ;(println pbls)
			    ;(println "******************")
			    ;(print-pbls dim pbls)
			    ;(println "******************")
	        (let [single-extract-result (extract-nonrelated-singles dim pbls sudokuArray) 
               tmpsudokuarray (single-extract-result :array)
               newindex (single-extract-result :newindex)]
             (cond 
               (< newindex 0) 
                 res
               (or (> newindex 0) (>= newindex (count pbls)))
                 (solve dim tmpsudokuarray res (inc level))
                 ;else
               :else
                 (let [actPoss (pbls newindex)]
				          (loop [index 0 actres res]
	                  (if (< level 1)
	                    (do 
	                      (printf (str "%" level "d: %s (%d)") level (str actPoss) index)
	                      (println)))
						        (let [val ((last actPoss) index) newsudokuarray (assoc tmpsudokuarray (first actPoss) val)]
						          (let [newres (solve dim newsudokuarray actres (inc level))]                
						             (if (or (>= index (dec (count (last actPoss)))) (> (count (newres :results)) 30))
						               newres
	                         ; else
						               (recur (inc index) newres))))))
		             )))))))



(defn sudoku [sudoku-array] 
  (let [
        dim (dim-by-array sudoku-array)
        res (time (solve-by-poss dim sudoku-array) )
        ;res2 (time (solve dim sudoku-array (struct solveres false nil -1 []) 1)) 
        ]
    (if (res :found?)
      (if (not (check-sudoku (res :result)))
        (println "invalid result")
      ;else
        (do
		      (println "original: " sudoku-array)
		      ;(print-sudoku dim sudoku-array)
		      (println "solved: " (res :result))
		      ;(print-sudoku dim (res :result))
		      ;(println "*************************************************************** ")
	        res))
      (do 
	      (println "no solution found")
	      (res :result))
      )))

    

    
         

;(sudoku testsudoku)

(comment 
  "
(defn initPossibilities [sudokuArray dim]
  (loop [i 0 e (* dim dim) res []] 
    (if (== 0 (sudokuArray i))
      (recur (inc i) e (conj res (possCheck i)))
    (recur (inc i) e []))))


(defn solveinner [currentIndex sudokuArray dim]
  (loop [startIndex currentIndex backtrack]
    (cond 
      (== currentIndex (* dim dim)) (true)
      (== 0 (sudokuArray currentIndex) (recur startIndex (inc currentIndex)))
      (if (tryIndex )
        true
       (recur startIndex startIndex (inc backTrack))))))")



      


(comment 
  "(sudoku (examples 1))
  (
	  loop [index 0]
	    (if (< index (count examples))
	      (do 
	        (sudoku (examples index))
	        (recur (inc index))
	        )
	      (println \"ready\")))"
)   
