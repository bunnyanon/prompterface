(ns prompterface.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [hiccup2.core :as h]
            [clojure.java.jdbc :as jdbc]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.file :refer :all]
            [ring.adapter.jetty :as jetty]

            [prompterface.templates :refer :all]
            [prompterface.image :refer :all]
            [prompterface.cardutil :refer :all])
  (:gen-class)
  (:import (clojure.lang ExceptionInfo)))

(def buffer-size 1024)

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "prompterface.db"})

(defn read-section [file buffer max read-bytes]
  (if (>= read-bytes max) -1
                          (if (> (count buffer) (- max read-bytes))
                            (.read file buffer 0 (- max read-bytes))
                            (.read file buffer 0 (count buffer)))))

(defn read-section-and-write [source dest skip-start skip-end buffer-size]
  (loop [buffer (byte-array buffer-size)
         max skip-start
         total 0]
    (let [bytes-read (read-section source buffer max total)]
      (if (= bytes-read -1)
        -1
        (do
          (.write dest buffer 0 bytes-read)
          (recur (byte-array buffer) skip-start (+ total bytes-read))))))

  (loop [bytes-to-skip skip-end]
    (when (> bytes-to-skip 0) (recur (- bytes-to-skip (.skip source bytes-to-skip)))))

  (loop [buffer (byte-array buffer-size)]
    (let [bytes-read (.read source buffer 0 buffer-size)]
      (if (= bytes-read -1)
        -1
        (do
          (.write dest buffer 0 bytes-read)
          (recur (byte-array buffer)))))))

;; Divide this by functions...
(defn card-upload-handler [request]
  (let [file (get-in request [:multipart-params "file" :tempfile])]
    (when (nil? file) (throw (ex-info "No Multipart Provided" {:status 400})))
    (let [[raw-data start-position bytes-read] (find-chara file)
          data (validate-card (json/read-str raw-data :key-fn keyword))
          character-id (get (first (jdbc/insert! db :characters
                                                 {:name    (get-in data [:data :name])
                                                  :defs    (json/write-str data)
                                                  :chat_id ((keyword "last_insert_rowid()")
                                                            (first (jdbc/insert! db :chats {:type 0})))}))
                            (keyword "last_insert_rowid()"))]
      (doseq [x (get-in data [:data :tags])]
        (when (empty? (jdbc/query db ["select id from tags where id = ?" x])) (jdbc/insert! db :tags {:id x}))
        (jdbc/insert! db :TagCharacter {:tag_id x
                                        :character_id character-id}))
      (let [filename (str character-id ".png")]
        (with-open [o (io/output-stream (str "./resources/public/" filename))]
          (read-section-and-write (io/input-stream file) o start-position bytes-read buffer-size))
        (str (h/html (htmx-entry (str "public/" filename)
                                 (get-in data [:data :name])
                                 character-id)))))))

;; add error handling
(defn card-delete-handler [request]
  (let [id (get-in request [:route-params :id])]
    (when (nil? id) (throw (ex-info "No ID provided" {:status 400})))
    (let [chat-id (get (first (jdbc/query db ["select chat_id from characters where id = ?" id])) :chat_id)]
      (when (nil? chat-id) (throw (ex-info "No chat found" {:status 404})))
      (jdbc/delete! db :characters ["id=?" id])
      (jdbc/delete! db :TagCharacter ["character_id=?" id])
      (jdbc/delete! db :chats ["id=?" chat-id])))
  {:status 200 :headers {"Content/Type" "text/plain"}})

(defn card-edit [request editor-type]               ;; editor-type because of full screen ediotr
  (let [id (:id (:route-params request))
        data (jdbc/query db ["select defs from characters where id = ?" id])]
    (str (h/html (editor-type (-> (first data) (:defs) (json/read-str :key-fn keyword) (:data)) id)))))
(defn card-edit-sidebar-handler [request] (card-edit request editor-div))

(defn card-edit-fullscreen-handler [request] nil)           ;; TODO: IMPLEMENT

(defn card-validate-edit-handler [request]
  (let [id (get-in request [:route-params :id])
        data (get request :form-params)]
    (when (or (empty? data) (empty? id)) (throw (ex-info "No Data Provided" {:status 400})))
    (let [defs (jdbc/query db ["select defs from characters where id = ?" id])]
      (when (empty? defs) (throw (ex-info "No Such Card" {:status 404})))
      (let [updated-data (validate-and-replace-existing
                           (json/read-str (get (first defs) :defs))
                           data)]
          (jdbc/update! db :characters {:defs (json/write-str updated-data)
                                        :name (get-in updated-data ["data" "name"])}
                        ["id = ?" id])
          (str (h/html (htmx-entry (str "public/" id ".png") (get-in updated-data ["data" "name"]) id)))))))

(defn wrap-error-handler [handler]
  (fn [request]
    (try
      (handler request)
      (catch ExceptionInfo e
        (println e)
        {:status (get (ex-data e) :status)
         :headers {"Content/Type" "text/plain"}
         :body (str (h/html (htmx-error (ex-message e))))})
      (catch Exception e
        (println (.getMessage e))
        {:status  500
         :headers {"Content/Type" "text/plain"}
         :body    (str (h/html (htmx-error (.getMessage e))))}))))

(def card-app
  (-> card-upload-handler
      wrap-params
      wrap-multipart-params
      wrap-error-handler))

(def card-delete-handler-app
  (-> card-delete-handler
      wrap-params
      wrap-error-handler))

(def card-edit-sidebar-handler-app
  (-> card-edit-sidebar-handler
      wrap-params))

(def card-edit-fullscreen-handler-app
  (-> card-edit-sidebar-handler
      wrap-params))

(def card-validate-edit-handler-app
  (-> card-validate-edit-handler
      wrap-params
      wrap-error-handler))

(defn root-handler [_]
  (str (h/html
         (h/raw "<!DOCTYPE html>")
         (htmx-boilerplate
           (htmx-sidebar
             (map #(htmx-entry
                     (str "public/" (% :id) ".png")
                     (:name %)
                     (:id %)) (jdbc/query db ["select id,name from characters limit 10"] {:id :name})))))))

(defroutes approutes
           (POST "/card/upload" [] card-app)
           (POST "/card/edit/:id" [] card-edit-sidebar-handler-app)
           (POST "/card/edit/fullscreen/:id" [] card-edit-fullscreen-handler-app) ;; UNUSED: TODO: IMPLEMENT
           (GET "/card/delete/:id" [] card-delete-handler-app)
           (POST "/card/edit/:id/validate" [] card-validate-edit-handler-app)
           (context "/public" []
             (-> (route/not-found "File Not Found")
                 (wrap-file "./resources/public")))
           (GET "/" [] root-handler)
           (route/not-found "<h1>404 NOT FOUND</h1>"))

(defn -main []
  (jetty/run-jetty approutes {:port 3000}))