(ns cljsdku.tests
   (:use cljsdku.tools)
  (:use cljsdku.print)
  (:use cljsdku.test-data)
  (:use cljsdku.solver))




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
  (let [res (pos-check 2 test-2dim-array2)]
    (is (=  ['(14 [2]) '(15 [1]) '(4 [3 4]) '(5 [3 4]) '(12 [3 4]) '(13 [3 4])] res))))

(deftest test-pos-check
  (let [res (pos-check 2 test-2dim-array4)]
    (is (= ['(12 [4]) '(13 [3]) '(14 [2]) '(15 [1])] res))))

(deftest check-sudoku-allow-zero-test
  (is (check-sudoku-allow-zero test-2dim-array4))
  (is (check-sudoku-allow-zero test-2dim-array2))
  (is (check-sudoku-allow-zero test-2dim-array3))
  (is (check-sudoku-allow-zero [1 2 4 3 3 4 1 2 2 1 3 4 4 3 2 1]))
  (is (not (check-sudoku-allow-zero [1 2 4 3 3 4 1 2 2 1 3 4 3 4 2 1]))))
   
(run-tests  )  
