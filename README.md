NanoPool
========

NanoPool (NP) is a lightweight and highly scalable JDBC2 connection pool.

By "highly scalable" is meant that NP scales very well (hopefully linearly) to
hundreds of concurrent threads, and with a very low constant-time overhead.

This means that thread contention will be on the *size* of the pool (or the
JDBC driver, or the database, or..) as oppose to the internal implementation
of the connection pool.

License
-------

Apache 2.0
