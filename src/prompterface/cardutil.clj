(ns prompterface.cardutil
  (:require [malli.core :as m]
            [malli.generator :as mg]))


;; pass the whole defs. Not just the data
;; adds missing fields. Does not remove fields that are not used,
;;as they might be used by another interface
;; optional and not generally implemented fields
;; We assume this JSON is ok I guess
(defn add-missing-fields [defs]
  (assoc defs :data (let [trans-defs (transient (defs :data))
                          missing-fields-data {:creator_notes ""
                                               :system_prompt ""
                                               :post_history_instructions ""
                                               :alternate_greetings nil
                                               :character_book {}
                                               :tags nil
                                               :creator ""
                                               :character_version "main"
                                               :extensions nil} ]
                      (doseq [x (keys missing-fields-data)]
                        (when (nil? (trans-defs x))
                          (assoc! trans-defs x (missing-fields-data x))))
                      (persistent! trans-defs))))

(def card-v1
  [:map
   [:name string?]
   [:description string?]
   [:personality string?]
   [:scenario string?]
   [:first_mes string?]
   [:mes_example string?]])


(def character-book ;; implement later
  [:map])

(def card-v2
  [:map
   [:spec [:enum "chara_card_v2"]]
   [:spec_version [:enum "2.0"]]
   [:data [:map
           [:name string?]
           [:description string?]
           [:personality string?]
           [:scenario string?]
           [:first_mes [:maybe string?]] ;; Add option of not having any first message
           [:mes_example string?]

           [:creator_notes {:optional true} string?]  ;; apparently chub doesn't support this
           [:system_prompt {:optional true} string?]
           [:post_history_instructions {:optional true} [:maybe string?]]
           [:alternate_greetings [:maybe [:vector string?]]]
           [:character_book {:optional true} [:maybe character-book]]
           
           [:tags [:maybe [:vector string?]]]
           [:creator string?]
           [:character_version string?]
           [:extensions [:map-of keyword? any?]]]]])

(mg/generate card-v1)
(mg/generate character-book)
(mg/generate card-v2)

(defn validate [data] (= (keys data) '("personality"
                                       "scenario"
                                       "mes_example"
                                       "system_prompt"
                                       "creator"
                                       "name"
                                       "creator_notes"
                                       "first_mes"
                                       "description")))

(defn replace-existing [existing replaced]
  (when (nil? (get existing "data")) (throw (ex-info "Malformed Request" {:status 400})))
  (assoc existing "data" (let [trans-defs (transient replaced)
                               missing-fields-data (existing :data)]
                           (doseq [x (keys missing-fields-data)]
                             (when (nil? (trans-defs x))
                               (assoc! trans-defs x (missing-fields-data x))))
                           (persistent! trans-defs))))

(defn validate-and-replace-existing [existing replaced]
  (if (validate replaced)
    (replace-existing existing replaced)
    (throw (ex-info "Malformed Request" {:status 400}))))

(defn v1->v2 [card] {:spec "chara_card_v2"
                     :spec_version "2.0"
                     :data (assoc card
                                  :creator_notes ""
                                  :system_prompt ""
                                  :post_history_instructions ""
                                  :alternate_greetings nil
                                  :character_book ""

                                  :tags nil
                                  :creator ""
                                  :character_version ""
                                  :extensions {})})
(defn validate-card [card]
  (cond (m/validate card-v2 card) (add-missing-fields card)
        (m/validate card-v1 card) (v1->v2 card)
        :else (throw (ex-info "Character Card JSON of unknown format" {:status 400})))) ;; TODO: This should be handled
