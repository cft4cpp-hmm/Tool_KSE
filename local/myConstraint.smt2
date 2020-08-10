(set-option :timeout 5000)
(declare-fun tvw_a () Int)
(declare-fun tvw_b () Int)
(declare-fun tvw_c () Int)
(assert (<=  tvw_a   (+  tvw_b   tvw_c ) ) )
(assert (<=  tvw_b   (+  tvw_a   tvw_c ) ) )
(assert (=  tvw_c   (+  (+  tvw_a   tvw_b )   1 ) ) )
(check-sat)(get-model)
