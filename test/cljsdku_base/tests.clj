(ns cljsdku-base.tests
  (:use [clojure.test])  
  (:use cljsdku-base.tools)
  (:use cljsdku-base.print)
  (:use cljsdku-base.test-data)
  (:use cljsdku-base.pbls)
  (:use cljsdku-base.checks)
  (:use cljsdku-base.solver))


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


(deftest test-block-indexes
  (is (= [0 1 2 3 4 5 6 7 8] (block-indexes 3 0)))
  (is (= [72 73 74 75 76 77 78 79 80] (block-indexes 3 8))))

(deftest test-col-indexes
  (is (= [0 3 6 27 30 33 54 57 60] (col-indexes 3 0)))
  (is (= [20 23 26 47 50 53 74 77 80] (col-indexes 3 8))))

(deftest test-row-indexes
  (is (= [0 1 2 9 10 11 18 19 20] (row-indexes 3 0)))
  (is (= [60 61 62 69 70 71 78 79 80] (row-indexes 3 8))))

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

(deftest test-coord-to-index
  (is (= (index-2-col 3 (coord-2-index 3 0 0)) 0)) 
  (is (= (index-2-col 3 (coord-2-index 3 1 0)) 1)) 
  (is (= (index-2-col 3 (coord-2-index 3 2 0)) 2)) 
  (is (= (index-2-col 3 (coord-2-index 3 8 8)) 8)) 
  (is (= (index-2-col 3 (coord-2-index 3 7 7)) 7)) 
  (is (= (index-2-col 3 (coord-2-index 3 7 0)) 7)) 
  (is (= (index-2-col 3 (coord-2-index 3 5 0)) 5)) 
  (is (= (index-2-col 3 (coord-2-index 3 5 8)) 5)) 
  (is (= (index-2-col 3 (coord-2-index 3 7 8)) 7)) 
  (is (= (index-2-row 3 (coord-2-index 3 0 0)) 0)) 
  (is (= (index-2-row 3 (coord-2-index 3 1 0)) 0)) 
  (is (= (index-2-row 3 (coord-2-index 3 2 0)) 0)) 
  (is (= (index-2-row 3 (coord-2-index 3 8 8)) 8)) 
  (is (= (index-2-row 3 (coord-2-index 3 7 7)) 7)) 
  (is (= (index-2-row 3 (coord-2-index 3 7 0)) 0)) 
  (is (= (index-2-row 3 (coord-2-index 3 5 0)) 0)) 
  (is (= (index-2-row 3 (coord-2-index 3 5 8)) 8)) 
  (is (= (index-2-row 3 (coord-2-index 3 7 8)) 8)) 
  (is (= (index-2-row 3 (coord-2-index 3 0 8)) 8)) 
  )
 

(deftest test-single-pos-check
  (is (= [4] (fnext (single-pos-check 2 12 test-2dim-array))))
  (is (= 12 (first (single-pos-check 2 12 test-2dim-array))))
  (is (= [3] (fnext (single-pos-check 2 13 test-2dim-array))))
  (is (= 13 (first (single-pos-check 2 13 test-2dim-array))))
  (is (= [2] (fnext (single-pos-check 2 14 test-2dim-array))))
  (is (= 14 (first (single-pos-check 2 14 test-2dim-array))))
  (is (= [1] (fnext (single-pos-check 2 15 test-2dim-array))))
  (is (= 15 (first (single-pos-check 2 15 test-2dim-array))))
  (is (= [] (single-pos-check 2 13 test-2dim-wrong-array)))
  (is (= [] (single-pos-check 2 11 test-2dim-array ))))




(deftest test-check-area
  (is (check-area 2 (row-indexes 2 3) 1 test-2dim-array4))
  (is (check-area 2 (row-indexes 2 3) 2 test-2dim-array4))
  (is (check-area 2 (row-indexes 2 2) 3 test-2dim-array4))
  (is (check-area 2 (row-indexes 2 2) 4 test-2dim-array4))
  (is (not (check-area 2 (row-indexes 2 3) 3 test-2dim-array4)))
  (is (not (check-area 2 (row-indexes 2 3) 4 test-2dim-array4)))
  (is (not (check-area 2 (row-indexes 2 2) 1 test-2dim-array4)))
  (is (not (check-area 2 (row-indexes 2 2) 2 test-2dim-array4)))
  (is (check-area 2 (col-indexes 2 3) 1 test-2dim-array4))
  (is (check-area 2 (col-indexes 2 3) 3 test-2dim-array4))
  (is (check-area 2 (col-indexes 2 2) 2 test-2dim-array4))
  (is (check-area 2 (col-indexes 2 2) 4 test-2dim-array4))
  (is (not (check-area 2 (col-indexes 2 3) 2 test-2dim-array4)))
  (is (not (check-area 2 (col-indexes 2 3) 4 test-2dim-array4)))
  (is (not (check-area 2 (col-indexes 2 2) 1 test-2dim-array4)))
  (is (not (check-area 2 (col-indexes 2 2) 3 test-2dim-array4)))
  (is (check-area 2 (block-indexes 2 3) 1 test-2dim-array4))
  (is (check-area 2 (block-indexes 2 3) 2 test-2dim-array4))
  (is (check-area 2 (block-indexes 2 3) 3 test-2dim-array4))
  (is (check-area 2 (block-indexes 2 3) 4 test-2dim-array4))
  (is (not (check-area 2 (block-indexes 2 2) 1 test-2dim-array4)))
  (is (not (check-area 2 (block-indexes 2 1) 2 test-2dim-array4)))
  (is (not (check-area 2 (block-indexes 2 2) 3 test-2dim-array4)))
  (is (not (check-area 2 (block-indexes 2 2) 4 test-2dim-array4))))




(deftest test-pos-check
  (let [res (create-pbls 2 test-2dim-array2)]
    (is (=  ['(14 [2]) '(15 [1]) '(4 [3 4]) '(5 [3 4]) '(12 [3 4]) '(13 [3 4])] res))))

(deftest test-pos-check
  (let [res (create-pbls 2 test-2dim-array4)]
    (is (= ['(12 [4]) '(13 [3]) '(14 [2]) '(15 [1])] res))))

(deftest check-sudoku-allow-zero-test
  (is (check-sudoku-allow-zero test-2dim-array4))
  (is (check-sudoku-allow-zero test-2dim-array2))
  (is (check-sudoku-allow-zero test-2dim-array3))
  (is (check-sudoku-allow-zero [1 2 4 3 3 4 1 2 2 1 3 4 4 3 2 1]))
  (is (not (check-sudoku-allow-zero [1 2 4 3 3 4 1 2 2 1 3 4 3 4 2 1]))))

(deftest mx-test2
  (let  [pbls (create-pbls 2 test-2dim-array2)
         mx (pbls-2-poss-mx 2 pbls)]
    (is ((mx 1) 14))
    (is ((mx 0) 15))
    (is ((mx 2) 4))
    (is ((mx 3) 4))
    (is ((mx 2) 5))
    (is ((mx 3) 5))
    (is ((mx 2) 12))
    (is ((mx 3) 12))
    (is ((mx 2) 13))
    (is ((mx 3) 13))
    ))

(deftest hidden-singles-test
  (is (= #{12 13 14 15} (mx-hidden-singles (pbls-2-poss-mx 2 (create-pbls test-2dim-array4)))))
  (is (= #{14 15} (mx-hidden-singles (pbls-2-poss-mx 2 (create-pbls test-2dim-array2)))))
  (is (= #{14 15} (mx-hidden-singles (pbls-2-poss-mx 2 (create-pbls test-2dim-array3)))))
  (is (= #{12 13 14 15} (mx-hidden-singles (pbls-2-poss-mx 2 (create-pbls test-2dim-array)))))
  )    
    
    


(run-tests  )  
