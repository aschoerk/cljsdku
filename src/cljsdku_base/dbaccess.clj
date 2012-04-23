(ns cljsdku-base.dbaccess
  (:use [cljsdku-base tools solver])
  (:require [clojure.java.jdbc :as sql])
   (:require [clojure.java.jdbc.internal :as sqli])
   (:import
    (clojure.lang RT)
    (java.net URI)
    (java.sql Timestamp Connection DriverManager PreparedStatement ResultSet SQLException Statement)
    (java.util Hashtable Map Properties)
    (javax.naming InitialContext Name)
    (javax.sql DataSource)))

(def postgres-db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost:5432/cljsdku"
                  :user "aschoerk"
                  :password "aschoerk"})

(def derby-db {:subprotocol "derby"
               :subname "clojure_test_derby"
               :classname "org.apache.derby.jdbc.EmbeddedDriver"
               :create false})

(defn create-sdku-table []
  (sql/with-connection derby-db
	  (clojure.java.jdbc/create-table 
	    :sdku 
	    [:dim :int]
	    [:createtime :timestamp]
	    [:updatetime :timestamp]
	    [:puzzle "varchar(1024)"]
      [:solution "varchar(1024)"]
	    [:complexity :int]
	    [:md5 "varchar(32)"]
	    )))

(defn drop-sdku-table []
  (sql/with-connection derby-db
	  (clojure.java.jdbc/drop-table 
	    :sdku 
	    )))



(defn create-user-table []
  (clojure.java.jdbc/create-table 
    :user 
    [:id :int]
    [:createtime :timestamp]  
    [:updatetime :timestamp]
    ))

(defn drop-user-table []
  (sql/with-connection derby-db
	  (clojure.java.jdbc/drop-table 
	    :user 
	    )))


(defn insert-or-update-puzzle-2 [solution sudoku-array complexity]
   (let [puzzleval (str sudoku-array) 
         dim (dim-by-array sudoku-array)
         solutionval (str solution)]
		  (sql/with-connection derby-db
		     (sql/with-query-results res [{:fetch-size 1000} 
		       (str "select * from sdku where puzzle = '" puzzleval "'")]
		         (sql/transaction
		           (if (> (count res) 0)
		             (sql/update-values
		               :sdku
		               ["puzzle=?" puzzleval]
		               {:solution solutionval :complexity complexity :dim dim :updatetime (java.sql.Timestamp. (System/currentTimeMillis))})
		             (sql/insert-record 
		               :sdku 
		               {:solution solutionval :complexity complexity :puzzle puzzleval 
                    :dim dim :createtime (java.sql.Timestamp. (System/currentTimeMillis))}
                 )))))))
		     

(defn insert-or-update-puzzle [sudoku-array]
    (let [puzzleval (str sudoku-array) 
          dim (dim-by-array sudoku-array)
          solvres (sudoku sudoku-array)
          solution (str (solvres :result))
          complexity (eval-way (solvres :ways))
          ]
      (insert-or-update-puzzle-2 solution puzzleval complexity)))

(defn read-all []
  (sql/with-connection derby-db
      (.setAutoCommit (sql/connection) true)
      (try 
        (sql/with-query-results res ["select * from sdku"]
          (doall res))             
        (finally (.setAutoCommit (sql/connection) true)))))



;(create-user-table)
;(create-sdku-table)

; (insert-or-update-puzzle [0 0 0 0 0 7 0 0 0 0 0 0 0 8 0 2 0 0 0 0 0 0 0 0 5 4 6 0 0 0 0 0 0 0 9 0 0 0 6 0 0 0 0 0 0 8 0 7 4 0 2 0 1 0 0 0 3 0 4 0 0 6 5 0 0 8 9 0 0 0 7 2 0 0 0 0 5 0 0 3 0])
; (read-all)         
