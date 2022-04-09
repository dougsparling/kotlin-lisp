(begin
    (require "lists.lisp")

    (define lines (readfile (head argv)))
    (define input (map lines atoi))

    (define findSumsTo (lambda (target nums)
        (if (< (size nums) 2)
            nil
            (begin
                (define first (head nums))
                (define need (- target first))

                (if (contains need (tail nums))
                    (cons first (cons need nil))
                    (findSumsTo target (tail nums)))))))

    (define result (findSumsTo 2020 input))

    (println "part-one:" (product result))

    (define findTripleSum (lambda (target nums)
        (if (< (size nums) 3)
            nil
            (begin
                (define first (head nums))
                (define need (- target first))
                (define subSum (findSumsTo need (tail nums)))

                (if (eq subSum nil)
                    (findTripleSum target (tail nums))
                    (cons first subSum))))))

    (define result2 (findTripleSum 2020 input))

    (println "part-two:" (product result2)))