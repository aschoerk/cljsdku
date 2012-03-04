(ns cljsdku.print
  (:use cljsdku.tools))


(defn print-sudoku-line [dim row-indexes sudokuarray] 
  (loop [col 0 endcol (* dim dim)]
    (if (< col endcol)
      (do 
        (print (sudokuarray (row-indexes col)) " ")
        (recur (inc col) endcol)))))


(defn print-sudoku 
  ([sudoku-array] (print-sudoku (dim-by-array sudoku-array) sudoku-array))
  ([dim sudokuarray]   
	  (loop [row 0 endrow (dec (* dim dim))]
	    (print-sudoku-line dim (row-indexes_c dim row) sudokuarray)
	    (println)
	    (if (< row endrow)
	      (recur (inc row) endrow))
	    )
	  (println "")))

(defn print-pbls [dim pbls]
  (println "pbls: " pbls)
  (if (<= 1 (count pbls))
	  (loop [index 0]
     (println "pblsindex" index)
	    (let [ix (first (pbls index)) row (index-2-row dim ix) col (index-2-col dim ix)]
	      (print "|" (inc row) "/" (inc col) ": " (last (pbls index)))
	      (if (< (inc index) (count pbls))
	        (recur (inc index)))))
    (println)))

