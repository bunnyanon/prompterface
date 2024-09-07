(ns prompterface.templates
  (:require [hiccup.util :as hu])
  (:import (java.time Instant)))

(defn editor-div [card-v2-data id]
  [:div {:id (str "editor")
         :class "font-[inter] fixed text-gray-50 w-[512px] h-full bg-zinc-800 z-[999] overflow-x-scroll rounded p-2 mb-4 bg-zinc-700"}
   [:form {:id "editor-form"
           :hx-post (str "/card/edit/" id "/validate")
           :hx-swap "outerHTML"
           :_ "on submit remove #editor"
           :hx-on:htmx:response-error "errorhandler(event)"
           :hx-target (str "#card-" id)}
    [:label {:class "block" :for "name"} "Name"]
    [:input {:class "rounded caret-blue-200 bg-zinc-600"
             :type "text"
             :id "name"
             :name "name"
             :value (card-v2-data :name)}]
    [:label {:class "block" :for "description"} "Description"]
    [:textarea {:class "w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "description"
                :name "description"}
     (card-v2-data :description)]
    
    [:label {:class "block" :for "first_mes"} "First Message"]
    [:textarea {:class "w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "first_mes"
                :name "first_mes"}
     (card-v2-data :first_mes)]
    
    [:label {:class "block" :for "mes_example"} "Message Example"]
    [:textarea {:class "w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "mes_example"
                :name "mes_example"}
     (card-v2-data :mes_example)]
    
    [:label {:class "block" :for "personality"} "Personality"]
    [:textarea {:class "w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "personaity"
                :name "personality"}
     (card-v2-data :personality)]
    
    [:label {:class "block" :for "scenario"} "Scenario"]
    [:textarea {:class "w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "scenario"
                :name "scenario"}
     (card-v2-data :description)]

    [:label {:class "block" :for "creator_notes"} "Creator Notes"]
    [:textarea {:class "w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "creator_notes"
                :name "creator_notes"}
     (card-v2-data :creator_notes)]

    [:label {:class "block" :for "system_prompt"} "System Prompt"]
    [:textarea {:class "block w-[90%] rounded caret-blue-200 bg-zinc-600 mb-4"
                :id "system_prompt"
                :name "system_prompt"}
     (card-v2-data :system_prompt)]
    [:label {:for "creator"} "Creator"]
    [:input {:class "mb-4 block rounded caret-blue-200 bg-zinc-600"
             :type "text"
             :id "creator"
             :name "creator"
             :value (card-v2-data :creator)}]


     [:button {:type "button" :_ "on click remove #editor"} "Discard"]
     [:button {:type "submit"} "Done Editing"]]])

(defn htmx-entry [url name id]
  [:div {:id (str "card-" id)
         :class "box-border outline-blue-200 hover:outline hover:outline-2 m-4 p-[0.25rem] rounded-lg flex items-center gap-4"}
   [:img {:class "flex-none object-cover rounded object-top h-14 w-14"
          :src (str url "?" (inst-ms (Instant/now)))}]
   [:div {:class "select-none w-full bg-zinc-700 rounded-lg flex items-center ml-1 pl-4 h-14"}
    [:span {:class "text-gray-50 text-xl font-[inter]"} name]
    [:div {:class "flex ml-auto mr-4 gap-4"}
     [:svg {:hx-post (str "/card/edit/" id)
            :hx-swap "afterbegin"
            :hx-target "body"
            :xmlns "http://www.w3.org/2000/svg"
            :width "24"
            :height "24"
            :viewbox "0 0 24 24"
            :class "fill-white hover:fill-blue-200"}
      [:path {:d "M18.404 2.998c-.757-.754-2.077-.751-2.828.005l-1.784 1.791L11.586 7H7a.998.998 0 0 0-.939.658l-4 11c-.133.365-.042.774.232 1.049l2 2a.997.997 0 0 0 1.049.232l11-4A.998.998 0 0 0 17 17v-4.586l2.207-2.207v-.001h.001L21 8.409c.378-.378.586-.881.585-1.415 0-.535-.209-1.038-.588-1.415l-2.593-2.581zm-3.111 8.295A.996.996 0 0 0 15 12v4.3l-9.249 3.363 4.671-4.671c.026.001.052.008.078.008A1.5 1.5 0 1 0 9 13.5c0 .026.007.052.008.078l-4.671 4.671L7.7 9H12c.266 0 .52-.105.707-.293L14.5 6.914 17.086 9.5l-1.793 1.793zm3.206-3.208-2.586-2.586 1.079-1.084 2.593 2.581-1.086 1.089z"}]]
     [:a {:href (str "/card/download/" id)}
      [:svg {:xmlns "http://www.w3.org/2000/svg"
             :width "24"
             :height "24"
             :viewbox "0 0 24 24"
             :class "fill-white hover:fill-blue-200"}
       [:path {:d "m12 16 4-5h-3V4h-2v7H8z"}]
       [:path {:d "M20 18H4v-7H2v7c0 1.103.897 2 2 2h16c1.103 0 2-.897 2-2v-7h-2v7z"}]]]
     [:svg {:hx-get (str "/card/delete/" id)
            :hx-swap "outerHTML"
            :hx-target (str "#card-" id)
            :xmlns "http://www.w3.org/2000/svg"
            :width "24"
            :height "24"
            :viewbox "0 0 24 24"
            :class "fill-white hover:fill-blue-200"}
      [:path {:d "M6 7H5v13a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7H6zm4 12H8v-9h2v9zm6 0h-2v-9h2v9zm.618-15L15 2H9L7.382 4H3v2h18V4z"}]]]]])

(defn htmx-sidebar [entires]
  [:div {:id "sidebar"
         :class "h-full bg-zinc-800 fixed w-[96px] hover:w-[512px] transition-all ease-out duration-600 overflow-x-hidden overflow-y-scroll"}
   [:div {:hx-on:click "document.getElementById('card').click()"
          :class "select-none flex-none hover:bg-blue-100 overflow-hidden bg-blue-200 m-[1.25rem] rounded-lg flex items-center gap-4"}
    [:svg {:class "flex-none w-14 h-14 fill-black"
           :xmlns "http://www.w3.org/2000/svg"
           :viewBox "0 0 24 24"}
     [:path {:d "M19 11h-6V5h-2v6H5v2h6v6h2v-6h6z"}]]
    [:span {:class "text-xl text-black font-[inter]"} "Add Character"]
    [:form {:id "form"
           :hx-trigger "change"
           :hx-encoding "multipart/form-data"
           :hx-swap "beforeend"
           :hx-target "#sidebar"
           :hx-post "/card/upload"}
    [:input {:id "card"
             :name "file"
             :type "file"
             :hidden "true"
             }]]]
   (for [entry entires] entry)])

(defn htmx-boilerplate [content]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:link {:rel "preconnect", :href "https://fonts.googleapis.com"}]
    [:link
     {:rel "preconnect",
      :href "https://fonts.gstatic.com",
      :crossorigin ""}]
    [:link
     {:href
      "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap",
      :rel "stylesheet"}]
    [:link
     {:href
      "https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;p1,800;1,900&display=swap",
      :rel "stylesheet"}]
    [:title "prompterface"]
    [:script {:src "https://cdn.tailwindcss.com"}] ;; TODO: Change in Production
    [:script {:src "https://unpkg.com/htmx.org@2.0.2"}]     ;; TODO: Change in Production
    [:script {:src "https://unpkg.com/hyperscript.org@0.9.12"}] ;; TODO: Change in Production
    [:body {:class "bg-neutral-900"}
     [:div {:id "notify-stack"
            :class "absolute right-0 h-full w-[300px]"}]
     content]
    [:script (hu/raw-string
               "function errorhandler(event) {
                 htmx.swap(\"#notify-stack\", event.detail.xhr.response, {swapStyle: 'afterbegin'});
               }")]]])

(defn htmx-error [error-message]                            ;; shit css but still
  [:div {:class "w-full rounded bg-red-400 text-xs opacity-70"
         :_     "on load wait 5s then remove me on click remove me"}
   error-message]
  )