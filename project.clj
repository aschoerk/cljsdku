(defproject org.clojars.aschoerk/cljsdku "1.0.0-SNAPSHOT"
  :description "Yet another sudoku in clojure"
  :dependencies [[org.clojure/clojure "1.2.0"]          
                 [seesaw "1.2.2"]]
  :dev-dependencies [[org.clojure/clojure-contrib "1.2.0"][lein-eclipse "1.0.0"]]
  :main cljsdku.seesaw
  :keep-non-project-classes true
  :disable-implicit-clean true)
