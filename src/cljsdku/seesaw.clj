(ns cljsdku.seesaw
  (:require 
        [seesaw.bind :as bind]
        [clojure.string :as string]
        [cljsdku.solver] [cljsdku.tools] [cljsdku.pbls] [cljsdku.generator]
        [cljsdku.test-data])
  (:use [seesaw core border table mig scroll]))

(comment " 
(def active-requests (atom #{}))

; schedule data requests for each panel (:data-fn) and call UI update functions
; (:update-fn) with results, in the UI thread!
(defn refresh-action-handler [e] 
  (let [root (to-frame e)
        city (text (select root [:#city]))]
    (doseq [{:keys [name data-fn update-fn]} panel-behaviors]
      (future 
        (swap! active-requests conj name)
        (let [data (data-fn city)]
          (swap! active-requests disj name)
          (invoke-later
            (update-fn root data)))))))


(def refresh-action
  (action :name \"Refresh\" :key \"menu R\" :handler refresh-action-handler))


(defn add-behaviors 
  [root]
  ; As active requests change, update the status bar
  (bind/bind
    active-requests
    (bind/transform #(if (empty? %) \"Ready\" (str \"Refreshing: \" (string/join \", \" %))))
    (bind/property (select root [:#status]) :text))

  ; Use binding to map selection changes in the table to update the
  ; displayed image
  (bind/bind
    (bind/selection (select root [:#webcam-table]))
    (bind/transform 
      #(-> (select root [:#webcam-table])
         (value-at %) 
         :CURRENTIMAGEURL))
    (bind/property (select root [:#webcam-image]) :icon))
  
  ; Use refresh-action as the action for everything marked with class
  ; :refresh.
  (config! (select root [:.refresh]) :action refresh-action)

  root)
")


;(def last-sudoku-array nil)

(defn sudokuindex-by-panelindex [dim index]
  (let [rowlen (* dim dim) rownum (quot index rowlen) rowix (mod index rowlen)]
    ((cljsdku.tools/row-indexes_c dim rownum) rowix)))

(defn panelindex-by-sudokuindex [dim index]
  (let [rowlen (* dim dim) rownum (cljsdku.tools/index-2-row dim index) rowix (cljsdku.tools/index-2-col dim index)]
    (+ (* rownum rowlen) rowix)))

(defn make-grid2 [dim sudokuArray]
  (let [rowlen (* dim dim)
        panel-array 
    ((fn [index res]
      (if (>= index (count sudokuArray))
        res
        (recur 
          (inc index) 
          (conj res 
            (vec 
              (list 
                (text (str  (sudokuArray (sudokuindex-by-panelindex dim index)))) 
                (str "grow 10,gap 2 " 
                     (* 3 (if (= 2 (mod index dim)) 3 1))
                     " 2 " (* 2 (if (= 2 (mod (quot index (* dim dim)) dim)) 4 1))) )))))) 0 [])]        
    ; (* 5 (if (= 2 (mod dim (div index (* dim dim)))) 2 1))
                ;(str "cell " (* 2 (cljsdku.tools/index-2-col dim index)) " " (cljsdku.tools/index-2-row dim index) ",gapx 2"))))))) 0 [])]                 
  panel-array))





(defn fill-sudoku-array-by-input [dim text-array]
  (loop [index 0 res []]
    (if (>= index (* dim dim dim dim))
      (do (println res) res)
    (let [text-field (text-array (panelindex-by-sudokuindex dim index))
         val (. Integer parseInt (.getText (text-field 0)))] 
      (recur (inc index) (conj res val))))))
      
(defn clear-text-array  [panel-array]
  (loop [panel-index 0]
    (if (>= panel-index (count panel-array))
      true
    (do
      (.setText ((panel-array panel-index) 0) (str 0))
      (recur (inc panel-index))))))

(defn fill-separators [dim panel-array]
  panel-array)

(def last-sudoku-array (ref nil))

(defn string-to-int-array [s]
  (println s)
  (try (vec (map (fn [s] (. Integer parseInt s)) (re-seq (re-pattern "[0-9]+") s))) (catch Exception e nil) ))

(defn make-mig []
  (let [dim 3
        text-array (make-grid2 dim (nth cljsdku.test-data/examples-3d 0))
        panel-array (fill-separators dim text-array)
        text-field (text "0")
        time-field (text "10")
        thread-field (text "2")
        complexity-field (text :text "-1" :editable? false)
        complexity-field2 (text :text "-1" :editable? false)
        
        grid-field (text :text "[]" :id :grid-text)
        error-field (text :text "\n" :multi-line? true :editable? false :wrap-lines? true :rows 30)
        s (scrollable error-field)
        get-index (fn [] (. Integer parseInt (.getText text-field)))
        set-index (fn [newindex] (.setText text-field (str newindex)))     
        get-time (fn [] (. Integer parseInt (.getText time-field)))
        get-grid (fn [] (. Integer parseInt (.getText grid-field)))
        get-threads (fn [] (. Integer parseInt (.getText thread-field)))
        set-time (fn [newtime] (.setText time-field (str newtime)))        
        
        append-error-text (fn [text] (.setText error-field (str (.getText error-field) text)))
        append-text (fn [text] (.setText error-field (str (.getText error-field) text)))
        fill-panel-array 
          (fn [sudokuArray]
            (.setText complexity-field 
              (str (cljsdku.pbls/pbls-complexity dim sudokuArray)))
            (.setText complexity-field2 
              (str (cljsdku.pbls/pbls-complexity2 dim sudokuArray)))
            (loop [panel-index 0]
              (if (>= panel-index (count text-array))
                true
                (do
                  (.setText ((text-array panel-index) 0) 
                    (str (sudokuArray (sudokuindex-by-panelindex dim panel-index))))
                  (recur (inc panel-index))))))
        check-res-fill-panel (fn [res orgarray duration] 
          (let [array (res :result) 
                level (res :level) 
                maxlevel (res :maxlevel) 
                results (res :results)
                cr (if (= nil results) 0 (count (res :results)))]
	          (if (not (res :found?))
	            (append-error-text "\n not solved")
              (do
	              (fill-panel-array array)
                (append-error-text 
                  (str "\nlevel: " level "/" maxlevel
                       " duration: " (/ duration 1000000.0) 
                       " complexity: " (cljsdku.generator/eval-way (res :ways))"/"(cljsdku.pbls/pbls-complexity dim orgarray)"/"(cljsdku.pbls/pbls-complexity2 dim orgarray)
                       " # of solutions: " cr
                       "\n" orgarray 
                       "\n" array 
                       (if (= 1 cr) (str "\nway:" (res :ways)) "")))))))
        fill-panel-array-by-example 
          (fn [index]
            (let [sudokuArray (nth cljsdku.test-data/examples-3d index)]
              (fill-panel-array sudokuArray)))
        nextprev-puzzle (fn [nextprev] 
                 (let [index (nextprev (get-index))]
                   (if (and (>= index 0)(<= index (count cljsdku.test-data/examples-3d)))
                   (do 
                     (set-index index)
                     (fill-panel-array-by-example index)))))
        add-one-random 
          (fn []
            (let [sudoku-array (fill-sudoku-array-by-input dim panel-array)
                  poss (cljsdku.pbls/create-pbls dim sudoku-array)
                  chosenpos (poss (rand-int (count poss)))
                  chosenval ((last chosenpos) (rand-int (count (last chosenpos))))]
              (println "pos: " chosenpos " val: " chosenval)
              (fill-panel-array (assoc sudoku-array (first chosenpos) chosenval))))  
        ]
  
  (println text-array)  
  (listen grid-field
            :document 
              #(let [dummy %
                      content (.getText grid-field)
                       new-array (string-to-int-array content)]
                 (if (and (not= nil new-array) (= (count text-array) (count new-array)))
                   (try (fill-panel-array new-array) (catch Exception e))
                   (println "invalid input")
                   )
                 ))
  (mig-panel :constraints ["fillx", "[left]"]
    :items [
          [ (mig-panel :constraints ["wrap 9"] :items panel-array) "spanx 1,spany 15, width 500"]
          [ (label :text "example: ") "cell 1 1"]
          [ text-field "cell 1 1,width 50"]
          [ (button :text "<<" :mnemonic \N :listen [:action 
               (fn [e] (nextprev-puzzle dec))]) "cell 2 1"]
          [ (button :text ">>" :mnemonic \N :listen [:action 
               (fn [e] (nextprev-puzzle inc))]) "cell 2 1,newline"]          
          [ (button :text "solvegrid" :mnemonic \N :listen [:action 
               (fn [e] 
                 (let [act-sudoku-array (fill-sudoku-array-by-input dim text-array)]
                   (dosync (ref-set last-sudoku-array act-sudoku-array))
                   (let [inputarray (deref last-sudoku-array)
                         starttime (. java.lang.System (clojure.solver/nanoTime))
                         res (cljsdku.solver/sudoku inputarray)
                         duration (- (. java.lang.System (clojure.solver/nanoTime)) starttime)]
                     (check-res-fill-panel res inputarray duration))))]) 
           "cell 1 3"]          
          [ (button :text "back before solve" :mnemonic \N :listen [:action 
               (fn [e] (fill-panel-array (deref last-sudoku-array )))])
           "cell 2 3"]          
          [ (button :text "cleargrid" :mnemonic \N :listen [:action 
               (fn [e] (clear-text-array text-array))]) 
           "cell 1 4"]
          [ (button :text "addrandom" :mnemonic \N :listen [:action 
               (fn [e] (add-one-random))])
           "cell 2 4"]   
          [ (button :text "create sudoku" :mnemonic \N :listen [:action 
               (fn [e] (fill-panel-array (cljsdku.generator/create-sudoku (fill-sudoku-array-by-input dim text-array))))])
           "cell 1 5"]
          [ (button :text "hints" :mnemonic \N :listen [:action 
               (fn [e] 
                 (let [pbls (cljsdku.pbls/create-pbls dim (fill-sudoku-array-by-input dim text-array))
                       infopbls (map #(list 
                                        (str (inc (cljsdku.tools/index-2-col dim (first %))) "/" (inc (cljsdku.tools/index-2-row dim (first %)))
                                          "(" (first %) ")")                                        
                                        (last %)) 
                                 pbls)] 
                   (println infopbls)
                   (append-text 
                     (str "\ncomplexity: " (cljsdku.pbls/pbls-complexity pbls) "/" (cljsdku.pbls/pbls-complexity2 pbls) "\n" 
                        (println-str infopbls)))))])
           "cell 1 6"]
           [ (label :text "complexity: ") "cell 2 6"]
          [ complexity-field "cell 2 6,width 40"]
          [ complexity-field2 "cell 2 6,width 50"]
          [ (label :text "search time: ") "cell 1 7"]
          [ time-field "cell 1 7,width 50"]
          [ (label :text "thread# : ") "cell 2 7"]
          [ thread-field "cell 2 7,width 50,newline,wrap"]
           [ (button :text "improve sudoku" :mnemonic \N :listen [:action 
               (fn [e] 
                 (fill-panel-array 
                   (cljsdku.generator/improve-parallel 
                     (get-threads) (get-time) (fill-sudoku-array-by-input dim text-array)))
                 (append-text "\ndone"))])
           "newline"]   
          [ (label :text "" ) "spany 20,newline"] 
          [ (label :text "input grid: ") "cell 0 16,spanx 4"]
          [ grid-field  "cell 0 16,spanx 4,width 1000"]
         
          [ s "cell 0 17,spanx 4, width 1000"]
          ])))



(defn make-frame 
  []
  (frame
    :title "Cljsdku - Sudoku in Clojure"
    :size  [600 :by 600]
    :on-close :exit
    ;:menubar (menubar :items [(menu :text "View" :items [(menu-item :class :refresh)])])
    :content (border-panel
               :border 5
               :hgap 5
               :vgap 5
;               :north  (make-toolbar)
               :center (make-mig)
               ;:south (label :id :status :text "Ready")
               )))

(defn -main [& args]
  (invoke-later 
    (-> 
      (make-frame)
;      add-behaviors
      show!))
  ; Avoid RejectedExecutionException in lein :(
  @(promise))

(def dummy "aaa")

(-main)
