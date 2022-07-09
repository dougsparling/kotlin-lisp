(begin
    (require "lists.lisp")

    (define lines (readfile (head argv)))

    (-- e.g. BFFFBBFRRR is BFFFBBF RRR, seat ID = 567, where: BFFFBBF + RRR = 1000110 << 3 + 111 = )
    (define seatId (lambda (pass) (begin
        (define binary (replace (replace (replace (replace pass "B" "1") "F" "0") "R" "1") "L" "0"))
        (atoi binary 2))))

    (define allSeats (map lines seatId))
    (define highestSeatId (reduce allSeats (lambda (lhs rhs) (if (> lhs rhs) lhs rhs))))

    (println "part one:" highestSeatId)

    (println "part two:" (find (range 0 1024) (lambda (i)
        (and (and
            (not (contains i allSeats))
            (contains (+ i 1) allSeats))
            (contains (- i 1) allSeats)))))

)