(begin
    (require "lists.lisp")

    (define lines (readfile (head argv)))

    (-- pairs of field to value)
    (define fields nil)

    (-- list of lists of field pairs)
    (define passports nil)

    (-- a full set of either of these fields is considered "complete")
    (define npc (sorted (quote ("byr" "iyr" "eyr" "hgt" "hcl" "ecl" "pid" "cid"))))
    (define complete (sorted (quote ("byr" "iyr" "eyr" "hgt" "hcl" "ecl" "pid"))))

    (define tap (lambda (val tapper) (begin (tapper val) val)))
    (define within (lambda (start value end)
        (and (<= start value) (>= end value))))

    (foreach lines (lambda (line) (begin
        (if (eq "" line)
            (begin
                (-- passport delimiter hit)
                (set! passports (cons (sorted fields) passports))
                (set! fields nil)
            )
            (foreach (splitp line " ") (lambda (pair) (begin
                (-- add fields to current passport)
                (set! fields (cons (splitp pair ":") fields))
                )))))
    ))

    (define completePassports (filter passports (lambda (pp) (begin
        (define ppFields (map pp head))
        (or (eq complete ppFields) (eq npc ppFields))))))

    (println "part one: " (size completePassports))

    (-- TODO: is there a better way to define list literals? this feels weird but CL does it)
    (define validators (list
        (list "byr" (lambda (val) (within 1920 (atoi val) 2002)))
        (list "iyr" (lambda (val)  (within 2010 (atoi val) 2020)))
        (list "eyr" (lambda (val)  (within 2020 (atoi val) 2030)))
        (list "hgt" (lambda (val)
            (if (not (matches val "\d+(cm|in)"))
                false
                (begin
                    (define unit (substr val (- (length val) 2) (length val)))
                    (define num (atoi (substr val 0 (- (length val) 2))))
                    (if (eq unit "cm")
                        (within 150 num 193)
                        (within 59 num 76))))))

        (list "hcl" (lambda (val) (matches val "#[a-f0-9]{6}")))
        (list "ecl" (lambda (val) (contains val (quote ("amb" "blu" "brn" "gry" "grn" "hzl" "oth")))))
        (list "pid" (lambda (val) (matches val "\d{9}")))
        (list "cid" (lambda (val) true))))

    (-- given a complete passport, is it valid?)
    (define validate (lambda (pp) (begin
        (define results (map pp (lambda (pair) (begin
            (-- println "searching for" (head pair))
            (define validator (head (tail (find validators (lambda (v) (eq (head v) (head pair)))))))
            (-- println "calling validator for " (head pair) "->" (validator (head (tail pair))))
            (validator (head (tail pair)))))))

        (reduce results and))))

    (println "part two: " (size (filter completePassports validate)))
)