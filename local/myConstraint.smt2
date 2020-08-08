(set-option :timeout 5000)
(declare-fun tvw_a () Int)
(assert (not  (<  tvw_a   5 ) ) )
(check-sat)(get-model)
