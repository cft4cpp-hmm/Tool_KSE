(set-option :timeout 5000)
(declare-fun tvw_x () Int)
(declare-fun tvw_minn () Int)
(declare-fun tvw_maxx () Int)
(assert (not  (or  (<=  tvw_x   tvw_minn )   (>=  tvw_x   tvw_maxx ) ) ) )
(check-sat)(get-model)
