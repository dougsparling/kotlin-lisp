(begin
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
