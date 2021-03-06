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

(defn improve2 [array initiallevel initialtested]
  ; (println "improve2" array)
  (let [dim (dim-by-array array)
        places (vec (myshuffle (remove #(= (last %1) 0) (map-indexed #(list %1 %2) array))))         
        ]   
    ;(println "improve by clear" array)
    ;(println "in places: " places)
    (loop [index 0 a array level initiallevel tested initialtested]
      (if (>= index (count places))
        (if (>= level initiallevel) (list a level tested) (list nil nil tested))
	      (let [pair (places index) 
	            testarray (assoc a (first pair) 0)
              restested (conj tested testarray)]
           (if (not (contains? tested testarray))             
	           (let [res (single-solution-by-poss dim testarray)
	                 newlevel (eval-way (res :ways))]
				        (if (and (= 1 (count (res :results))) (<= level newlevel)) ; (res :maxlevel) (res :level))))
			             (do 
			               (if (> newlevel level)
                       (do 
			                   (println "better at level: " newlevel testarray)
                         (println "linear" (transl-to-linear testarray))))
				             (improve2 testarray newlevel restested)) ; (* (res :maxlevel) (res :level))))
				           (recur (inc index) a level restested)))
             (recur (inc index) a level restested))
           )))))

(defn reduceimprove [i1 i2 callres]
  ;(println "reduceimprove: " (count (last callres)))
  (let [restested (union (last i1) (last callres))]
    (if (not= nil (first callres)) 
      (do
        ;(println "join" (first callres) (fnext callres))
        (list (conj (first i1) (list (first callres) (fnext callres))) restested))
        (list (first i1) restested))))

(defn improve-by-solution [array chgnum improve2runs initialtested]
  (let [dim (dim-by-array array)
        sol (single-solution-by-poss dim array)
        solution (sol :result)
        initiallevel (eval-way (sol :ways))
        filledplaces (vec (myshuffle (remove #(not= (last %1) 0) (map-indexed #(list %1 %2) array))))
        ]   
    ;(println "         initial level" initiallevel)
    ;(println "         solution" solution)
    (loop [index 0 a array level initiallevel tested initialtested]
      ;(println "filled places" filledplaces)
      (if (> index (- (count filledplaces) chgnum))
        (list a level tested)
	      (let [ 
              indices (range index (+ index chgnum))
              testarray (reduce #(let [pair (filledplaces %2)] 
                                   ;(println "pair" pair) ; (first pair) (solution (first pair))) 
                                   (assoc %1 (first pair) (solution (first pair)))) 
                                array indices) 
	            results (reduce #(reduceimprove %1 %2 (improve2 testarray level (last %1))) 
                                                    (list #{} tested) (take improve2runs (iterate inc 0)))
              rescount (count (first results))
              best   (reduce #(if (> (last %2) (last %1)) %2 %1) (list a level) (first results))
	            ]          
           (if (> (last best) level)
             (println "better" best))
           (recur (inc index) (first best) (last best) (last results))
      )))
    ))

(defn cycle-improve [duration array]
  (let [start-time (. System currentTimeMillis)
        end-time (+ start-time (* duration 1000))]
    (loop [res (list array 0 #{})]
      (if (> (. System currentTimeMillis) end-time)
        (first res)

        (let [chgnum (inc (rand-int 20))
              improve2runs (inc (rand-int 100))]
	        (println (java.util.Date.) "chgnum" chgnum "improve2runs" improve2runs)
	        (println  "         using" (fnext res) (first res))
	        (println  "         donecount" (count (last res)))
	        (let [improved (improve-by-solution (first res) chgnum improve2runs (last res))]
	          (if (> (fnext improved) (fnext res))
	            (recur improved)
	            (recur res))))))))
        
  




(println "ready doing creation")

; (improve-by-solution [] [] 20)

;(println (improve-by-solution 
; [0 0 4 0 0 6 3 0 0 0 0 5 0 1 0 0 0 0 0 0 8 0 0 0 0 0 9 0 0 0 0 0 0 0 2 3 7 0 0 0 0 3 0 5 0 2 0 0 0 0 7 0 0 1 0 0 0 5 0 0 0 0 9 0 4 0 0 7 1 3 0 0 0 0 0 0 8 0 1 0 4]
; [2 1 4 9 5 6 3 8 7 9 3 5 8 1 7 2 6 4 6 7 8 3 4 2 5 1 9 4 6 1 8 9 5 7 2 3 7 9 8 1 2 3 4 5 6 2 3 5 4 6 7 8 9 1 1 3 8 5 4 2 6 7 9 5 4 9 6 7 1 3 8 2 7 2 6 9 8 3 1 5 4]
; 91))

(println (cycle-improve 10000
;[0 0 0 0 0 3 0 0 0 0 0 7 0 2 1 0 0 9 5 0 8 0 0 0 0 0 0 0 0 0 1 0 0 8 2 0 0 0 0 6 0 0 7 0 4 0 0 9 8 0 0 0 3 0 2 8 0 7 0 4 3 0 0 0 0 0 1 3 0 0 0 8 0 0 0 0 0 0 0 6 0]
;[0 0 0 0 0 3 0 0 0 0 0 7 0 2 1 0 0 9 5 0 8 0 0 0 0 0 0 0 0 0 1 0 0 8 2 0 0 0 0 6 0 0 7 0 0 0 0 9 8 0 0 0 3 0 2 8 0 7 0 4 3 0 0 0 0 0 1 3 0 0 0 8 0 0 0 0 0 2 0 6 0]
;[0 0 0 0 0 3 0 0 0 0 0 0 0 0 1 0 5 9 5 0 8 0 0 0 0 0 0 0 0 0 1 0 0 8 2 0 0 0 0 6 0 0 7 0 4 0 0 9 8 0 0 0 3 0 2 8 0 7 0 0 3 0 5 0 0 0 0 3 0 2 0 8 0 0 0 0 0 2 0 0 0]
;[0 0 0 4 0 7 0 0 0 0 0 5 0 8 0 2 0 0 0 0 9 0 0 0 0 0 6 2 0 0 0 0 0 0 9 0 0 0 6 0 0 0 0 0 0 8 0 7 4 0 0 0 1 0 0 0 3 0 4 0 0 6 5 0 6 8 0 0 0 0 7 2 0 0 0 0 5 0 0 0 0]
;[0 0 0 4 0 7 0 0 0 0 0 5 0 8 0 2 0 0 0 0 9 0 0 0 0 0 6 2 0 0 0 0 0 0 9 0 0 0 6 0 0 0 0 0 0 8 0 7 4 0 0 0 1 0 0 0 3 0 4 0 0 6 5 0 6 8 0 0 0 0 7 2 0 0 0 0 5 0 9 0 0]
;[0 0 0 0 0 7 0 0 0 0 0 0 0 8 0 2 0 0 0 0 0 0 0 0 5 4 6 0 0 0 0 0 0 0 9 0 0 0 6 0 0 0 0 0 0 8 0 7 4 0 2 0 1 0 0 0 3 0 4 0 0 6 5 0 0 8 9 0 0 0 7 2 0 0 0 0 5 0 0 3 0]
;[0 0 4 0 0 6 3 0 0 0 0 5 0 1 0 0 0 0 0 0 8 0 0 0 0 0 9 0 0 0 0 0 0 0 2 0 7 0 0 0 0 3 0 5 0 2 3 0 0 0 7 0 0 1 0 0 0 5 4 2 0 0 9 0 4 0 0 7 1 3 0 0 0 0 0 0 8 3 1 0 0]
;[0 0 4 0 0 6 3 0 0 0 0 5 0 1 0 0 0 0 0 0 8 0 0 0 0 0 9 0 0 0 0 0 0 0 2 0 7 0 0 0 0 3 0 5 0 2 3 0 0 0 7 0 0 1 0 0 0 5 4 0 0 0 9 0 4 0 0 7 1 3 0 0 0 0 0 0 8 3 1 0 0]
;[0 0 4 0 0 6 3 0 0 0 0 5 0 1 0 0 0 0 0 0 8 0 0 0 0 0 9 0 0 0 0 0 0 0 2 0 7 0 0 0 0 3 0 5 0 2 3 0 0 0 7 0 0 1 0 0 0 5 0 0 0 0 9 0 4 0 0 7 1 3 0 0 0 0 0 0 8 3 1 0 0]
;[0 0 4 0 0 6 3 0 0 0 0 5 0 1 0 0 0 0 0 0 8 0 0 0 0 0 9 0 0 0 0 0 0 0 2 3 7 0 0 0 0 3 0 5 0 2 0 0 0 0 7 0 0 1 0 0 0 5 0 0 0 0 9 0 4 0 0 7 1 3 0 0 0 0 0 0 8 3 1 0 0]
))


