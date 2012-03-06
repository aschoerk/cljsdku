(defproject org.clojars.aschoerk/cljsdku "1.0.1-SNAPSHOT"
  :description "Sudoku in clojure, in the beginning just small project used for learning functional and parallel programming, now an ugly user-interface but capable to create really fiendish sudoku-puzzles, if you invest enough time and processors."
  :dependencies [[org.clojure/clojure "1.2.0"]          
                 [seesaw "1.2.2"]]
  :dev-dependencies [[org.clojure/clojure-contrib "1.2.0"][lein-eclipse "1.0.0"]]
  :main cljsdku.seesaw
  :keep-non-project-classes true
  :disable-implicit-clean true)
