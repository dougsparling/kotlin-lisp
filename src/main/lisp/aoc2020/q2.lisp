(begin
    (require "lists.lisp")

    (define lines (readfile (head argv)))

    (define parsed (map lines (lambda (line) (splitp line "[ \-:]+"))))

    (define countMatching (lambda (list predicate)
         (if (eq list nil)
            0
            (+
                (if (predicate (head list)) 1 0)
                (countMatching (tail list) predicate)))))

    (define isValidP1 (lambda (line) (begin
        (define min  (atoi (head line)))
        (define max  (atoi (head (tail line))))
        (define char (head (tail (tail line))))
        (define str  (head (tail (tail (tail line)))))
        (define matches (size (filter (chars str) (lambda (c) (eq c char)))))
        (and (>= matches min) (<= matches max)))))

    (define isValidP2 (lambda (line) (begin
        (define i1  (atoi (head line)))
        (define i2  (atoi (head (tail line))))
        (define char (head (tail (tail line))))
        (define str  (head (tail (tail (tail line)))))

        (not (eq
            (eq (charAt str (- i1 1)) char)
            (eq (charAt str (- i2 1)) char))))))

    (println "part one: " (countMatching parsed isValidP1))
    (println "part two: " (countMatching parsed isValidP2))
)