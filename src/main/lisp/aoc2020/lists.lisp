(begin
    (-- invokes a callback for each element in the list. Returns nil and ignores
        whatever is returned from the action)
    (define foreach (lambda (list action) (begin
        (if (eq list nil)
            nil
            (begin
                (action (head list))
                (foreach (tail list) action))))))

    (-- map transforms all elements of the given list using the supplied mapping proc)
    (define map (lambda (list mapper)
        (if (eq list nil)
            nil
            (cons
                (mapper (head list))
                (map (tail list) mapper)))))

    (-- reduce produces a single value by repeatedly applying a combining function on adjacent elements
        of the list, starting from the left)
    (define reduce (lambda (list reducer)
        (if (eq list nil)
            nil
            (if (eq (tail list) nil)
                (head list)
                (reducer (head list) (reduce (tail list) reducer))))))

    (-- filter returns a list containing only elements matching a predicate)
    (define filter (lambda (list predicate)
        (if (eq list nil)
            nil
            (if (predicate (head list))
                (cons (head list) (filter (tail list) predicate))
                (filter (tail list) predicate)))))

    (-- find returns the first element that satisfies the predicate, or nil if none did)
    (define find (lambda (list predicate)
        (if (eq list nil)
            nil
            (if (predicate (head list))
                (head list)
                (find (tail list) predicate)))))

    (-- drop returns a list with the first n elements removed. If n exceeds the length of the
        list, returns nil)
    (define drop (lambda (list n)
        (if (<= n 0) list (drop (tail list) (- n 1)))))

    (define sum (lambda (nums) (reduce nums +)))
    (define product (lambda (nums) (reduce nums *)))

    (-- returns true if some element in the list is equal to the given needle)
    (define contains (lambda (needle haystack)
        (if (eq haystack nil)
            false
            (if (eq (head haystack) needle)
                true
                (contains needle (tail haystack))))))

    (-- returns the size of the given list, or 0 if the list is empty)
    (define size (lambda (list)
        (if (eq list nil)
            0
            (+ 1 (size (tail list)))))))
