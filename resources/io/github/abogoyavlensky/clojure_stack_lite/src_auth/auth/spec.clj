(ns {{main/ns}}.auth.spec)

(def Email
  [:and
   [:string
    {:min 5
     :max 254}]
   [:re
    {:error/message "Invalid email format"}
    #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"]])
