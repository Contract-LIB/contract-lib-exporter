
/*

Contract-Lib:

NOT CHECKED AT ALL!

(declare-abstractions 
  ((List 0))
  ((List (content (Seq Int)))) )
  
;; META: name of return value is result
(declare-contract List.get
  ((this (in List)) (idx (in Int)) (result (out Int))
  ( (and (<= 0 idx) (< idx (len (content this))))
    (= result (select (content this) idx))))


(declare-contract List.get
  ((this (inout List)) (idx (in Int)) (value (in Int))
  ( (and (<= 0 idx) (< idx (len (content this))))
    (= (content this) (store (old (content this)) idx value)) ))


// ...

*/

interface List {

   //@ predicate valid(list<int> content);

   int get(int idx);
     //@ requires valid(?content) &*& 0 <= idx &*& idx < length(content);
     //@ ensures  valid(content) &*& result == nth(idx, content);
   
   void set(int idx, int value);
     //@ requires valid(?content) &*& 0 <= idx &*& idx < length(content);
     //@ ensures  valid(?content2) &*& content2 == update(idx, value, content);
}


// ------------------------------------

class ArrayList implements List {

   int[] data;

   /*@ predicate valid(list<int> content) = this.data |-> ?d &*& array_slice(d, 0, d.length, content); @*/
   
   int get(int idx) 
     //@ requires valid(?content) &*& 0 <= idx &*& idx < length(content);
     //@ ensures  valid(content) &*& result == nth(idx, content);
   { 
     //@ open valid(content);
     //@ assert 0 <= idx && idx < length(content);
     //@ close valid(content);     
     return data[idx]; 
   }
   
   void set(int idx, int value) 
    //@ requires valid(?content) &*& 0 <= idx &*& idx < length(content);
    //@ ensures  valid(?content2) &*& content2 == update(idx, value, content);
   { 
     //@ open valid(content);
     data[idx] = value; 
     //@ close valid(update(idx, value, content));  
   }
}

