(ns testvg.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [testvg.service :refer [get-statistics-volume]])
    (:require-macros
      [cljs.core.async.macros :refer [go alt!]])
    (:import goog.History))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to testvg"]
  [:div [:a {:href "#/about"} "go to about page"]]
  [:div [:a {:href "#/test1"} "go to test1 page"]]    
  [:div [:a {:href "#/test2"} "go to test2 page"]]
  [:div [:a {:href "#/test3"} "go to test3 page"]]
  [:div [:a {:href "#/bonus"} "go to bonus page"]]
  [:div [:a {:href "#/chart"} "go to chart page"]]
])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Test1
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn test1-page []
  ;;[:div [:h2 "test1"]
   (let [seconds-elapsed (reagent/atom 0)]     ;; setup, and local state
    (fn []        ;; inner, render function is returned
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
        [:div "Seconds Elapsed: " @seconds-elapsed                          
          [:div [:a {:href "#/"} "go to the home page"]]
      ]))  
          ;;[:div
            ;;[:p "I am a component !"]
            ;;[:p "I include simple-component. !"]
              ;;[:p.someclass
                ;;"I have " [:strong "bold"]
                ;;  [:span {:style{:color "red"}} " and red"] " text."]
                  ;;]
 )

  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Test2
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn test2-page []  
  (let [seconds-elapsed (reagent/atom 0)]     ;; setup, and local state
    (fn []        ;; inner, render function is returned
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
        [:div "Seconds Elapsed: " @seconds-elapsed      
        [:div 
           [:input {:type "text" :value @seconds-elapsed
            }]
          ]                    
          [:div [:a {:href "#/"} "go to the home page"]]          
      ]))  
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Test3
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn test3-page []    
  (let [seconds-elapsed (reagent/atom 0)]     ;; setup, and local state
    (fn []        ;; inner, render function is returned
      (js/setTimeout  #(swap! seconds-elapsed inc) 1000)

      (let [stop ()]     ;; setup, and local state
        (fn []        ;; inner, render function is returned
          (js/clearTimeout #(swap! stop inc))            
        )
        [:div ;;"Seconds Elapsed: " @seconds-elapsed
            [:div                               
              [:input {:type "button" :value "Start"
               :on-click #(swap! seconds-elapsed inc)
              }]
              [:input {:type "text" :value @seconds-elapsed
               :on-click #(swap! seconds-elapsed inc)
              }]                    
              [:input {:type "button" :value "Stop"
               :on-click #(swap! stop inc)
              }]]             
            [:div
              [:div [:a {:href "#/"} "go to the home page"]]
            ]]
      )))
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bonus - Multiple counter, Single counter
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bonus - Single counter
(defonce app-state (atom {:count 0}))
  (defn increment-single-count [e]
    (swap! app-state update-in [:count] inc))

;; Bonus - Multiple counter
(defonce app-state (atom {  :counters {"c1" {:id "c1" :name "counter1" :count 0 } 
                                       "c2" {:id "c2" :name "counter2" :count 0}}}))
(defn increment-multi-count [c]
  (swap! app-state update-in [:id c] :count inc))
(defn counter-component [c]
  [:div
   [:h2 (str " " (:count c) " " (:name c))]
    [:button {:on-click #(increment-multi-count c)} "Increment-count"]])

(defn bonus-page []      
    [:div
      ;;[:h1 (:text @app-state)]        
        ;;[:h4 (str " " (apply + (map :count (vals (:counters @app-state))))
          ;;" counter.")]
   ;;(for [counter (vals (:counters @app-state))]
    ;; ^{:key (:name counter)} [counter-component counter])
        [:h1 (str "Bonus " (:count @app-state) " counter.")]
          [:button {:on-click increment-single-count} "Increment-count"]
            [:div 
              [:div [:a {:href "#/"} "go to the home page"]]  
                [:div 
                  (println "Hello, " name)
                    (let [name "Roy"])]]
          ] 
) 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(defn stop []     
  ;;(fn []        
    ;;(js/clearTimeout #(swap! stop inc))            
;;))

;;(defn load [(.reload (.-location js/window))])

;;(defn refresh []
;;  [:code "load"]
  ;;@load 
  ;;[:input {:type button :value "Press"
   ;; :on-click #(swap! load inc)}]
  ;;)

(defonce state (atom {:start     nil
                      :end       nil
                      :max-range 3600000}))
(defonce counter (atom 0))

(defn chart-page []
  [:div [:h2 "Welcome to Volume"]
    [:p "Press F5 to check the volum statics"]
   [:div [:a {:href "#/"} "go to home"]]
    [:div#container {:style {:min-width "310px" :max-width "800px"
                            :height    "400px" :margin "0 auto"}}]])

(defn get-highcharts []
  (aget js/Highcharts "charts" @counter)  
)

(defn update-range [range]
  (.update (aget (get-highcharts) "xAxis" 0) (clj->js {:minRange range :range range}))
)

(defn add-point [data]
  (let [date (.parse js/Date (first data))
        value (second data)
        start (:start @state)]
    (if start
      (-> (- date start)
          (min (:max-range @state))
          (update-range))
      (swap! state assoc :start date))
    (.addPoint (aget (get-highcharts) "series" 0)
               (clj->js [date value])))
  )

(def chart-config
  {:chart       {:type   "spline"
                 :events {:load (fn []
                                  (.setInterval js/window #(go
                                                            (add-point (<! (get-statistics-volume)))) 2000))}}
   :title       {:text "Volume"}
   :xAxis       {:type "datetime",
                 :tickPixelInterval 50}
   :yAxis       {:plotLines [{:value 0
                              :width 1
                              :color "#808080"}]}
   :plotOptions {:bar {:dataLabels {:enabled true}}}
   :credits     {:enabled false}
   :series      [{:name "Volume"
                  :data []}]
   }
)

(defn chart-did-mount []
  (do
    (js/$ (fn []
            (.highcharts (js/$ "#container")
                         (clj->js chart-config)))))
  )

(defn chart-component []
  (reagent/create-class {:reagent-render     chart-page
                        :component-did-mount chart-did-mount})
)


(defn about-page []
  [:div [:h2 "About testvg"]
   [:div [:a {:href "#/"} "go to the home page"]]
    [:div "Remark"
      [:ul "Complete new environment except FP :-p"]
      [:ul "All time spend to render with react. Still confuse, rendering with highcharts, duplcation -oooooo.. :-|"]
      [:ul "Did not do any other interaction with the highchart in this environment :-( . Creatation of new chart with every figwheel update oohhh no!!!"]
      [:ul "Nice Vigiglobe API"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/test1" []
  (session/put! :current-page #'test1-page))

(secretary/defroute "/test2" []
  (session/put! :current-page #'test2-page))

(secretary/defroute "/test3" []
  (session/put! :current-page #'test3-page))

(secretary/defroute "/bonus" []
  (session/put! :current-page #'bonus-page))

(secretary/defroute "/chart" []
  (session/put! :current-page #'chart-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))
  
  (reagent/render [chart-component] (.getElementById js/document "app"))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))

(defn on-js-reload []
  (swap! counter inc)
  (.log js/console @counter))


;;(def tumblr-url "http://pipes.yahoo.com/pipes/pipe.run?_id=4ddef10340ec6ec0374cbd0f73bce819&_render=json")
 
;;(defn display-count [json-obj]
  ;;(let [data (js->clj json-obj :keywordize-keys true)
    ;;    post-count (:count data)]
    ;;(js/alert (str "Number of posts: " post-count))))
 
;;(defn display-items [json-obj]
  ;;(let [data (js->clj json-obj :keywordize-keys true)
    ;;    items (:items (:value data))
      ;;  titles (map :title items)]
    ;;(js/alert (pr-str titles))))
 
;;(defn retrieve-tumblr [callback error-callback]
  ;;(.send (goog.net.Jsonp. tumblr-url "_callback")
    ;;"" callback error-callback))
 
;;(retrieve-tumblr display-items #(js/alert (str "An error occurred: " %)))