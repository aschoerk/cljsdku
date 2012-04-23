(ns cljsdku-base.generator
  (:use cljsdku-base.tools)
  (:use cljsdku-base.print)
  (:use cljsdku-base.test-data)
  (:use cljsdku-base.checks)
  (:use cljsdku-base.pbls)
  (:use cljsdku-base.solver)
  (:use clojure.test)
  (:use clojure.set)
  (:use cljsdku-base.dbaccess))
  ;(:use clojure.data))

 
  
(defn discriminate [dim sudoku-array results]
  (let [c (count results)
        a (results (dec c))
        b (results (- c 2))
        diffs (filter (fn [s] (not= (first s) (last s))) (partition 3 (interleave a (iterate inc 0) b )))]
    (println "discriminate: " diffs)))


  
  
(defn improvex [array]
  (let [dim (dim-by-array array)
        pbls (create-pbls dim array)
        singles (extractsingles pbls)
        single-dependence-mapper 
          (fn [poss]
            (let [pos (first poss)
                  indices ((joined-index-array dim) pos)
                  neighbour-value-map (group-by #(array %) indices)]
              ;(println "mapper" poss indices neighbour-value-map) 
               (list pos (sort #(< (first %1) (first %2)) (filter #(not= 0 (first %)) 
                    (map #(list % (neighbour-value-map %)) (keys neighbour-value-map)))))))
        dependencies (map single-dependence-mapper singles)
        ]   
    (print-sudoku array)
    (println "array" array)
    (println "pbls" pbls)
    (println "singles" singles)
    (println "dependencies" dependencies)                  
    ))

(def random (java.util.Random.))

(defn myshuffle
  "Return a random permutation of coll"
  {:added "1.2"}
  [coll]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al random)
    (clojure.lang.RT/vector (.toArray al))))


(comment
  (println (last (first (first (first way)))))
  (let [waylist (way 0)
        posslist (map #(let [c (count (last (first %)))] (* c c)) waylist)
        ]
    (println posslist)
    (reduce + posslist)))


(defn improve [array]
  (let [dim (dim-by-array array)
        places (vec (myshuffle (remove #(= (last %1) 0) (map-indexed #(list %1 %2) array))))         
        ]   
    ;(println "improve by clear" array)
    ;(println "in places: " places)
    (loop [index 0 a array level -1]
      (if (>= index (count places))
        (list a level)
	      (let [pair (places index) 
	            testarray (assoc a (first pair) 0)
	            res (single-solution-by-poss dim testarray)
              newlevel (eval-way (res :ways))]
	        (if (and (= 1 (count (res :results))) (<= level newlevel)) ; (res :maxlevel) (res :level))))
             (do 
               ;(println  "way" (res :ways))
               ;(println (thread-name) "in level" level "->" (res :level) "improved to " testarray  )
               (when (> newlevel 50) (insert-or-update-puzzle-2 (res :result) testarray newlevel))
	             (recur (inc index) testarray newlevel)) ; (* (res :maxlevel) (res :level))))
	           (recur (inc index) a level)))))))

(defn improve-times [times array ]
  (ffirst (sort #(> (last %1) (last %2)) (repeatedly times (fn [] (improve array))))))

(myshuffle [1 2 3 4])
;(def al (java.util.ArrayList. [1 2 3]))
;(java.util.Collections/shuffle al)
;(clojure.lang.RT/vector (.toArray (java.util.Collections/shuffle al)))

(defn thread-name [] (.getName (. Thread currentThread)))


(defn improve-seconds [duration array]
  (let [start-time (. System currentTimeMillis)
        end-time (+ start-time (* duration 1000))]
    (loop [res (list nil 0)]
      (if (> (. System currentTimeMillis) end-time)
        (first res)
        (let [improved (improve array)  ; improve works 
              improved-complexity (pbls-complexity (first improved))
              new-res (list improved improved-complexity)]
          ;(println "improved: " improved)
          (recur 
            (cond (= nil (first res)) new-res
                  (< (last (first res)) (last improved)) 
	                  (do
	                    (println (thread-name) "in level:" (last improved) "improved to" (first improved))
                      ;(insert-or-update-puzzle (first improved))
	                    new-res)
                   (and (= (last (first res)) (last improved)) (< (last res) improved-complexity))
	                   (do
		                    (println (thread-name) "in level and compl:" (last improved) "improved to" (first improved))
                        ;(insert-or-update-puzzle (first improved))
		                    new-res)                                                         
                  :else res)))))))
                 

;(improve-seconds 10 (examples-3d 8))
(defn improve-parallel [threadnum seconds array]
  (println "start improving" array)
  (let [res (pmap 
              (fn [a] (improve-seconds seconds a)) 
                (repeat threadnum array))]
    (ffirst (sort #(> (last %1) (last %2)) res))))

;(improve-times (examples-3d 8) 2)   

;(improve-times 10 [5 0 0 0 0 1 0 2 0 0 0 0 0 0 8 0 6 0 3 0 1 7 0 0 4 5 9 0 4 5 8 0 3 6 0 0 0 0 6 9 0 0 0 1 0 0 0 0 0 0 0 8 0 0 7 0 6 9 8 2 0 0 0 0 4 0 7 0 3 0 0 0 9 0 8 6 0 0 0 0 0])

(defn find-single-sets [array]
  (let 
    [dim (dim-by-array array)
     res (reduce 
          #(if (= 0 (array %2))
             %1
             (let [poss (single-pos-check dim %2 (assoc array %2 0))]
             (println poss (last poss))
             (if (= 1 (count (last poss))) 
               (union %1 #{%2}) 
               %1))) #{}
          (take (count array) (iterate inc 0)))]
    res))

;(find-single-sets test-2dim-array2)
    
;(map #(find-single-sets %) examples-3d)

;(map #(improve %) examples-3d)

(defn gen-compl-set [dim]
  (let [num (* dim dim dim dim)
        allset (set (take num (iterate inc 0)))]
    (loop [actres allset]
      (let [pos (rand-int num)]))))

(defn make-new-array [sudoku-array res]
  (println "make-new-array" (res :ways))
  (let [ways (res :ways)
        way1 (first ways)
        wayn (last ways)
        wayx (remove 
               #(= %1 :equal) 
               (map #(if (and (= (last %1) (last %2)) (= (ffirst %1) (ffirst %2))) :equal %2) way1 wayn))]
    (println "wayx" wayx)
    ;(reduce #(assoc %1 (ffirst %2) ((first (nfirst %2)) (last %2))) sudoku-array wayx)
    (assoc sudoku-array (ffirst (first wayx)) ((first (nfirst (first wayx))) (last (first wayx)))
    )))
    
    
    

(defn create-sudoku [sudoku-array]
  (let [dim (dim-by-array sudoku-array)]
    (loop [resarray sudoku-array]
      (println "looping" resarray)
      (let [res (solve-by-poss dim resarray) ; (struct solveres false nil -1 []) 1)
            results (res :results)
            c (count results)
            ]
        ;(println "currentarr" resarray "resultnum: " c) 
        (cond (= 1 c) resarray
              (< 1 c) (recur (make-new-array resarray res))
              (< 1000 c) 
              (let [chosen-res (results (rand-int c))
                    pbls (create-pbls dim resarray)
                    poss (pbls (rand-int (count pbls)))
                    pos (first poss)]
                (recur (assoc resarray pos (chosen-res pos)))) 
              (= 0 c)
                  (let [ex-entries (filter (fn [s] (not (= 0 (first s)))) (partition 2 (interleave resarray 
                                                                   (iterate inc 0))))
                        chosen-entry (nth ex-entries (rand-int (count ex-entries)))]
                    (println "ex-entries: " ex-entries)
                    (println "clearing: " chosen-entry)
                   (recur (assoc resarray (fnext chosen-entry) 0))))))))



(defn rand-fill [dim num sudoku-array]
  ;(println "rand-fill" dim num sudoku-array)
  (loop [i 0 array sudoku-array]
    ; (println "i" i "array" array)
    (if (= i num)
      array
	  (let [poss (create-pbls dim array)
	        chosenpos (poss (rand-int (count poss)))
	        chosenval ((last chosenpos) (rand-int (count (last chosenpos))))]
	          (recur (inc i)(assoc array (first chosenpos) chosenval))))))
           

(defn auto-gen1 [threadnum dim seconds]
  (try 
	  (let [size (* dim dim dim dim)
	        first-size (inc (rand-int (dec (* dim dim dim))))        
	        first-array (rand-fill dim first-size (vec (take size (iterate #(+ 0 %) 0))))
	        ;first-sudoku (create-sudoku first-array)
	        solution (sudoku first-array)]
	    (if (not= nil solution)
	      (let [improved (improve-parallel threadnum seconds (solution :result))
	            improvedsolution (sudoku improved)
	           ]
			    (println "first-array" first-array)
			    ;(println "first-sudoku" first-sudoku)
			    ;(println "solution" solution "complexity" (eval-way (solution :ways)))
			    (println improved)
			    (println improvedsolution "complexity" (eval-way (solution :ways)))
	      ))
	    
	  )
   (catch Exception e)
  ))

;(println (auto-gen1 3 3 20))





        

