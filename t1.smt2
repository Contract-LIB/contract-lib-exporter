(declare-datatypes ((list 0) (tree 0)) 
 (((cons (head tree) (tail list)) (nil))
  ((node (data Int) (children list))))
)

; DeclareDatatypes[arities=[Pair[first=list, second=0], Pair[first=tree, second=0]],
;                  datatypes=[Datatype[params=[list, tree], constrs=[Pair[first=cons, second=[Pair[first=head, second=[Param[name=tree]]],
;                                                                                             Pair[first=tail, second=[Param[name=list]]]]],
;                                                                    Pair[first=nil, second=[]]]],
;                             Datatype[params=[list, tree], constrs=[Pair[first=node, second=[Pair[first=data, second=[Sort[name=Int, arguments=[]]]],
;                                                                                             Pair[first=children, second=[Param[name=list]]]]]]]]]
(declare-abstractions
  ((List 0))
  (((List (content (Seq Int))))))

;; META: name of return value is result
(define-contract List.get
  ((this (in List)) (idx (in Int)) (result (out Int)))
  ( ( (and (<= 0 idx) (< idx (len (content this))))
      ( = result (select (content this) idx)))) )


(define-contract List.set
  ((this (inout List)) (idx (in Int)) (value (in Int)))
  (( (and (<= 0 idx) (< idx (len (content this))))
     ( = (content this) (store (old (content this)) idx value)) )))
