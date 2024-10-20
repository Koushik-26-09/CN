set ns [new Simulator]
$ns rtproto LS
$ns color 1 green
	
set n0 [$ns node]
set n1 [$ns node]
set n2 [$ns node]
set n3 [$ns node]
set n4 [$ns node]
set n5 [$ns node]
set n6 [$ns node]

set tf [open out_ls.tr w]
$ns trace-all $tf

set nf [open out_ls.nam w]
$ns namtrace-all $nf

set ft [open "lsr_th" "w"]

$n0 label "node 0"
$n1 label "node 1"
$n2 label "node 2"
$n3 label "node 3"
$n4 label "node 4"
$n5 label "node 5"
$n6 label "node 6"

$ns duplex-link $n0 $n1 1.5Mb 10ms DropTail
$ns duplex-link $n1 $n2 1.5Mb 10ms DropTail
$ns duplex-link $n2 $n3 1.5Mb 10ms DropTail
$ns duplex-link $n3 $n4 1.5Mb 10ms DropTail
$ns duplex-link $n4 $n5 1.5Mb 10ms DropTail
$ns duplex-link $n5 $n6 1.5Mb 10ms DropTail
$ns duplex-link $n6 $n0 1.5Mb 10ms DropTail

$ns duplex-link-op $n0 $n1 orient left-down
$ns duplex-link-op $n1 $n2 orient left-down
$ns duplex-link-op $n2 $n3 orient right-down
$ns duplex-link-op $n3 $n4 orient right
$ns duplex-link-op $n4 $n5 orient right-up
$ns duplex-link-op $n5 $n6 orient left-up
$ns duplex-link-op $n6 $n0 orient left-up

set tcp2 [new Agent/TCP]
$tcp2 set class_ 1
$ns attach-agent $n0 $tcp2
set sink2 [new Agent/TCPSink]
$ns attach-agent $n3 $sink2
$ns connect $tcp2 $sink2

set traffic_ftp2 [new Application/FTP]
$traffic_ftp2 attach-agent $tcp2


proc record {} {
global sink2 tf ft
global ftp

set ns [Simulator instance]
set time 0.1
set now [$ns now]
set bw0 [$sink2 set bytes_]
puts $ft "$now [expr $bw0/$time*8/1000000]"
$sink2 set bytes_ 0
$ns at [expr $now+$time] "record"
}

proc finish {} {
global ns nf
$ns flush-trace
close $nf

exec nam out_ls.nam &
exec xgraph lsr_th &
exit 0
}

$ns at 0.55 "record"
#Schedule events for the CBR agents
$ns at 0.5 "$n0 color \"Green\""
$ns at 0.5 "$n3 color \"Green\""
$ns at 0.5 "$ns trace-annotate \"Starting FTP n0 to n3\""
$ns at 0.5 "$n0 label-color green"
$ns at 0.5 "$n3 label-color green"

$ns at 0.5 "$traffic_ftp2 start"

$ns at 0.5 "$n1 label-color green"
$ns at 0.5 "$n2 label-color green"
$ns at 0.5 "$n4 label-color blue"
$ns at 0.5 "$n5 label-color blue"
$ns at 0.5 "$n6 label-color blue"

$ns rtmodel-at 2.0 down $n2 $n3

$ns at 2.0 "$n4 label-color green"
$ns at 2.0 "$n5 label-color green"
$ns at 2.0 "$n6 label-color green"
$ns at 2.0 "$n1 label-color blue"
$ns at 2.0 "$n2 label-color blue"

$ns rtmodel-at 3.0 up $n2 $n3
$ns at 3.0 "$traffic_ftp2 start"
$ns at 4.9 "$traffic_ftp2 stop"
$ns at 5.0 "finish"
$ns run


