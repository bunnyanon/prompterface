(ns prompterface.presetutil)

(def prompt
  [:map
   [:name string?
    :identifier string?
    :system_prompt boolean?
    :injection_position int?
    :injection_depth int?

    :role [:maybe string?]
    :content [:maybe string?]
    :marker [:maybe boolean?]
    :enabled [:maybe boolean?]]])

(def preset

  )