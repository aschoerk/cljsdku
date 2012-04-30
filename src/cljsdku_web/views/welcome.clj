(ns cljsdku-web.views.welcome
  (:require [cljsdku-web.views.common :as common]
            [noir.content.getting-started]
            [clojure.string :as cl-str])
  (:use [noir.core :only [defpage render]]
        [hiccup.core :only [html]]
        );[clojure.string :as cl-str :only [split replace]])
  (:use hiccup.page-helpers hiccup.form-helpers)
  (:use cljsdku-base.tools)
  (:use cljsdku-base.test-data)
  (:use [cljsdku-base pbls solver generator]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to cljsdku-web"]))



(defn blocks-row-from-sudoku [ix-start dim calc-element]
  (loop [x 0 res-row [:tr]]
    (if (= x dim)
	      res-row
	      (let [ix (+ ix-start x)              
             ]
           (recur 
             (inc x)
             (conj res-row (calc-element ix)
               ))))))

(defn block-from-sudoku [blockx blocky dim calc-element]
  (let [qdim (* dim dim ) 
        ix-start (+ (* blocky dim qdim) (* blockx qdim))
        ] 
    (loop [y 0 res-block [:table {:border 0 :cellpadding "2" }]]
      (if (= dim y)
        res-block
        (let [block-row (blocks-row-from-sudoku (+ ix-start (* y dim)) dim calc-element)]
          (recur (inc y) (conj res-block block-row)))))))

(defn blocked-row-from-sudoku [blocky  dim calc-element]
  (loop [blockx 0 res-row [:tr]]
    (if (= dim blockx)
      res-row
      (let [block [:td (block-from-sudoku blockx blocky dim calc-element)]]
        (recur (inc blockx) (conj res-row block))))))

(defn blocked-table-from-sudoku [dim calc-element] 
    (loop [blocky 0 res-table [:table {:id "sudokutable" :border "1" :cellpadding "1" :cellspacing "1"}]]
      (if (= dim blocky)
        res-table
      (let [row (blocked-row-from-sudoku blocky dim calc-element)]
        (recur (inc blocky) (conj res-table row))))))
        


(defn examplParam [params]  
  (let [val (params :example)] 
     (if (= val nil) (rand-int (count  examples-3d)) (. Integer parseInt val))))

(defstruct paramstruct :sudokuarray :solve? :hints? :currentexample :solvres :check :clear :import :importfield)


(defn array-from-params [params]
  (vec (map #(try (. Integer parseInt (% 1)) (catch Exception e 0)) 
  (sort #(< (%1 0) (%2 0))
  (map #(vec (list (. Integer parseInt (subs (str (% 0)) 2)) (% 1)))
    (filter #(= ":@" (subs (str (% 0)) 0 2)) params))))))

(defn array-from-importfield [params]
  (println "importfield" (params :importfield))
  (let [importfield (cl-str/replace (params :importfield) #"[^0-9]" #(subs %1 1 1))
        entries (cl-str/split (cl-str/replace importfield #"[0-9]" #(str %1 " ")) #" ")
    res (transl-from-linear (vec (map #(. Integer parseInt %) (filter #(re-matches #"[0-9]+" %) entries))))]
    (println "import nonlinear: " res)
    res
  )) 

(defn encode-array [array] 
  (if (coll? array) 
    (reduce #(str %1 "a" %2) (map #(str %) array)) 
    (println "could not encode: " array)))

(defn decode-array [astr]
  (let [res (if (not= nil astr)
               (map #(. Integer parseInt %) (cl-str/split astr #"a"))
               nil)]    
    (vec res)))



(defn interpret-params [params]
  (let [examplep (examplParam params)
        example 
          (cond 
            (and (params :next) (< examplep (dec (count examples-3d)))) (inc examplep)
            (and (params :prev) (> examplep 0)) (dec examplep)
            :else examplep)
        loadexample? (and (not (or (params :check) (params :import) (params :hints) (params :solver) (params :clear)))
                          (or (params :next) (params :prev) (not (params :example))))
        havesolution? (and (not loadexample?) (params :solution) (not (params :clear)) (not (params :check)) (not (params :import)))
        sudoku-array (if loadexample? 
                       (nth examples-3d example)
                       (if (params :import)
                         (try (array-from-importfield params) (catch Exception e (array-from-params params))) 
                         (array-from-params params)))          
        ] 
    (print "sudoku-array" sudoku-array "loadexample?" loadexample?)
    (let [res 
        (struct paramstruct 
          sudoku-array 
          (not= nil (params :solver))
          (not= nil (params :hints))
          example
          (if (or loadexample? (not (params :solution)) (not havesolution?)) 
            (let [res (sudoku sudoku-array)] (vec (list (res :result) (eval-way (res :ways))))) 
            (vec (list (decode-array (params :solution)) (params :ways))))
          (not= nil (params :check))
          (not= nil (params :clear))
          (not= nil (params :import))
          (params :importfield))]  res)))
          

,


;(defpage "/start-page"
;  (render "/my-page" {:example 2}))
                       
(defpage [:post "/handler"] {:as params}
  ;(println params)
  ;(println (interpret-params params))
  (render "/my-page" params))

     
(defpage [:get "/handler"] {:as params}
  ;(println params)
  ;(println (interpret-params params))
  (render "/my-page"  params))

(defn calc-new-array [paramstruct]
  (println "calc-new-array" paramstruct)
  (cond
    (paramstruct :solve?) ((sudoku (paramstruct :sudokuarray)) :result)
    ;(paramstruct :hints?) (paramstruct :sudokuarray)        
    :else (paramstruct :sudokuarray))) 

(defn create-calc-normal-element [sudoku-array check-sol solution clear]
  (fn [ix]
    (if clear
      [:td [:input {:type "text" :name (str "@" ix) :size "1" :onchange "sdkuchg2(this)"}]]
    (let [val (sudoku-array ix)
          attrs0 {:type "text" :name (str "@" ix) 
                   :size "1"}
          inputattrs (if (= 0 val)
                 (assoc attrs0 :onchange "sdkuchg(this)")
                 (assoc (assoc attrs0 :readonly "") :value (str val)))   
          tdattrs0 {}
          tdattrs (if (and check-sol (not= nil solution) (not= (solution ix) val)) 
               tdattrs0 ; (assoc tdattrs0 :class "emptycell")
               tdattrs0)
          element [:td tdattrs [:input inputattrs]] 
         ]
      element))))


(defpage [:get "/my-page"] {:as params}  
  (println "my-page" params)
  (let [pinfo (interpret-params params)
        array (calc-new-array pinfo)
        dim (dim-by-array array)
        checksol true
        solution ((pinfo :solvres) 0)
        qdim (* dim dim)
        ]
	  (common/site-layout    
	    [:h1 "Welcome to Sudoku!"]
      [:style {:type "text/css"}
       "body { background-color:#CCCCCC;
               margin-left:100px; }
        * { color:blue; }
        h1 { font-size:300%;
             color:#FF0000;
             font-style:italic;
             border-bottom:solid thin black; }
        p,li  { font-size:110%;
             line-height:140%;
             font-family:Helvetica,Arial,sans-serif;
             letter-spacing:0.1em;
             word-spacing:0.3em; }
        #table input { color:black; background-color:#FFFFCC; font-style:normal; size:1}
        #table .wrong { color:red; background-color:#FFCCCC; }
        .emptycell { background-color:#FFFFFF; }
        .ok { color:blue; background-color:#CCCCCC; }
        #sudokutable { margin-left:50px}
        .buttons { color:grey; background-color:grey; }

        #navi { float:left; margin: 0 0 1em 1em; padding: 0 }
        #table { float:right }
"
       ]
      [:script {:type "text/javascript"}
       (str "function sdkuchg (obj) {
          // alert(\"extracted: \" + document.sudokuform.solution.value.split(\"a\")[obj.name.substring(1)]);
          if (0 > obj.value || " qdim " < obj.value || obj.value != document.sudokuform.solution.value.split(\"a\")[obj.name.substring(1)])
            {// alert(\"wrong input: \" + obj.value);
            obj.className = \"wrong\";}
          else obj.className=\"\";            
        }\n")
       
       (str "function sdkuchg2 (obj) {
          if (0 > obj.value || " qdim " < obj.value)
            {// alert(\"wrong input: \" + obj.value);
            obj.className = \"wrong\";}
          else obj.className=\"\";            
        }\n")
       ]
	    [:form {:method "post" :value "my-page" :action "/handler" :name "sudokuform"}       

        [:table 
         [:tr
          [:td
         [:div {:id "table"}
           (blocked-table-from-sudoku dim 
             (create-calc-normal-element array true ((pinfo :solvres) 0) (pinfo :clear)))]]
           (println "encodearray solution" (encode-array ((pinfo :solvres) 0)) "solution: " (pinfo :solvres))
         [:td
         [:div {:id "navi"}
		      [:input {:type "submit" :color "grey" :name "check" :value "check"} ] [:br]      
			    [:input {:type "submit" :name "clear" :value "clear"} ] [:br]       
			    [:input {:type "submit" :name "hints" :value "hints"} ] [:br]
	        [:input {:type "submit" :name "solver" :value "solve"} ] [:br]
          [:br 
	         [:input {:type "submit" :name "next" :value "next"} ][:br]
		       [:input {:type "submit" :name "prev" :value "prev"} ][:br]
	         "Example: " [:input 
	           {:type "text" :name "example" :value (str (pinfo :currentexample)) :size "3"}
	           ] [:br]
          [:p "Complexity: " (str ((pinfo :solvres) 1))]]
         ]]]
        ]
	      [:input {:type "hidden" :name "solution" :value (encode-array ((pinfo :solvres) 0))}]
		    [:input {:type "hidden" :name "ways" :value ((pinfo :solvres) 1)}]
	      [:br 
         "Linear: " [:input 
           {:type "text" :name "importfield" :value (str (transl-to-linear array)) :size "100"}
           ]
         [:br]
         [:input {:type "submit" :name "import" :value "import"} ]]
	    ]
      (if (pinfo :hints?)
        (let [ hintinfo (create-pbls dim array)]
        [:p (str "\ncomplexity: " (cljsdku-base.pbls/pbls-complexity hintinfo) "/" (cljsdku-base.pbls/pbls-complexity2 hintinfo) "\n" 
                        (println-str (cljsdku-base.print/info-pbls dim hintinfo) 
	       ))]))
      )))
