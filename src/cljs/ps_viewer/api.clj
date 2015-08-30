(ns ps-viewer.api)

(defmacro if-success-let
  "Similar like if-let but expects the result of the
  tst to be a map with a :success key which it then 
  checks against to choose then or else, also makes
  the result of tst available to else"
  ([bindings then]
   `(success-let ~bindings ~then nil))
  ([bindings then else]
    (let [form (bindings 0) tst (bindings 1)]
      `(let [temp# ~tst
             ~form temp#]
        (if (:success temp#)
          ~then
          ~else)))))

