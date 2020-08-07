(set-option :timeout 5000)
(declare-fun tvw_year () Int)
(assert (or  (and  (=  (mod  tvw_year   4 )   0 )   (or (>  (mod  tvw_year   100 )   0 ) (<  (mod  tvw_year   100 )   0 )) )   (or (>  (mod  tvw_year   400 )   0 ) (<  (mod  tvw_year   400 )   0 )) ) )
(check-sat)(get-model)
