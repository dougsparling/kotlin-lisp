(begin
    (define lines (readfile (head argv)))

    (define map (lambda (in mapper)
        (if (eq in nil)
            nil
            (cons
                (mapper (head in))
                (map (tail in) mapper)))))

    (define reduce (lambda (list reducer)
        (if (eq list nil)
            nil
            (if (eq (tail list) nil)
                (head list)
                (reducer (head list) (reduce (tail list) reducer))))))


    (define sum (lambda (nums) (reduce nums +)))
    (define product (lambda (nums) (reduce nums *)))

    (define input (map lines atoi))

    (define contains (lambda (needle haystack)
        (if (eq haystack nil)
            false
            (if (eq (head haystack) needle)
                true
                (contains needle (tail haystack))))))

    (define size (lambda (list)
        (if (eq list nil)
            0
            (+ 1 (size (tail list))))))

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

    (println "part-two:" (product result2))
)