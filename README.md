# java8-epoll-kqueue-webserver

Simple lock free single threaded webserver `8080`

Depends:
* redis on default port
* Java 1.8
* All jars from `lib` folder

Example curl request:
`curl -v -XPOST http://127.0.0.1:8080/handler -d'{"command":"PING","arguments":[{"key":"argument1","value":"some argument value"}]}'`

Performance tested with `wrk` command: `./wrk --latency -d 5 -c 200 -t 4 http://127.0.0.1:8080/handler -s test-java.lua`

	Running 5s test @ http://127.0.0.1:8080/handler
	  4 threads and 200 connections
	  Thread Stats   Avg      Stdev     Max   +/- Stdev
	    Latency     7.20ms    6.02ms  96.26ms   95.75%
	    Req/Sec     1.78k   695.75     3.47k    70.00%
	  Latency Distribution
	     50%    6.03ms
	     75%    7.43ms
	     90%    8.95ms
	     99%   31.13ms
	  35436 requests in 5.02s, 4.90MB read
	  Socket errors: connect 0, read 0, write 0, timeout 0
	Requests/sec:   7054.41
	Transfer/sec:      0.98MB
