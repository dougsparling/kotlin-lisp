(begin
    (define map (lambda (list mapper)
        (if (eq list nil)
            nil
            (cons
                (mapper (head list))
                (map (tail list) mapper)))))

    (define reduce (lambda (list reducer)
        (if (eq list nil)
            nil
            (if (eq (tail list) nil)
                (head list)
                (reducer (head list) (reduce (tail list) reducer))))))

    (define filter (lambda (list predicate)
        (if (eq list nil)
            nil
            (if (predicate (head list))
                (cons (head list) (filter (tail list) predicate))
                (filter (tail list) predicate)))))

    (define drop (lambda (list n)
        (if (<= n 0) list (drop (tail list) (- n 1)))))

    (define sum (lambda (nums) (reduce nums +)))
    (define product (lambda (nums) (reduce nums *)))

    (define contains (lambda (needle haystack)
        (if (eq haystack nil)
            false
            (if (eq (head haystack) needle)
                true
                (contains needle (tail haystack))))))

    (define size (lambda (list)
        (if (eq list nil)
            0
            (+ 1 (size (tail list)))))))
