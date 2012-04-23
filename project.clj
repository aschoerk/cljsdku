(defproject org.clojars.aschoerk/cljsdku "1.0.4-SNAPSHOT"
            :description "Clojure Sudoku Generator/Solver as Web-Application"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.2.1"]
			   [org.apache.derby/derby "10.8.2.2"]
                           [org.clojure/java.jdbc "0.1.3"]]
            :main cljsdku-web.server)

