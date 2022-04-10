(begin
    (require "lists.lisp")

    (define lines (readfile (head argv)))

    (define N (length (head lines)))

    (define toboggan (lambda (pos rest right down) (begin
        (define row (head rest))
        (define tree (if (eq (charAt row pos) "#") 1 0))
        (set! pos (% (+ right pos) N))
        (set! rest (drop rest down))

        (if (eq nil rest)
            tree
            (+ tree (toboggan pos rest right down))))))

    (println "part one: " (toboggan 0 lines 3 1))

    (-- TODO: there's gotta be a better way to do this --)
    (println "part two: "
        (* (toboggan 0 lines 1 1)
            (* (toboggan 0 lines 3 1)
                (* (toboggan 0 lines 5 1)
                    (* (toboggan 0 lines 7 1) (toboggan 0 lines 1 2))))))
)