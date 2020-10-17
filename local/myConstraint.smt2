(set-option :timeout 5000)
(declare-fun tvw_averageGrade () Int)
(assert (and  (>  tvw_averageGrade   95 )   (<  tvw_averageGrade   100 ) ) )
(check-sat)(get-model)
